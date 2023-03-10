package org.openlcb;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Store containing mimic proxies for nodes on external connections
 * <p>
 * Provides a Connection for incoming Messages.
 *
 * @author  Bob Jacobsen   Copyright 2011
 */
public class MimicNodeStore extends AbstractConnection {
    public static final String ADD_PROP_NODE = "AddNode";
    public static final String CLEAR_ALL_NODES = "ClearAllNodes";
    private final static Logger logger = Logger.getLogger(MimicNodeStore.class.getName());
    
    private static class MimicNodeStoreTimer {
        private Timer timer;
        
        private synchronized void init() {
            if (timer == null) {
                // only initialize timer if it is not initialized or has been canceled before
                timer = new Timer("OpenLCB Mimic Node Store Timer");
            }
        }

        private synchronized void schedule(final TimerTask t, final int delay) {
            if (timer != null) {
                // only schedule task if timer is initialized and not canceled
                timer.schedule(t,delay);
            }
        }
        
        private synchronized void cancel() {
            if (timer != null ) {
                // only cancel time if it is initialized and not canceled
                timer.cancel();
                timer = null;
            }
        }
    }

    public MimicNodeStore(Connection connection, NodeID node) {
        this.connection = connection;
        this.node = node;
        
        timer.init();
    }

    public void dispose() {
        // cancel the timer.
        timer.cancel();
    }

    void scheduleTask(TimerTask t, int delay) {
        timer.schedule(t,delay);
    }
    
    Connection connection;
    NodeID node;
    private static MimicNodeStoreTimer timer = new MimicNodeStoreTimer();
    
    public Collection<NodeMemo> getNodeMemos() {
        return map.values();
    } 
    
    @Override
    public void put(Message msg, Connection sender) {
        if (msg.getSourceNodeID() == null) {
            // We don't really know where this message came from; this is usually due to the
            // alias map not knowing about an alias. We cannot add this node now, because null
            // keys are problematic in the node store.
            return;
        }
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
     * 
     * @param id remote node ID to find
     * @return NodeMemo already known, but note you have to register listeners before calling
     *         in any case
     */
    public NodeMemo findNode(NodeID id) {
        NodeMemo memo = map.get(id);
        if (memo != null) {
            return memo;
        }
        
        // create and send targeted request
        connection.put(new VerifyNodeIDNumberMessage(node, id), null);
        return null;
    }
    
    public SimpleNodeIdent getSimpleNodeIdent(NodeID dest) {
        NodeMemo memo = map.get(dest);
        return (memo == null) ? null : memo.getSimpleNodeIdent();
    }
    
    public ProtocolIdentification getProtocolIdentification(NodeID dest) {
        NodeMemo memo = map.get(dest);
        return (memo == null) ? null : memo.getProtocolIdentification();
    }
    
    HashMap<NodeID, NodeMemo> map = new java.util.HashMap<NodeID, NodeMemo>();

    PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public class NodeMemo extends MessageDecoder {
        public static final String UPDATE_PROP_SIMPLE_NODE_IDENT = "updateSimpleNodeIdent";
        public static final String UPDATE_PROP_PROTOCOL = "updateProtocol";
        NodeID id;
        
        public NodeMemo(NodeID id) {
            this.id = id;
        }
        
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
            scheduleTask(currentTask, request.deadlineMsec);
        }

        public synchronized void tryCompleteInteraction(@Nullable Interaction request) {
            if (request == null) {
                return;
            }
            synchronized (request) {
                request.isComplete = true;
            }
            if (currentInteraction != request) {
                return;
            }
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
        
        @Override
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
                    int numTriesLeft = 1;

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
                            if (isComplete) {
                                return;
                            }
                        }
                        final Interaction request = this;
                        if (--numTriesLeft > 0) {
                            scheduleTask(new TimerTask() {
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
        
        @Override
        public void handleSimpleNodeIdentInfoReply(
                SimpleNodeIdentInfoReplyMessage msg, Connection sender){
            // accept assumes from mimic'd node
            if (pSimpleNode == null) {
                pSimpleNode = new SimpleNodeIdent(msg);
            } else {
                pSimpleNode.addMsg(msg);
            }
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
                    int numTriesLeft = 1;

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
                            if (isComplete) {
                                return;
                            }
                        }
                        final Interaction request = this;
                        if (--numTriesLeft > 0) {
                            scheduleTask(new TimerTask() {
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

        @Override
        public void handleOptionalIntRejected(OptionalIntRejectedMessage msg, Connection sender){
            if (msg.getRejectMTI() == MessageTypeIdentifier.SimpleNodeIdentInfoRequest.mti()) {
                // check for temporary error
                if ((msg.getCode() & 0x1000) == 0) {
                    // not a temporary error, assume a permanent error
                    String logmsg = "Permanent error geting Simple Node Info "
                            + "for node {0} code 0x{1}";
                    Object[] logparam = new Object[] {
                                msg.getSourceNodeID(),
                                Integer.toHexString(msg.getCode()).toUpperCase()
                            };
                    logger.log(Level.SEVERE, logmsg, logparam);
                    return;
                }
                // have to resend the SNII request
                connection.put(new SimpleNodeIdentInfoRequestMessage(
                        node, msg.getSourceNodeID()), null);
            }
            if (msg.getRejectMTI() == MessageTypeIdentifier.ProtocolSupportInquiry.mti()) {
                // check for temporary error
                if ((msg.getCode() & 0x1000) == 0) {
                    // not a temporary error, assume a permanent error
                    String logmsg = "Permanent error geting Protocol Identification information "
                            + "for node {0} code 0x{1}";
                    Object[] logparam = new Object[] {
                                msg.getSourceNodeID(),
                                Integer.toHexString(msg.getCode()).toUpperCase()
                            };
                    logger.log(Level.SEVERE, logmsg, logparam);
                    return;
                }
                // have to resend the PIP request
                connection.put(new ProtocolIdentificationRequestMessage(
                        node, msg.getSourceNodeID()), null);
            }
        }

        @Override
        public void handleInitializationComplete(
                InitializationCompleteMessage msg, Connection sender) {
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

        PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }
        
        public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }
    }
}
