package org.openlcb;

import org.openlcb.implementations.DatagramMeteringBuffer;
import org.openlcb.implementations.DatagramService;
import org.openlcb.implementations.MemoryConfigurationService;
import org.openlcb.protocols.VerifyNodeIdHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Collects all objects necessary to run an OpenLCB standards-compatible interface.
 * <p/>
 * Created by bracz on 12/27/15.
 */
public class OlcbInterface {

    /// Object for sending messages to the network.
    protected final Connection outputConnection;
    private final OutputConnectionSniffer wrappedOutputConnection = new OutputConnectionSniffer();
    /// Object for taking incoming messages and forwarding them to the necessary handlers.
    private final MessageDispatcher inputConnection;
    private final NodeID nodeId;

    // These are protocol support libraries for various OpenLCB protocols.

    // Client library for SNIP, PIP etc protocols.
    private final MimicNodeStore nodeStore;
    // Outgoing connection wrapper for datagrams that ensures that we only send one datagram at a
    // time to one destination node.
    private final DatagramMeteringBuffer dmb;
    // Client (and server) for datagrams.
    private final DatagramService dcs;
    // Client for memory configuration requests.
    private final MemoryConfigurationService mcs;

    /**
     * Creates the message-level interface.
     * @param nodeId_ is the node ID for the node on this interface. Will send out a node
     *                initialized ready with this node ID.
     * @param outputConnection_ implements the hardware interface for sending messages to the
     *                          network. Usually this is an internal object of the CanInterface.
     */
    public OlcbInterface(NodeID nodeId_, Connection outputConnection_) {
        nodeId = nodeId_;
        this.outputConnection = outputConnection_;
        inputConnection = new MessageDispatcher();

        nodeStore = new MimicNodeStore(getOutputConnection(), nodeId);
        dmb = new DatagramMeteringBuffer(getOutputConnection());
        dcs = new DatagramService(nodeId, dmb);
        mcs = new MemoryConfigurationService(nodeId, dcs);
        inputConnection.registerMessageListener(nodeStore);
        inputConnection.registerMessageListener(dmb.connectionForRepliesFromDownstream());
        inputConnection.registerMessageListener(dcs);
        new VerifyNodeIdHandler(nodeId, this); // will register itself.

        outputConnection.registerStartNotification(new Connection.ConnectionListener() {
            @Override
            public void connectionActive(Connection c) {
                Message m = new InitializationCompleteMessage(nodeId);
                outputConnection.put(m, getInputConnection());
            }
        });
    }

    /** Accessor for the outside interface for arriving inbound messages.
     * @return the Connection that the incoming messages (from the network) have to be forwarded
     * to. */
    public Connection getInputConnection() {
        return inputConnection;
    }
    /** Accessor for client libraries to send messages out. */
    public Connection getOutputConnection() { return wrappedOutputConnection; }

    public NodeID getNodeId() { return nodeId; }

    public MimicNodeStore getNodeStore() { return nodeStore; }

    public DatagramService getDatagramService() { return dcs; }

    public MemoryConfigurationService getMemoryConfigurationService() { return mcs; }

    public void registerMessageListener(Connection c) {
        inputConnection.registerMessageListener(c);
    }
    public void unRegisterMessageListener(Connection c) {
        inputConnection.unRegisterMessageListener(c);
    }

    class MessageDispatcher extends AbstractConnection {
        // This is not the ideal container for add/remove, but keeping the ordering of
        // registrations is useful in ensuring that the system components receive the messages
        // earlier as the later-registered user components.
        private List<Connection> listeners = new ArrayList<>();
        public void registerMessageListener(Connection c) {
            listeners.add(c);
        }
        public void unRegisterMessageListener(Connection c) {
            listeners.remove(c);
        }

        @Override
        public void put(Message msg, Connection sender) {
            for (Connection c : listeners) {
                c.put(msg, sender);
            }
        }
    }

    /**
    * Performs local feedback of addressed and global messages. This class is on the critical
    * path to sending messages.
    */
    class OutputConnectionSniffer implements Connection {
        @Override
        public void put(Message msg, Connection sender) {
            // For addressed messages we check if the target is local or remote.
            if (msg instanceof AddressedMessage) {
                AddressedMessage amsg = (AddressedMessage) msg;
                if (amsg.destNodeID.equals(nodeId)) {
                    // Addressed to local host. Skip sending to the network.
                    inputConnection.put(msg, sender);
                    return;
                }
                // The MimicNodeStore needs to know about all messages sent.
                nodeStore.put(msg, sender);
            } else {
                // For global messages, we always send a copy of the message locally.
                inputConnection.put(msg, sender);
            }
            outputConnection.put(msg, sender);
        }

        @Override
        public void registerStartNotification(ConnectionListener c) {
            outputConnection.registerStartNotification(c);
        }
    }

}
