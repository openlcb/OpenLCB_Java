package org.openlcb;

import org.openlcb.cdi.impl.ConfigRepresentation;
import org.openlcb.implementations.DatagramMeteringBuffer;
import org.openlcb.implementations.DatagramService;
import org.openlcb.implementations.EventTable;
import org.openlcb.implementations.MemoryConfigurationService;
import org.openlcb.protocols.VerifyNodeIdHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;

/**
 * Collects all objects necessary to run an OpenLCB standards-compatible interface.
 * <p>
 * Created by bracz on 12/27/15.
 */
public class OlcbInterface {
    private final static Logger log = Logger.getLogger(OlcbInterface.class.getName());
    private final Timer timer = new Timer();

    /// Object for sending messages to the network.
    protected final Connection internalOutputConnection;
    /// Object we return to the customer when they ask for the output connection
    protected final Connection outputConnection;

    private final OutputConnectionSniffer wrappedOutputConnection;
    private final QueuedOutputConnection queuedOutputConnection;
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
    private MemoryConfigurationService mcs;

    // CDIs for the nodes
    private final Map<NodeID, ConfigRepresentation> nodeConfigs = new HashMap<>();
    // Event Table is a helper for user interfaces to register and retrieve user names for
    // events. By default this is null, initialized lazily when needed only.
    private EventTable eventTable = null;


    private ThreadPoolExecutor threadPool = null;
    final static int minThreads = 10;
    final static int maxThreads = 100;
    final static long threadTimeout = 10; // allowed idle time for threads, in seconds.



    /**
     * Creates the message-level interface.
     *
     * @param nodeId_           is the node ID for the node on this interface. Will send out a node
     *                          initialized ready with this node ID.
     * @param outputConnection_ implements the hardware interface for sending messages to the
     *                          network. Usually this is an internal object of the CanInterface.
     *
     * @deprecated since OlcbLibrary version 0.18.  Use {@link #OlcbInterface(NodeID, Connection, ThreadPoolExecutor)} instead.
     */
    @Deprecated
    public OlcbInterface(NodeID nodeId_, Connection outputConnection_) {
          this(nodeId_,outputConnection_, 
               new ThreadPoolExecutor(minThreads,maxThreads,threadTimeout,
                                      TimeUnit.SECONDS,
                                      new LinkedBlockingQueue<Runnable>(),
                                      new OlcbThreadFactory()));
          threadPool.allowCoreThreadTimeOut(true);
    }

    /**
     * Creates the message-level interface.
     *
     * @param nodeId_           is the node ID for the node on this interface. Will send out a node
     *                          initialized ready with this node ID.
     * @param outputConnection_ implements the hardware interface for sending messages to the
     *                          network. Usually this is an internal object of the CanInterface.
     * @param tpe ThreadPoolExecutor for the interface.
     */
    public OlcbInterface(NodeID nodeId_, Connection outputConnection_,ThreadPoolExecutor tpe) {
        threadPool = tpe;
        nodeId = nodeId_;
        this.internalOutputConnection = outputConnection_;
        this.wrappedOutputConnection = new OutputConnectionSniffer(internalOutputConnection);
        this.queuedOutputConnection = new QueuedOutputConnection(this.wrappedOutputConnection);
        this.outputConnection = this.queuedOutputConnection;
        inputConnection = new MessageDispatcher();

        nodeStore = new MimicNodeStore(getOutputConnection(), nodeId);
        dmb = new DatagramMeteringBuffer(getOutputConnection(),threadPool);
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
                // Starts the output queue once we have the confirmation from the lower level that
                // the connection is ready and we have enqueued the initialization complete message.
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        queuedOutputConnection.run();
                    }
                });
           }
        });
    }

    /**
     * @return a shared Timer thread to be used by all components in this interface. Tasks scheduled on this timer are not allowed to block (as it's a shared timer thread).
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * Accessor for the outside interface for arriving inbound messages.
     *
     * @return the Connection that the incoming messages (from the network) have to be forwarded
     * to.
     */
    public Connection getInputConnection() {
        return inputConnection;
    }

    /**
     * Accessor for client libraries to send messages out.
     * @return the connection through which to send messages to the bus.
     */
    public Connection getOutputConnection() {
        return outputConnection;
    }

    public NodeID getNodeId() {
        return nodeId;
    }

    public MimicNodeStore getNodeStore() {
        return nodeStore;
    }

    public DatagramService getDatagramService() {
        return dcs;
    }

    public DatagramMeteringBuffer getDatagramMeteringBuffer() {
        return dmb;
    }

    public MemoryConfigurationService getMemoryConfigurationService() {
        return mcs;
    }

    /// Useful for testing.
    public void injectMemoryConfigurationService(MemoryConfigurationService s) {
        mcs = s;
    }

    public synchronized EventTable getEventTable() {
        if (eventTable == null) {
            eventTable = new EventTable();
        }
        return eventTable;
    }
    /**
     * Creates a new or returns a cached CDI representation for the given node.
     * @param remoteNode    target node (on the network)
     * @return the cached CDI representation for that node (may be newly created and thus empty)
     */
    public synchronized ConfigRepresentation getConfigForNode(NodeID remoteNode) {
        if (nodeConfigs.containsKey(remoteNode)) {
            return nodeConfigs.get(remoteNode);
        }
        ConfigRepresentation rep = new ConfigRepresentation(this, remoteNode);
        nodeConfigs.put(remoteNode, rep);
        return rep;
    }

    /**
     * Blocks the current thread until the outgoing messages are all sent out. Useful for testing.
     */
    public void flushSendQueue() {
        dmb.waitForSendQueue();
        queuedOutputConnection.waitForSendQueue();
    }

    public void registerMessageListener(Connection c) {
        inputConnection.registerMessageListener(c);
    }

    public void unRegisterMessageListener(Connection c) {
        inputConnection.unRegisterMessageListener(c);
    }

    /**
     * @return how many listeners are currently registered using registerMessageListener.
     */
    public int numMessageListeners() {
        return inputConnection.numListeners();
    }

    class MessageDispatcher extends AbstractConnection {
        // This is not the ideal container for add/remove, but keeping the ordering of
        // registrations is useful in ensuring that the system components receive the messages
        // earlier as the later-registered user components.
        private List<Connection> listeners = new ArrayList<>();
        private List<Connection> pendingListeners = new ArrayList<>();
        private List<Connection> unpendingListeners = new ArrayList<>();

        public synchronized void registerMessageListener(Connection c) {
            pendingListeners.add(c);
        }

        public synchronized void unRegisterMessageListener(Connection c) {
            unpendingListeners.add(c);
        }

        public synchronized int numListeners() {
            return listeners.size() + pendingListeners.size() - unpendingListeners.size();
        }

        @Override
        public synchronized void put(Message msg, Connection sender) {
            if (!pendingListeners.isEmpty() || !unpendingListeners.isEmpty()) {
                listeners.addAll(pendingListeners);
                pendingListeners.clear();
                listeners.removeAll(unpendingListeners);
                unpendingListeners.clear();
            }
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
        private final Connection realOutput;

        OutputConnectionSniffer(Connection realOutput) {
            this.realOutput = realOutput;
        }

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
            realOutput.put(msg, sender);
        }

        @Override
        public void registerStartNotification(ConnectionListener c) {
            realOutput.registerStartNotification(c);
        }
    }

    /**
     * cleanup local resources
     */
    public void dispose(){
        // shut down shared timer's thread.
        timer.cancel();
        // shut down the thread pool
        if(threadPool != null && !(threadPool.isShutdown())) {
           // modified from the javadoc for ExecutorService 
           threadPool.shutdown(); // Disable new tasks from being submitted
           try {
              // Wait a while for existing tasks to terminate
              if (!threadPool.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                 threadPool.shutdownNow(); // Cancel currently executing tasks
                 // Wait a while for tasks to respond to being cancelled
                 if (!threadPool.awaitTermination(100, TimeUnit.MILLISECONDS))
                     log.warning("Pool did not terminate");
              }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                threadPool.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }
        threadPool = null;
        dmb.dispose();
        mcs.dispose();
        nodeStore.dispose();
    }    

    /**
     * This class keeps an output connection operating using an internal queue. It keeps
     * messages in an internal thread-safe queue and sends them on a separate thread.
     * <p>
     * The caller must donate a thread to this connection by calling the run() method.
     */
    private class QueuedOutputConnection implements Connection {
        private final Connection realOutput;
        private final BlockingQueue<QEntry> outputQueue = new
                LinkedBlockingQueue<>();
        private int pendingCount = 0;

        QueuedOutputConnection(Connection realOutput) {
            this.realOutput = realOutput;
        }

        @Override
        public void put(Message msg, Connection sender) {
            synchronized(this) {
                pendingCount++;
            }
            outputQueue.add(new QEntry(msg, sender));
        }

        @Override
        public void registerStartNotification(ConnectionListener c) {
            internalOutputConnection.registerStartNotification(c);
        }

        public void waitForSendQueue() {
            while(true) {
                synchronized (this) {
                    if (pendingCount == 0) return;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        /**
         * Never returns.
         */
        private void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    QEntry m = outputQueue.take();
                    try {
                        realOutput.put(m.message, m.connection);
                    } catch (RejectedExecutionException ex) {
                        throw ex; // re-throw so the outer try will handle these.
                    } catch (Throwable e) {
                        log.warning("Exception while sending message: " + e.toString());
                        e.printStackTrace();
                    }
                    synchronized(this) {
                        pendingCount--;
                    }
                } catch (InterruptedException|RejectedExecutionException e) {
                    // thread must exit when interrupted or rejected.
                    return;
                }
            }
        }

        private class QEntry {
            Message message;
            Connection connection;
            QEntry(Message m, Connection c) {
                message = m;
                connection = c;
            }
        }
    }
}
