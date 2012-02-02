package org.openlcb;

import java.util.HashMap;
import java.util.Collection;

/**
 * Store containing mimic proxies for nodes on external connections
 * <p>
 * Provides a Connection for incoming Messages.
 *
 * @author  Bob Jacobsen   Copyright 2011
 * @version $Revision$
 */
public class MimicNodeStore extends MessageDecoder implements Connection {
    public MimicNodeStore() {
    }
    
    public Collection<NodeMemo> getNodeMemos() {
        return map.values();
    } 
    
    public void put(Message msg, Connection sender) {
        if (map.get(msg.getSourceNodeID()) == null)
            map.put(msg.getSourceNodeID(), new NodeMemo(msg.getSourceNodeID()));
    }
    
    HashMap<NodeID, NodeMemo> map = new java.util.HashMap<NodeID, NodeMemo>();
    
    class NodeMemo {
        NodeID id;
        
        public NodeMemo(NodeID id) { this.id = id; }
        
        public NodeID getNodeID() {
            return id;
        }
    }
}
