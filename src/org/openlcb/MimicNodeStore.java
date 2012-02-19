package org.openlcb;

import java.util.HashMap;
import java.util.Collection;

import java.beans.PropertyChangeListener;

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
        NodeMemo memo = map.get(msg.getSourceNodeID());
        if (memo == null) {
            memo = new NodeMemo(msg.getSourceNodeID());
            map.put(msg.getSourceNodeID(), memo);
            pcs.firePropertyChange("AddNode", null, memo);
        }
        // check for necessary updates in specific node
        System.out.println("about to forward "+msg);
        memo.put(msg, sender);
    }
    
    HashMap<NodeID, NodeMemo> map = new java.util.HashMap<NodeID, NodeMemo>();

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {pcs.addPropertyChangeListener(l);}
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {pcs.removePropertyChangeListener(l);}

    public class NodeMemo extends MessageDecoder {
        NodeID id;
        
        public NodeMemo(NodeID id) { this.id = id; }
        
        public NodeID getNodeID() {
            return id;
        }
        
        ProtocolIdentification pIdent = null;
        public void handleProtocolIdentificationReply(ProtocolIdentificationReplyMessage msg, Connection sender){
            // accept assumes from mimic'd node
            System.out.println("start protocol handling");
            pIdent = new ProtocolIdentification(msg);
            pcs.firePropertyChange("updateProtocol", null, pIdent);
        }  
        public ProtocolIdentification getProtocolIdentification() {
            return pIdent;
        }

        SimpleNodeIdent pSimpleNode = null;
        public void handleSimpleNodeIdentInfoReply(SimpleNodeIdentInfoReplyMessage msg, Connection sender){
            // accept assumes from mimic'd node
            System.out.println("start simple ident handling");
            if (pSimpleNode == null) 
                pSimpleNode = new SimpleNodeIdent(msg);
            else
                pSimpleNode.addMsg(msg);
            pcs.firePropertyChange("updateSimpleNodeIdent", null, pSimpleNode);
        }  
        public SimpleNodeIdent getSimpleNodeIdent() {
            return pSimpleNode;
        }

        java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
        public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {pcs.addPropertyChangeListener(l);}
        public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {pcs.removePropertyChangeListener(l);}
    }
}
