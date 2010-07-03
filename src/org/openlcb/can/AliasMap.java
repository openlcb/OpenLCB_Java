package org.openlcb.can;

import org.openlcb.*;

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
        if (f.isInitializationComplete()) {
            Integer alias = new Integer(f.getSourceAlias());
            NodeID nid = f.getNodeID();
            nMap.put(alias, nid);
            iMap.put(nid, alias);
        }
    }
    
    public NodeID getNodeID(int alias) {
        return nMap.get(new Integer(alias));
    }
    public int getAlias(NodeID nid) {
        Integer r = iMap.get(nid);
        if (r == null) return -1;
        else return r.intValue();
    }
 }
