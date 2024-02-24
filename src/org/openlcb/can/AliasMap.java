package org.openlcb.can;

import org.openlcb.NodeID;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Maintains a 2-way map between nodes and CAN node ID aliases.
 *<p>
 * Input is CAN frames, looking for VerifyNodeID frames.
 * 
 * @author  Bob Jacobsen   Copyright 2010
 * @version $Revision$
 */
public class AliasMap {

    public AliasMap() {
    }
    java.util.HashMap<NodeID, Integer> iMap = new java.util.HashMap<NodeID, Integer>();
    java.util.HashMap<Integer, NodeID> nMap = new java.util.HashMap<Integer, NodeID>();
    java.util.List<Watcher> watchers = new ArrayList<>();
    private final static Logger logger = Logger.getLogger(AliasMap.class.getName());
    
    /// This interface allows an external component to watch for newly discovered aliases.
    public interface Watcher {
        /// Called when a new alias was discovered.
        void aliasAdded(NodeID id, int alias);
    }

    public synchronized void addWatcher(Watcher w) {
        watchers.add(w);
    }

    public void processFrame(OpenLcbCanFrame f) {
        // check type
        if (f.isInitializationComplete() || f.isVerifiedNID() || f.isAliasMapDefinition()) {
            // some nodes don't properly send their NodeID in the data part, so we armour against that.
            if (f.data.length >= 6) {
                Integer alias = Integer.valueOf(f.getSourceAlias());
                NodeID nid = f.getNodeID();
                insert(alias, nid);
            }
        } else if (f.isAliasMapReset()) {
            Integer alias = Integer.valueOf(f.getSourceAlias());
            remove(alias);
        }
    }
    
    public void insert(int alias, NodeID nid) {
        synchronized (this) {
            if (nMap.containsKey(alias) && nid.toLong() != nMap.get(alias).toLong()) {
                logger.warning("map contains alias "
                    +String.format("0x%03X", alias & 0xFFF)
                    +" for node "+nMap.get(alias)+" change to "+nid);
            }
            if (iMap.containsKey(nid) && alias != iMap.get(nid)) {
                logger.warning("map contains nodeID "+nid+" for alias "
                    +String.format("0x%03X", iMap.get(nid) & 0xFFF)
                    +" change to "
                    +String.format("0x%03X", alias & 0xFFF));
            }
            nMap.put(alias, nid);
            iMap.put(nid, alias);
        }
        for (Watcher w: watchers) {
            w.aliasAdded(nid, alias);
        }
    }
    
    public synchronized void remove(int alias) {
        NodeID nid = getNodeID(alias);
        if (nid == null) return;
        nMap.remove(alias);
        iMap.remove(nid);
    }
    
    public synchronized NodeID getNodeID(int alias) {
        NodeID retVal = nMap.get(Integer.valueOf(alias));
        if (retVal != null) return retVal;
        else return new NodeID();
    }
    public synchronized int getAlias(NodeID nid) {
        Integer r = iMap.get(nid);
        if (r == null) return -1;
        else return r.intValue();
    }
 }
