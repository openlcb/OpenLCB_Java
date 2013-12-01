package org.openlcb.can;

import org.openlcb.NodeID;

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
    
    public void processFrame(OpenLcbCanFrame f) {
        // check type
        if (f.isInitializationComplete() || f.isVerifiedNID() || f.isAliasMapDefinition()) {
            Integer alias = Integer.valueOf(f.getSourceAlias());
            NodeID nid = f.getNodeID();
            insert(alias, nid);
        } else if (f.isAliasMapReset()) {
            Integer alias = Integer.valueOf(f.getSourceAlias());
            remove(alias);
        }
    }
    
    public void insert(int alias, NodeID nid) {
        nMap.put(alias, nid);
        iMap.put(nid, alias);
    }
    
    public void remove(int alias) {
        NodeID nid = getNodeID(alias);
        if (nid == null) return;
        nMap.remove(alias);
        iMap.remove(nid);
    }
    
    public NodeID getNodeID(int alias) {
        NodeID retVal = nMap.get(Integer.valueOf(alias));
        if (retVal != null) return retVal;
        else return new NodeID();
    }
    public int getAlias(NodeID nid) {
        Integer r = iMap.get(nid);
        if (r == null) return -1;
        else return r.intValue();
    }
 }
