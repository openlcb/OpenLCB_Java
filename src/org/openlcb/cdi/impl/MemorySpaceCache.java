package org.openlcb.cdi.impl;

import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.cdi.impl.RangeCacheUtil.Range;
import org.openlcb.implementations.MemoryConfigurationService;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Maintains the connection to a specific remote node's specific memory space, and maintains a
 * cache of the information retrieved from there.
 * <p/>
 * Created by bracz on 4/2/16.
 */
public class MemorySpaceCache {
    private static final String TAG = "MemorySpaceCache";
    // This event will be fired when the cache is completely pre-filled.
    private static final String UPDATE_LOADING_COMPLETE = "UPDATE_LOADING_COMPLETE";
    // This event will be fired on the registered data listeners.
    private static final String UPDATE_DATA = "UPDATE_DATA";
    private final OlcbInterface connection;
    private final NodeID remoteNodeID;
    private final int space;
    private final RangeCacheUtil ranges = new RangeCacheUtil();
    private final NavigableMap<Range, byte[]> dataCache = new TreeMap<>();
    private final NavigableMap<Range, ChangeEntry> dataChangeListeners = new
            TreeMap<>();
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    private Range nextRangeToLoad = null;
    private int currentRangeNextOffset;
    private byte[] currentRangeData;

    public MemorySpaceCache(OlcbInterface connection, NodeID remoteNode, int space) {
        this.connection = connection;
        this.remoteNodeID = remoteNode;
        this.space = space;
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
     */
    public void addRangeToCache(int start, int end) {
        ranges.addRange(start, end);
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
    public void addRangeListener(int start, int end, PropertyChangeListener listener) {
        synchronized (this) {
            Range r = new Range(start, end);
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
     *
     * @param start offset (inclusive)
     * @param end   offset (exclusive)
     */
    private void notifyPartialRead(int start, int end) {
        PropertyChangeEvent ev = null;
        for (Map.Entry<Range, ChangeEntry> e : dataChangeListeners.entrySet()) {
            if (e.getKey().start < end && e.getKey().end > start) {
                // There is overlap
                if (e.getKey().end <= end) {
                    // Data is fully available
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
    private void notifyAfterWrite(int start, int end) {
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
        }
        continueLoading();
    }

    /**
     * Finds the next unloaded cached range and invokes load on it.
     */
    private void continueLoading() {
        if (dataCache.isEmpty()) return;
        if (nextRangeToLoad == null) {
            nextRangeToLoad = dataCache.firstKey();
        }
        while (nextRangeToLoad != null && dataCache.get(nextRangeToLoad) != null) {
            nextRangeToLoad = dataCache.higherKey(nextRangeToLoad);
        }
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
        if (currentRangeNextOffset < 0) {
            currentRangeNextOffset = nextRangeToLoad.start;
            currentRangeData = new byte[nextRangeToLoad.end - nextRangeToLoad.start];
            dataCache.put(nextRangeToLoad, currentRangeData);
        }
        int count = nextRangeToLoad.end - currentRangeNextOffset;
        if (count <= 0) {
            continueLoading();
            return;
        }
        if (count > 64) {
            count = 64;
        }
        connection.getMemoryConfigurationService().request(
                new MemoryConfigurationService.McsReadMemo(remoteNodeID, space,
                        currentRangeNextOffset, count) {
                    @Override
                    public void
                    handleWriteReply(int code) {
                        super.handleWriteReply(code);
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
                                    MemorySpaceCache.this.space + " expectedcount=" + this.count);
                        }
                        System.arraycopy(data, 0, currentRangeData, currentRangeNextOffset -
                                nextRangeToLoad.start, data.length);
                        notifyPartialRead(currentRangeNextOffset, currentRangeNextOffset + data
                                .length);
                        currentRangeNextOffset += data.length;
                        loadRange();
                    }
                }
        );
    }

    private Map.Entry<Range, byte[]> getCacheForRange(int offset, int len) {
        Range r = new Range(offset, Integer.MAX_VALUE);
        Map.Entry<Range, byte[]> entry = dataCache.floorEntry(r);
        if (entry == null) return null;
        if (entry.getKey().end < offset + len) {
            return null;
        }
        if (entry.getValue() == null) return null;
        return entry;
    }

    public byte[] read(int offset, int len) {
        Map.Entry<Range, byte[]> entry = getCacheForRange(offset, len);
        if (entry == null) return null;
        byte[] ret = new byte[len];
        System.arraycopy(entry.getValue(), offset - entry.getKey().start, ret, 0, len);
        return ret;
    }

    public void write(int offset, byte[] data) {
        int len = data.length;
        Map.Entry<Range, byte[]> entry = getCacheForRange(offset, len);
        if (entry != null && entry.getValue() != null) {
            System.arraycopy(data, 0, entry.getValue(), offset - entry.getKey().start, data.length);
        }
        connection.getMemoryConfigurationService().request(
                new MemoryConfigurationService.McsWriteMemo(remoteNodeID, space, offset, data));
        // @TODO: 4/2/16 Handle write errors and report to user somehow.
        notifyAfterWrite(offset, offset + data.length);
    }

    /**
     * Represents the registered listeners of a given range.
     */
    private class ChangeEntry {
        List<PropertyChangeListener> listeners = new ArrayList<>();
        int previousMax;
    }
}

