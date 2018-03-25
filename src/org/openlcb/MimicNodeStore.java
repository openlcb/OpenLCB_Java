package org.openlcb;

import java.util.HashMap;
import java.util.Collection;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

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
    public static final String CLEAR_ALL_NODES = "ClearAllNodes";
    private final static Logger logger = Logger.getLogger(MimicNodeStore.class.getName());

    public MimicNodeStore(Connection connection, NodeID node) {
        this.connection = connection;
        this.node = node;
        timer = new Timer("OpenLCB Mimic Node Store Timer");
    }

    public void dispose(){
       // cancel the timer.
       timer.cancel();
       timer=null;
    }
    
    Connection connection;
    NodeID node;
    private Timer timer;
    
    public Collection<NodeMemo> getNodeMemos() {
        return map.values();
    } 
    
    public void put(Message msg, Connection sender) {
        NodeMemo memo = addNode(msg.getSourceNodeID());
        // check for necessary updates in specific node
        memo.put(msg, sender);
    }

    /**
     * Resets the node store object by clearing all members, and sending out a new message to the bus to validate all nodes. Will cause a callback for clearing all nodes, then an AddNode for all nodes that actually exist on the network.
     */
    public void refresh() {
        map.clear();
        pcs.firePropertyChange(CLEAR_ALL_NODES, null, null);
        connection.put(new VerifyNodeIDNumberMessage(node), this);
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
     * @param id    remote node ID to find
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

        Queue<Interaction> pendingInteractions = new ConcurrentLinkedDeque<>();
        Interaction currentInteraction = null;
        private TimerTask currentTask;

        public synchronized void startInteraction(final Interaction request) {
            if (currentInteraction == null) {
                doStart(request);
            } else {
                pendingInteractions.add(request);
            }
        }

        private synchronized void doStart(final Interaction request) {
            if (request == null) {
                return;
            }
            currentInteraction = request;
            request.sendRequest(connection);
            currentTask = new TimerTask() {
                @Override
                public void run() {
                    request.onTimeout();
                    tryCompleteInteraction(request);
                }
            };
            timer.schedule(currentTask, request.deadlineMsec);
        }

        public synchronized void tryCompleteInteraction(@Nullable Interaction request) {
            if (request == null) return;
            synchronized (request) {
                request.isComplete = true;
            }
            if (currentInteraction != request) return;
            completeInteraction(request);
        }

        public synchronized void completeInteraction(Interaction request) {
            if (request != currentInteraction) {
                throw new RuntimeException("Trying to complete an interaction that is not started.");
            }
            synchronized (request) {
                request.isComplete = true;
            }
            currentTask.cancel();
            currentInteraction = null;
            currentTask = null;
            if (pendingInteractions.isEmpty()) {
                return;
            }
            currentInteraction = pendingInteractions.remove();
            doStart(currentInteraction);
        }

        ProtocolIdentification pIdent = null;
        Interaction pipInteraction = null;
        public void handleProtocolIdentificationReply(ProtocolIdentificationReplyMessage msg, Connection sender){
            // accept assumes from mimic'd node
            pIdent = new ProtocolIdentification(node, msg);
            pcs.firePropertyChange(UPDATE_PROP_PROTOCOL, null, pIdent);
            tryCompleteInteraction(pipInteraction);
            pipInteraction = null;
        }  
        public ProtocolIdentification getProtocolIdentification() {
            if (pIdent == null) {
                if (id == null) {
                    throw new AssertionError("MimicNodeStore id == null");
                }
                pIdent = new ProtocolIdentification(node, id);
                pipInteraction = new Interaction() {
                    int numTriesLeft = 3;

                    @Override
                    void sendRequest(Connection downstream) {
                        pIdent.start(downstream);
                    }

                    @Override
                    NodeID dstNode() {
                        return node;
                    }

                    @Override
                    void onTimeout() {
                        synchronized (this) {
                            if (isComplete) return;
                        }
                        final Interaction request = this;
                        if (--numTriesLeft > 0) {
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    startInteraction(request);
                                }
                            }, 200);
                        }
                    }
                };
                startInteraction(pipInteraction);
            }
            return pIdent;
        }

        SimpleNodeIdent pSimpleNode = null;
        Interaction snipInteraction = null;
        public void handleSimpleNodeIdentInfoReply(SimpleNodeIdentInfoReplyMessage msg, Connection sender){
            // accept assumes from mimic'd node
            if (pSimpleNode == null) 
                pSimpleNode = new SimpleNodeIdent(msg);
            else
                pSimpleNode.addMsg(msg);
            if (pSimpleNode.contentComplete()) {
                tryCompleteInteraction(snipInteraction);
                snipInteraction = null;
            }
            pcs.firePropertyChange(UPDATE_PROP_SIMPLE_NODE_IDENT, null, pSimpleNode);
        }  
        public SimpleNodeIdent getSimpleNodeIdent() {
            if (pSimpleNode == null) {
                pSimpleNode = new SimpleNodeIdent(node, id);
                snipInteraction = new Interaction() {
                    int numTriesLeft = 3;

                    @Override
                    void sendRequest(Connection downstream) {
                        pSimpleNode.start(downstream);
                    }

                    @Override
                    NodeID dstNode() {
                        return node;
                    }

                    @Override
                    void onTimeout() {
                        synchronized (this) {
                            if (isComplete) return;
                        }
                        final Interaction request = this;
                        if (--numTriesLeft > 0) {
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    startInteraction(request);
                                }
                            }, 200);
                        }
                    }
                };
                startInteraction(snipInteraction);
            }
            return pSimpleNode;
        }

        public void handleOptionalIntRejected(OptionalIntRejectedMessage msg, Connection sender){
            if (msg.getMti() == MessageTypeIdentifier.SimpleNodeIdentInfoRequest.mti()) {
                // check for temporary error
                if ( (msg.getCode() & 0x1000 ) == 0) {
                    // not a temporary error, assume a permanent error
                    logger.log(Level.SEVERE, "Permanent error geting Simple Node Info for node {0} code 0x{1}", new Object[]{msg.getSourceNodeID(), Integer.toHexString(msg.getCode()).toUpperCase()});
                    return;
                }
                // have to resend the SNII request
                connection.put(new SimpleNodeIdentInfoRequestMessage(node, msg.getSourceNodeID()), null);
            }
            if (msg.getMti() == MessageTypeIdentifier.ProtocolSupportInquiry.mti()) {
                // check for temporary error
                if ( (msg.getCode() & 0x1000 ) == 0) {
                    // not a temporary error, assume a permanent error
                    logger.log(Level.SEVERE, "Permanent error geting Protocol Identification information for node {0} code 0x{1}", new Object[]{msg.getSourceNodeID(), Integer.toHexString(msg.getCode()).toUpperCase()});
                    return;
                }
                // have to resend the PIP request
                connection.put(new ProtocolIdentificationRequestMessage(node, msg.getSourceNodeID
                        ()), null);
            }
        }

        @Override
        public void handleInitializationComplete(InitializationCompleteMessage msg, Connection sender) {
            if (!msg.getSourceNodeID().equals(id)) {
                return;
            }
            int timeoutMsec = (int) (100 + Math.random() * 200);
            Interaction fakeInteraction = new Interaction() {
                @Override
                void sendRequest(Connection downstream) {
                    // do nothing
                }

                @Override
                NodeID dstNode() {
                    return node;
                }

                @Override
                void onTimeout() {
                    // do nothing; let the next interaction begin
                }
            };
            fakeInteraction.deadlineMsec = timeoutMsec;
            startInteraction(fakeInteraction);
            if (pSimpleNode != null) {
                pSimpleNode = null;
                getSimpleNodeIdent();
            }
            if (pIdent != null) {
                pIdent = null;
                getProtocolIdentification();
            }
        }

        java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
        public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {pcs.addPropertyChangeListener(l);}
        public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {pcs.removePropertyChangeListener(l);}
    }

}
