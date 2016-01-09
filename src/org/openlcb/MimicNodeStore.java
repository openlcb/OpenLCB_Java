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
public class MimicNodeStore extends AbstractConnection {

    public static final String ADD_PROP_NODE = "AddNode";

    public MimicNodeStore(Connection connection, NodeID node) {
        this.connection = connection;
        this.node = node;
    }
    
    Connection connection;
    NodeID node;
    
    public Collection<NodeMemo> getNodeMemos() {
        return map.values();
    } 
    
    public void put(Message msg, Connection sender) {
        NodeMemo memo = addNode(msg.getSourceNodeID());
        // check for necessary updates in specific node
        memo.put(msg, sender);
    }
    
    public NodeMemo addNode(NodeID id) {
        NodeMemo memo = map.get(id);
        if (memo == null) {
            memo = new NodeMemo(id);
            map.put(id, memo);
            pcs.firePropertyChange(ADD_PROP_NODE, null, memo);
        }
        return memo;
    }
    
    /**
     * If node not present, initiate process to find it.
     * @return NodeMemo already known, but note you have to 
     * register listeners before calling in any case
     */
    public NodeMemo findNode(NodeID id) {
        NodeMemo memo = map.get(id);
        if (memo != null) return memo;
        
        // create and send targeted request
        connection.put(new VerifyNodeIDNumberMessage(node, id), null);
        return null;
    }
    
    public SimpleNodeIdent getSimpleNodeIdent(NodeID dest) {
        NodeMemo memo = map.get(dest);
        if (memo == null) {
            return null;
        } else {
            return memo.getSimpleNodeIdent();
        }
    }
    
    public ProtocolIdentification getProtocolIdentification(NodeID dest) {
        NodeMemo memo = map.get(dest);
        if (memo == null) {
            return null;
        } else {
            return memo.getProtocolIdentification();
        }
    }
    
    HashMap<NodeID, NodeMemo> map = new java.util.HashMap<NodeID, NodeMemo>();

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {pcs.addPropertyChangeListener(l);}
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {pcs.removePropertyChangeListener(l);}

    public class NodeMemo extends MessageDecoder {
        public static final String UPDATE_PROP_SIMPLE_NODE_IDENT = "updateSimpleNodeIdent";
        public static final String UPDATE_PROP_PROTOCOL = "updateProtocol";
        NodeID id;
        
        public NodeMemo(NodeID id) { this.id = id; }
        
        public NodeID getNodeID() {
            return id;
        }
        
        ProtocolIdentification pIdent = null;
        public void handleProtocolIdentificationReply(ProtocolIdentificationReplyMessage msg, Connection sender){
            // accept assumes from mimic'd node
            pIdent = new ProtocolIdentification(msg);
            pcs.firePropertyChange(UPDATE_PROP_PROTOCOL, null, pIdent);
        }  
        public ProtocolIdentification getProtocolIdentification() {
            if (pIdent == null) {
                pIdent = new ProtocolIdentification(node, id);
                pIdent.start(connection);
            }
            return pIdent;
        }

        SimpleNodeIdent pSimpleNode = null;
        public void handleSimpleNodeIdentInfoReply(SimpleNodeIdentInfoReplyMessage msg, Connection sender){
            // accept assumes from mimic'd node
            if (pSimpleNode == null) 
                pSimpleNode = new SimpleNodeIdent(msg);
            else
                pSimpleNode.addMsg(msg);
            pcs.firePropertyChange(UPDATE_PROP_SIMPLE_NODE_IDENT, null, pSimpleNode);
        }  
        public SimpleNodeIdent getSimpleNodeIdent() {
            if (pSimpleNode == null) {
                pSimpleNode = new SimpleNodeIdent(node, id);
                pSimpleNode.start(connection);
            }
            return pSimpleNode;
        }

        public void handleOptionalIntRejected(OptionalIntRejectedMessage msg, Connection sender){
            if (msg.getMti() == MessageTypeIdentifier.SimpleNodeIdentInfoRequest.mti()) {
                // check for temporary error
                if ( (msg.getCode() & 0x1000 ) == 0) {
                    // not a temporary error, assume a permanent error
                    System.out.println("Permanent error geting Simple Node Info for node "+msg.getSourceNodeID()+" code 0x"+Integer.toHexString(msg.getCode()).toUpperCase());
                    return;
                }
                // have to resend the SNII request
                connection.put(new SimpleNodeIdentInfoRequestMessage(node, msg.getSourceNodeID()), null);
            }
            if (msg.getMti() == MessageTypeIdentifier.ProtocolSupportInquiry.mti()) {
                // check for temporary error
                if ( (msg.getCode() & 0x1000 ) == 0) {
                    // not a temporary error, assume a permanent error
                    System.out.println("Permanent error geting Protocol Identification information for node "+msg.getSourceNodeID()+" code 0x"+Integer.toHexString(msg.getCode()).toUpperCase());
                    return;
                }
                // have to resend the PIP request
                connection.put(new ProtocolIdentificationRequestMessage(node, msg.getSourceNodeID()), null);
            }
        }
        
        java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
        public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {pcs.addPropertyChangeListener(l);}
        public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {pcs.removePropertyChangeListener(l);}
    }
}
