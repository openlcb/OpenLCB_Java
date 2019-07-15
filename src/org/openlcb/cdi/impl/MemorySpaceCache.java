package org.openlcb.cdi.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Queue;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.cdi.impl.RangeCacheUtil.Range;
import org.openlcb.implementations.MemoryConfigurationService;

/**
 * Maintains the connection to a specific remote node's specific memory space, and maintains a
 * cache of the information retrieved from there.
 * <p>
 * Created by bracz on 4/2/16.
 */
public class MemorySpaceCache {
    // This event will be fired when the cache is completely pre-filled.
    public static final String UPDATE_LOADING_COMPLETE = "UPDATE_LOADING_COMPLETE";
    // This event will be fired on the registered data listeners.
    public static final String UPDATE_DATA = "UPDATE_DATA";
    private static final Logger logger = Logger.getLogger(MemorySpaceCache.class.getName());
    private final int space;
    private final RangeCacheUtil ranges = new RangeCacheUtil();
    private final NavigableMap<Range, byte[]> dataCache = new TreeMap<>();
    private final NavigableMap<Range, ChangeEntry> dataChangeListeners = new
            TreeMap<>();
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    private Range nextRangeToLoad = null;
    private long currentRangeNextOffset;
    private byte[] currentRangeData;
    private Queue<Range> rangesToLoad = new LinkedList<>();
    private final ReadWriteAccess access;
    private final String remoteNodeString; // used for error printouts


    public MemorySpaceCache(OlcbInterface connection, final NodeID remoteNode, int space) {
        final MemoryConfigurationService mcs = connection.getMemoryConfigurationService();
        this.remoteNodeString = remoteNode.toString();
        this.access = new ReadWriteAccess() {
            @Override
            public void doWrite(long address, int space, byte[] data, MemoryConfigurationService
                    .McsWriteHandler handler) {
                mcs.requestWrite(remoteNode, space, address, data, handler);
            }

            @Override
            public void doRead(long address, int space, int length, MemoryConfigurationService
                    .McsReadHandler handler) {
                mcs.requestRead(remoteNode, space, address, length, handler);
            }
        };
        this.space = space;
    }

    public MemorySpaceCache(ReadWriteAccess access, int space) {
        this.access = access;
        this.space = space;
        this.remoteNodeString = "(mock)";
    }

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    /**
     * Prepares for caching a given range.
     *
     * @param start address of first byte to be cached (inclusive)
     * @param end   address of first byte after the cached region
     * @param nullTerminated true for string ranges whose load can be stopped at the first null.
     */
    public void addRangeToCache(long start, long end, boolean nullTerminated) {
        ranges.addRange(start, end, nullTerminated);
    }

    /**
     * Registers a listener to be called when a given address range experiences a change. Will
     * only be called upon initial load when the entire range has been loaded.
     *
     * @param start    address in the space of the first monitored byte (includive)
     * @param end      address of the first byte after the monitored range (i.e. range end
     *                 exclusive)
     * @param listener callback to invoke
     */
    public void addRangeListener(long start, long end, boolean nullTerminated,
                                 PropertyChangeListener listener) {
        synchronized (this) {
            Range r = new Range(start, end, nullTerminated);
            ChangeEntry lt = dataChangeListeners.get(r);
            if (lt == null) {
                lt = new ChangeEntry();
                dataChangeListeners.put(r, lt);
            }
            lt.listeners.add(listener);
        }
    }

    /**
     * Sends an data updated event to all listeners that are registered to be interested in
     * a given range. Skips those listeners that extend beyond 'end', given the assumption that
     * the data is read from the beginning of the range.
     *  @param start offset (inclusive)
     * @param end   offset (exclusive)
     * @param hasZero true if the data payload loaded has a zero byte.
     */
    private void notifyPartialRead(long start, long end, boolean hasZero) {
        PropertyChangeEvent ev = null;
        for (Map.Entry<Range, ChangeEntry> e : dataChangeListeners.entrySet()) {
            if (e.getKey().start < end && e.getKey().end > start) {
                // There is overlap
                boolean needNotify = false;
                if (e.getKey().end <= end) needNotify = true; // Data is fully available
                if ((start >= e.getKey().start) && e.getKey().nullTerminated && hasZero) {
                    needNotify = true;
                }
                if (needNotify) {
                    if (ev == null) ev = new PropertyChangeEvent(this, UPDATE_DATA, null, null);
                    for (PropertyChangeListener l : e.getValue().listeners) {
                        l.propertyChange(ev);
                    }
                }
            }
        }
    }

    /**
     * Sends an data updated event to all listeners that are registered to be interested in
     * a given range.
     *
     * @param start offset (inclusive)
     * @param end   offset (exclusive)
     */
    private void notifyAfterWrite(long start, long end) {
        PropertyChangeEvent ev = null;
        for (Map.Entry<Range, ChangeEntry> e : dataChangeListeners.entrySet()) {
            if (e.getKey().start < end && e.getKey().end > start) {
                // There is overlap
                if (ev == null) ev = new PropertyChangeEvent(this, UPDATE_DATA, null, null);
                for (PropertyChangeListener l : e.getValue().listeners) {
                    l.propertyChange(ev);
                }
            }
        }
    }

    /**
     * Instructs the class to load all data that needs to be pre-cached.
     */
    public void fillCache() {
        if (!dataCache.isEmpty()) {
            throw new UnsupportedOperationException("The data cache can be filled only once.");
        }
        List<Range> rlist = ranges.getRanges();
        if (rlist.isEmpty()) return;
        for (Range r : rlist) {
            dataCache.put(r, null);
            rangesToLoad.add(r);
        }
        continueLoading();
    }

    /**
     * Finds the next unloaded cached range and invokes load on it.
     */
    private void continueLoading() {
        if (dataCache.isEmpty() && rangesToLoad.isEmpty()) return;
        nextRangeToLoad = rangesToLoad.poll();
        if (nextRangeToLoad == null) {
            // loading complete.
            firePropertyChange(UPDATE_LOADING_COMPLETE, null, null);
            return;
        }
        currentRangeNextOffset = -1;
        loadRange();
    }

    /**
     * Loads the nextRangeToLoad range.
     */
    private void loadRange() {
        if (currentRangeNextOffset < 0) { // first cut in loading this range
            int len = (int)(nextRangeToLoad.end - nextRangeToLoad.start);
            // Try to check if there is an existing range covering the stuff to load.
            Map.Entry<Range, byte[]> cachedRange = getCacheForRange(nextRangeToLoad.start, len);
            if (cachedRange == null) {
                currentRangeData = new byte[len];
                dataCache.put(nextRangeToLoad, currentRangeData);
                currentRangeNextOffset = nextRangeToLoad.start;
            } else {
                currentRangeData = cachedRange.getValue();
                currentRangeNextOffset = nextRangeToLoad.start;
                if (!nextRangeToLoad.equals(cachedRange.getKey())) {
                    // We must make sure that the start offset is the same as the cached range,
                    // otherwise the bytes will be copied to the wrong place inside the data array.
                    // When finding an overlapping range, we always disable null termination.
                    nextRangeToLoad = new Range(cachedRange.getKey().start, nextRangeToLoad.end,
                            false);
                }
            }
        }
        int count = (int)(nextRangeToLoad.end - currentRangeNextOffset);
        if (count <= 0) {
            continueLoading();
            return;
        }
        if (count > 64) {
            count = 64;
        }
        final int fcount = count;
        access.doRead(currentRangeNextOffset, space, count,
                new MemoryConfigurationService.McsReadHandler() {
                    @Override
                    public void handleFailure(int code) {
                        logger.warning("Error reading memory space cache: dest " + remoteNodeString +
                                "space" + space + " offset " + currentRangeNextOffset + " error " +
                                "0x" + Integer.toHexString(code));
                        // ignore and continue reading other stuff.
                        currentRangeNextOffset += fcount;
                        loadRange();
                    }

                    @Override
                    public void
                    handleReadData(NodeID dest, int space, long address, byte[] data) {
                        if (currentRangeNextOffset != address) {
                            throw new RuntimeException("spurious return data for address=" +
                                    address + " length " + data.length);
                        }
                        if (data.length + currentRangeNextOffset - nextRangeToLoad.start >
                                currentRangeData.length) {
                            throw new RuntimeException("return data won't fit, space=" + space +
                                    " address= " + address + " length=" + data.length + " " +
                                    "expected" +
                                    " address=" + currentRangeNextOffset + " expectedspace=" +
                                    MemorySpaceCache.this.space + " expectedcount=" + fcount);
                        }
                        boolean hasZero = false;
                        if (data.length == 0) {
                            logger.warning(String.format("Datagram read returned 0 bytes. " +
                                            "Remote node %s, space %d, address 0x%x", dest
                                            .toString(),
                                    space, address));
                            currentRangeNextOffset += fcount;
                        } else {
                            System.arraycopy(data, 0, currentRangeData, (int)
                                    (currentRangeNextOffset -
                                            nextRangeToLoad.start), data.length);
                            for (int i = 0; i < data.length; ++i) {
                                if (data[i] == 0) {
                                    hasZero = true;
                                    break;
                                }
                            }
                            notifyPartialRead(currentRangeNextOffset, currentRangeNextOffset + data
                                    .length, hasZero);
                            currentRangeNextOffset += data.length;
                        }
                        if (hasZero && nextRangeToLoad.nullTerminated) {
                            continueLoading();
                        } else {
                            loadRange();
                        }
                    }
                });
    }

    private Map.Entry<Range, byte[]> getCacheForRange(long offset, int len) {
        Range r = new Range(offset, Integer.MAX_VALUE, true);
        Map.Entry<Range, byte[]> entry = dataCache.floorEntry(r);
        if (entry == null) return null;
        if (entry.getKey().end < offset + len) {
            return null;
        }
        if (entry.getValue() == null) return null;
        return entry;
    }

    public byte[] read(long offset, int len) {
        Map.Entry<Range, byte[]> entry = getCacheForRange(offset, len);
        if (entry == null) return null;
        byte[] ret = new byte[len];
        System.arraycopy(entry.getValue(), (int) (offset - entry.getKey().start), ret, 0, len);
        return ret;
    }

    public void write(final long offset, final byte[] data, final ConfigRepresentation.CdiEntry
            cdiEntry) {
        int len = data.length;
        Map.Entry<Range, byte[]> entry = getCacheForRange(offset, len);
        if (entry != null && entry.getValue() != null) {
            System.arraycopy(data, 0, entry.getValue(), (int) (offset - entry.getKey().start), data.length);
        }
        logger.finer("Writing to space " + space + " offset 0x" + Long.toHexString(offset) +
                " payload length " + data.length);

        class RepeatedWrite implements MemoryConfigurationService.McsWriteHandler {
            int dataOffset = 0;

            public void next() {
                int len = Math.min(data.length-dataOffset, 64);
                byte[] p;
                if (len == data.length) {
                    p = data;
                } else {
                    p = new byte[len];
                    System.arraycopy(data, dataOffset, p, 0, len);
                }
                long writeAddress = offset + dataOffset;
                dataOffset += len;
                access.doWrite(writeAddress, space, p, this);
            }

            @Override
            public void handleFailure(int errorCode) {
                logger.warning(String.format("Write failed (space %d address %d): 0x" +
                        "%04x", space, offset, errorCode));
                cdiEntry.fireWriteComplete();
            }

            @Override
            public void handleSuccess() {
                logger.finer(String.format("Write complete (space %d address %d).",
                        space, offset));
                if (dataOffset >= data.length) {
                    cdiEntry.fireWriteComplete();
                } else {
                    next();
                }
            }
        }
        new RepeatedWrite().next();
        // TODO: 4/2/16 Handle write errors and report to user somehow.
        notifyAfterWrite(offset, offset + data.length);
    }

    /**
     * Performs a refresh of some data. Calls the data update listeners when done.
     * @param origin address of first byte in memory space to reload
     * @param size number of bytes to reload
     * @param nullTerminated true if this reload can stop at a null byte.
     */
    public void reload(long origin, int size, boolean nullTerminated) {
        rangesToLoad.add(new Range(origin, origin + size, nullTerminated));
        continueLoading();
    }

    /**
     * Represents the registered listeners of a given range.
     */
    private class ChangeEntry {
        List<PropertyChangeListener> listeners = new ArrayList<>();
        int previousMax;
    }
}

