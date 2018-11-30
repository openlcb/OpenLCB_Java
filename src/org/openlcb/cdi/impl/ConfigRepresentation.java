package org.openlcb.cdi.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.openlcb.DefaultPropertyListenerSupport;
import org.openlcb.EventID;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.Utilities;
import org.openlcb.cdi.CdiRep;
import org.openlcb.cdi.jdom.CdiMemConfigReader;
import org.openlcb.cdi.jdom.JdomCdiReader;
import org.openlcb.cdi.jdom.XmlHelper;
import org.openlcb.implementations.MemoryConfigurationService;

/**
 * Maintains a parsed cache of the CDI config of a remote node. Responsible for fetching the CDI,
 * parsing the XML, identifying all variables with their correct offsets and creating useful
 * internal representations of these variables. Performs reads and writes to the configuration
 * space.
 *
 * Created by bracz on 3/29/16.
 */
public class ConfigRepresentation extends DefaultPropertyListenerSupport {
    // Fires when the loading state changes.
    public static final String UPDATE_STATE = "UPDATE_STATE";
    // Fired when the CDI is loaded and the representation is ready.
    public static final String UPDATE_REP = "UPDATE_REP";
    // Fired when all the caches have been pre-filled.
    public static final String UPDATE_CACHE_COMPLETE = "UPDATE_CACHE_COMPLETE";
    // Fired on the individual internal entries when they are changed.
    public static final String UPDATE_ENTRY_DATA = "UPDATE_ENTRY_DATA";
    // Fired on an CDI entry when the write method completes.
    public static final String UPDATE_WRITE_COMPLETE = "PENDING_WRITE_COMPLETE";
    private static final Logger logger = Logger.getLogger(ConfigRepresentation.class.getName());
    static final Charset UTF8 = Charset.forName("UTF8");

    private final OlcbInterface connection;
    private final NodeID remoteNodeID;
    private final ReadWriteAccess mockAccess;
    private CdiRep cdiRep;
    private String state = "Uninitialized";
    private CdiContainer root = null;
    private final Map<Integer, MemorySpaceCache> spaces = new TreeMap<>();
    private final Map<String, CdiEntry> variables = new HashMap<>();
    // Last time the progressbar was updated from the load.
    private long lastProgress;


    /**
     * Connects to a node, populates the cache by fetching and parsing the CDI.
     * @param connection OpenLCB network.
     * @param remoteNodeID the node to fetch CDI from.
     */
    public ConfigRepresentation(OlcbInterface connection, NodeID remoteNodeID) {
        this.connection = connection;
        this.remoteNodeID = remoteNodeID;
        this.mockAccess = null;
        triggerFetchCdi();
    }

    public ConfigRepresentation(ReadWriteAccess memoryAccess, CdiRep xmlRep) {
        this.connection = null;
        this.remoteNodeID = null;
        this.mockAccess = memoryAccess;
        cdiRep = xmlRep;
        parseRep();
    }

    public @Nullable OlcbInterface getConnection() { return connection; }
    public @Nullable NodeID getRemoteNodeID() { return remoteNodeID; }

    /**
     * Retrieves the CDI from the remote node, and if successful, calls @link parseRep.
     */
    private void triggerFetchCdi() {
        new CdiMemConfigReader(remoteNodeID, connection,
                MemoryConfigurationService.SPACE_CDI).startLoadReader(new CdiMemConfigReader
                .ReaderAccess() {

            @Override
            public void progressNotify(long bytesRead, long totalBytes) {
                lastProgress = new Date().getTime();
                if (totalBytes > 0) {
                    setState(String.format("Loading: %.2f%% complete", bytesRead * 100.0 /
                            totalBytes));
                } else {
                    setState(String.format("Loading: %d bytes complete", bytesRead));
                }
            }

            @Override
            public void provideReader(Reader r) {
                try {
                    cdiRep = new JdomCdiReader().getRep(XmlHelper.parseXmlFromReader(r));
                } catch (Exception e) {
                    String error = "Failed to parse CDI output: " + e.toString();
                    logger.warning(error);
                    setState(error);
                    return;
                }
                parseRep();
            }
        });
    }

    private void parseRep() {
        root = new Root(cdiRep);
        setState("Representation complete.");
        prefillCaches();
        firePropertyChange(UPDATE_REP, null, root);
    }

    public CdiRep getCdiRep() {
        return cdiRep;
    }

    int pendingCacheFills = 0;
    PropertyChangeListener prefillListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            if (propertyChangeEvent.getPropertyName().equals(MemorySpaceCache
                    .UPDATE_LOADING_COMPLETE)) {
                synchronized (this) {
                    if (--pendingCacheFills == 0) {
                        firePropertyChange(UPDATE_CACHE_COMPLETE, null, null);
                        for (MemorySpaceCache sp : spaces.values()) {
                            sp.removePropertyChangeListener(prefillListener);
                        }
                    }
                }
            }
        }
    };

    private void prefillCaches() {
        variables.clear();
        visit(new Visitor() {
                  @Override
                  public void visitLeaf(final CdiEntry e) {
                      variables.put(e.key, e);
                      MemorySpaceCache cache = getCacheForSpace(e.space);
                      cache.addRangeToCache(e.origin, e.origin + e.size);
                      cache.addRangeListener(e.origin, e.origin + e.size, new
                              PropertyChangeListener() {
                                  @Override
                                  public void propertyChange(PropertyChangeEvent event) {
                                      e.fireUpdate();
                                  }
                              });
                  }
              }
        );
        pendingCacheFills = spaces.size();
        for (MemorySpaceCache sp : spaces.values()) {
            sp.addPropertyChangeListener(prefillListener);
            // This will send off the first read, then continue asynchronously.
            sp.fillCache();
        }
    }

    public synchronized void reloadAll() {
        spaces.clear();  // destroys all the caches
        prefillCaches();
    }

    /**
     * @return the internal representation of the root entry. The root entry contains all
     * segments as children.
     */
    public CdiContainer getRoot() {
        return root;
    }

    public String getRemoteNodeAsString() {
        if (remoteNodeID == null) {
            return "mock";
        } else {
            return remoteNodeID.toString();
        }
    }

    public @Nullable CdiEntry getVariableForKey(@NonNull String key) {
        return variables.get(key);
    }

    private synchronized MemorySpaceCache getCacheForSpace(int space) {
        if (spaces.containsKey(space)) {
            return spaces.get(space);
        } else {
            MemorySpaceCache s;
            if (connection != null) {
                s = new MemorySpaceCache(connection, remoteNodeID, space);
            } else {
                s = new MemorySpaceCache(mockAccess, space);
            }
            spaces.put(space, s);
            return s;
        }
    }

    /**
     * Performs a visitation of the entire tree (starting at the root node).
     * @param v is an implementation of a tree Visitor.
     */
    public void visit(Visitor v) {
        v.visitContainer(getRoot());
    }

    /**
     * Processes the CdiRep entries of a children of a group and builds the internal representation
     * for each entry.
     *
     * @param baseName name of the prefix of all these group entries
     * @param segment  memory configuration segment number
     * @param items    the list of CDI entries to render
     * @param output   the list of output variables to append to
     * @param origin   offset in the segment of the beginning of the group payload
     * @return the number of bytes (one repeat of) this group covers in the address space
     */
    private long processGroup(String baseName, int segment, List<CdiRep.Item> items,
                             List<CdiEntry> output, long origin) {
        if (items == null) return 0;
        long base = origin;
        for (int i = 0; i < items.size(); i++) {
            CdiRep.Item it = (CdiRep.Item) items.get(i);

            origin = origin + it.getOffset();
            CdiEntry entry = null;
            String entryName = it.getName();
            if (entryName == null || entryName.trim().isEmpty()) {
                entryName = "child" + it.getIndexInParent();
            }
            String name = baseName + "." + entryName;
            if (it instanceof CdiRep.Group) {
                entry = new GroupEntry(name, (CdiRep.Group) it, segment, origin);
            } else if (it instanceof CdiRep.IntegerRep) {
                entry = new IntegerEntry(name, (CdiRep.IntegerRep) it, segment, origin);
            } else if (it instanceof CdiRep.EventID) {
                entry = new EventEntry(name, (CdiRep.EventID) it, segment, origin);
            } else if (it instanceof CdiRep.StringRep) {
                entry = new StringEntry(name, (CdiRep.StringRep) it, segment, origin);
            } else {
                logger.log(Level.SEVERE, "could not process CDI entry type of {0}", it);
            }
            if (entry != null) {
                origin = entry.origin + entry.size;
                output.add(entry);
            }
        }
        return origin - base;
    }

    private void setState(String state) {
        String oldState = this.state;
        this.state = state;
        firePropertyChange(UPDATE_STATE, oldState, this.state);
    }

    public String getStatus() {
        return state;
    }

    /**
     * Checks that the representation is complete. If it is not, starts a new load of the
     * representation.
     */
    public void restartIfNeeded() {
        if (root == null && (lastProgress + 5000) < (new Date().getTime())) {
            triggerFetchCdi();
        }
    }

    /**
     * Interface for all internal representation of nodes that have children.
     */
    public interface CdiContainer {
        List<CdiEntry> getEntries();
    }

    /**
     * Interface for traversing the tree of settings. The default implementation will call
     * visitLeaf for each variable, and recurse automatically for each node with children.
     */
    public static class Visitor {
        public void visitEntry(CdiEntry e) {
            if (e instanceof StringEntry) {
                visitString((StringEntry) e);
            } else if (e instanceof IntegerEntry) {
                visitInt((IntegerEntry) e);
            } else if (e instanceof EventEntry) {
                visitEvent((EventEntry) e);
            } else if (e instanceof GroupRep) {
                visitGroupRep((GroupRep) e);
            } else if (e instanceof GroupEntry) {
                visitGroup((GroupEntry) e);
            } else if (e instanceof SegmentEntry) {
                visitSegment((SegmentEntry) e);
            } else if (e instanceof CdiContainer) {
                visitContainer((CdiContainer) e);
            } else {
                logger.warning("Don't know how to visit entry: " + e.getClass().getName());
            }
        }

        public void visitLeaf(CdiEntry e) {
        }

        public void visitString(StringEntry e) {
            visitLeaf(e);
        }

        public void visitInt(IntegerEntry e) {
            visitLeaf(e);
        }

        public void visitEvent(EventEntry e) {
            visitLeaf(e);
        }

        public void visitGroupRep(GroupRep e) {
            visitContainer(e);
        }

        public void visitGroup(GroupEntry e) {
            visitContainer(e);
        }

        public void visitSegment(SegmentEntry e) {
            visitContainer(e);
        }

        public void visitContainer(CdiContainer c) {
            for (CdiEntry e : c.getEntries()) {
                visitEntry(e);
            }
        }
    }

    /**
     * Base class for all internal representations of the nodes (both variables as well as groups
     * and segments).
     */
    public abstract class CdiEntry extends DefaultPropertyListenerSupport {
        /// Memory space number.
        public int space;
        /// Address of the first byte of this item in the memory space.
        public long origin;
        /// The number of bytes that this component takes in the configuration space.
        public int size;
        /// Internal key for this variable or group
        public String key;
        /// String-rendered value of this entry. Populated for leaf entries.
        public String lastVisibleValue = null;

        public abstract CdiRep.Item getCdiItem();
        protected void updateVisibleValue() {}

        public void fireUpdate() {
            updateVisibleValue();
            firePropertyChange(UPDATE_ENTRY_DATA, null, null);
        }

        public void fireWriteComplete() {
            firePropertyChange(UPDATE_WRITE_COMPLETE, null, null);
        }

        /// Reads the values again from the original source.
        public void reload() {
            MemorySpaceCache cache = getCacheForSpace(space);
            cache.reload(origin, size);
        }
    }

    public class Root implements CdiContainer {
        public final List<CdiEntry> items;
        public final CdiRep rep;

        /**
         * Parses the root of the CdiRep into an internal representation that is a container.
         * @param rep the CDI representation
         */
        public Root(CdiRep rep) {
            items = new ArrayList<>();
            this.rep = rep;
            for (CdiRep.Segment e : rep.getSegments()) {
                items.add(new SegmentEntry(e));
            }
        }

        @Override
        public List<CdiEntry> getEntries() {
            return items;
        }
    }

    /**
     * Represents a Segment that looks like a group as well as an Entry to allow common handling
     * of groups and segments.
     */
    public class SegmentEntry extends CdiEntry implements CdiContainer, CdiRep.Item {
        public final CdiRep.Segment segment;
        public final List<CdiEntry> items;

        public SegmentEntry(CdiRep.Segment segment) {
            this.segment = segment;
            this.items = new ArrayList<>();
            this.key = getName();
            if (key == null || key.trim().isEmpty()) {
                key = "seg" + segment.getIndexInParent();
            }
            this.origin = segment.getOrigin();
            this.space = segment.getSpace();
            this.size = (int)processGroup(key, this.space, segment.getItems(), this.items, this
                    .origin);
        }

        @Override
        public List<CdiEntry> getEntries() {
            return items;
        }

        @Override
        public CdiRep.Item getCdiItem() {
            return this;
        }

        @Override
        public void reload() {
        }

        @Override
        public String getName() {
            return segment.getName();
        }

        @Override
        public String getDescription() {
            return segment.getDescription();
        }

        @Override
        public CdiRep.Map getMap() {
            return segment.getMap();
        }

        @Override
        public int getOffset() {
            return segment.getOrigin();
        }

        @Override
        public int getIndexInParent() {
            return segment.getIndexInParent();
        }
    }

    /**
     * Base class for both repeated and non-repeated groups.
     */
    public class GroupBase extends CdiEntry implements CdiContainer {
        public final CdiRep.Group group;
        public final List<CdiEntry> items;

        public GroupBase(String name, CdiRep.Group group, int segment, long origin) {
            this.key = name;
            this.space = segment;
            this.origin = origin;
            this.group = group;
            this.items = new ArrayList<>();
        }

        @Override
        public List<CdiEntry> getEntries() {
            return items;
        }

        @Override
        public CdiRep.Item getCdiItem() {
            return group;
        }
    }

    /**
     * Represents one repeat of a repeated group. Contains a unique entry of all children.
     */
    public class GroupRep extends GroupBase {
        /**
         *
         * @param name is the string key of this group repeat
         * @param group is the base CDI representation
         * @param segment is the memory space number
         * @param origin is the address of this repeat in that memory space (All skips are
         *               already performed)
         * @param index is the 1-based index of this repeat of the given group
         */
        GroupRep(String name, CdiRep.Group group, int segment, long origin, int index) {
            super(name, group, segment, origin);
            size = (int) processGroup(name, segment, group.getItems(), items, origin);
            this.index = index;
        }
        // The 1-based index of this replica.
        public int index;
    }

    /**
     * Represents the root entry of a group. If the group is repeated, the children will be the
     * individual repeats. If the group is not repeated, the children will be the members in this
     * group.
     */
    public class GroupEntry extends GroupBase {
        /**
         * @param baseName is the string key of this group (including the name of the current group)
         * @param group is the CDI representation
         * @param segment is the memory space number
         * @param origin is the address of this repeat in that memory space (all skips are
         *               already performed)
         */
        GroupEntry(String baseName, CdiRep.Group group, int segment, long origin) {
            super(baseName, group, segment, origin);
            if (group.getReplication() <= 1) {
                size = (int) processGroup(baseName, segment, group.getItems(), this.items, this
                        .origin);
            } else {
                size = 0;
                for (int i = 0; i < group.getReplication(); ++i) {
                    CdiEntry e = new GroupRep(baseName + "(" + i + ")", group, segment, origin,
                            i + 1);
                    items.add(e);
                    origin += e.size;
                    size += e.size;
                }
            }
        }
    }

    /**
     * Represents an integer variable.
     */
    public class IntegerEntry extends CdiEntry {
        public CdiRep.IntegerRep rep;

        IntegerEntry(String name, CdiRep.IntegerRep rep, int segment, long origin) {
            this.key = name;
            this.space = segment;
            this.origin = origin;
            this.rep = rep;
            this.size = rep.getSize();
        }

        @Override
        public CdiRep.Item getCdiItem() {
            return rep;
        }

        @Override
        protected void updateVisibleValue() {
            lastVisibleValue = Long.toString(getValue());
            CdiRep.Map map = rep.getMap();
            if (map != null && map.getKeys().size() > 0) {
                String value = map.getEntry(lastVisibleValue);
                if (value != null) lastVisibleValue = value;
            }
        }

        public long getValue() {
            MemorySpaceCache cache = getCacheForSpace(space);
            byte[] b = cache.read(origin, size);
            if (b == null) return 0;
            long ret = 0;
            for (int i = 0; i < b.length; ++i) {
                ret <<= 8;
                int p = b[i] & 0xff;
                //if (p < 0) p += 128;
                ret |= p;
            }
            return ret;
        }

        public void setValue(long value) {
            MemorySpaceCache cache = getCacheForSpace(space);
            byte[] b = new byte[size];
            for (int i = size - 1; i >= 0; --i) {
                b[i] = (byte)(value & 0xff);
                value >>= 8;
            }
            cache.write(origin, b, this);
        }
    }

    /**
     * Represents an event variable.
     */
    public class EventEntry extends CdiEntry {
        public CdiRep.EventID rep;

        EventEntry(String name, CdiRep.EventID rep, int segment, long origin) {
            this.key = name;
            this.space = segment;
            this.origin = origin;
            this.rep = rep;
            this.size = 8;
        }

        @Override
        public CdiRep.Item getCdiItem() {
            return rep;
        }

        @Override
        protected void updateVisibleValue() {
            EventID v = getValue();
            if (v != null) {
                lastVisibleValue = Utilities.toHexDotsString(v.getContents());
            } else {
                lastVisibleValue = "";
            }
        }

        public EventID getValue() {
            MemorySpaceCache cache = getCacheForSpace(space);
            byte[] b = cache.read(origin, size);
            if (b == null) return null;
            return new EventID(b);
        }

        public void setValue(EventID event) {
            MemorySpaceCache cache = getCacheForSpace(space);
            byte[] b = event.getContents();
            if (b == null) return;
            cache.write(origin, b, this);
        }
    }

    /**
     * Represents a string variable.
     */
    public class StringEntry extends CdiEntry {
        public CdiRep.StringRep rep;

        StringEntry(String name, CdiRep.StringRep rep, int segment, long origin) {
            this.key = name;
            this.space = segment;
            this.origin = origin;
            this.rep = rep;
            this.size = rep.getSize();
        }

        @Override
        public CdiRep.Item getCdiItem() {
            return rep;
        }

        @Override
        protected void updateVisibleValue() {
            lastVisibleValue = getValue();
        }

        public String getValue() {
            MemorySpaceCache cache = getCacheForSpace(space);
            byte[] b = cache.read(origin, size);
            if (b == null) return null;
            // We search for a terminating null byte and clip the string there.
            int len = 0;
            while (len < b.length && b[len] != 0) ++len;
            byte[] rep = new byte[len];
            System.arraycopy(b, 0, rep, 0, len);
            String ret = new String(rep, UTF8);
            return ret;
        }

        public void setValue(String value) {
            MemorySpaceCache cache = getCacheForSpace(space);
            byte[] b = new byte[size];
            byte[] f;
            f = value.getBytes(UTF8);
            System.arraycopy(f, 0, b, 0, Math.min(f.length, b.length - 1));
            cache.write(this.origin, b, this);
        }
    }

}
