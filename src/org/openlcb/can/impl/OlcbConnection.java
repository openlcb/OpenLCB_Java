package org.openlcb.can.impl;


import org.openlcb.Connection;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.can.CanFrame;
import org.openlcb.can.CanFrameListener;
import org.openlcb.can.CanInterface;
import org.openlcb.cdi.impl.ConfigRepresentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bracz on 12/23/15.
 */
public class OlcbConnection {
    // @TODO: 3/31/16 balazs.racz: THis should be migrated into a singleton class like
    // ConnectionManager.
    public static OlcbConnection lastConnection = null;
    private final NodeID nodeId;
    private final ListenerProxy listenerProxy;
    private final Map<NodeID, ConfigRepresentation> nodeConfigs = new HashMap<>();
    private String hostName;
    private int portNumber;
    private GridConnectInput input;
    private GridConnectOutput output;
    /// Hub for received frames (from network).
    private CanFrameHub inputHub;
    /// Hub for sent frames (to network).
    private CanFrameHub outputHub;
    private CanInterface canInterface;
    private Socket socket;

    public OlcbConnection(NodeID nodeId, String
            hostName, int portNumber, ConnectionListener connectionListener) {
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.listenerProxy = new ListenerProxy();
        this.listenerProxy.add(connectionListener);
        this.nodeId = nodeId;
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // ignore
                }
                connect();
            }
        }.start();
    }

    private Runnable mOnError = new Runnable() {
        @Override
        public void run() {
            synchronized(OlcbConnection.this) {
                if (outputHub != null) {
                    outputHub.removeEntry(output);
                }
                listenerProxy.onDisconnect();
                shutdown();
            }
        }
    };

    private synchronized void connect() {
        this.inputHub = new CanFrameHub();
        this.outputHub = new CanFrameHub();
        listenerProxy.onConnectionPending();
        listenerProxy.onStatusChange("Connecting...");
        BufferedReader reader;
        OutputStream outputStream;
        try {
            socket = new Socket(hostName, portNumber);
            socket.setTcpNoDelay(true);
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            listenerProxy.onStatusChange("Connection failed: " + e.toString());
            listenerProxy.onDisconnect();
            return;
        }
        input = new GridConnectInput(reader, inputHub, mOnError);
        output = new GridConnectOutput(outputStream, mOnError);
        outputHub.addEntry(output);

        // Creates the actual OpenLCB objects and wires up with the interface.
        canInterface = new CanInterface(nodeId, outputHub);
        inputHub.addEntry(canInterface.frameInput());
        canInterface.addStartListener(new Connection.ConnectionListener() {
            @Override
            public void connectionActive(Connection c) {
                listenerProxy.onConnect();
            }
        });
        lastConnection = this;
    }

    public synchronized void shutdown() {
        if (inputHub != null) {
            inputHub.clear();
            inputHub = null;
        }
        if (outputHub != null) {
            outputHub.clear();
            outputHub = null;
        }
        input = null;
        output = null;

        if (socket == null) return;

        // Should cause input thread to die.
        try {
            socket.close();
        } catch (IOException e) {
            // Ignore.
        }

        socket = null;
    }

    public void addConnectionListener(ConnectionListener l) {
        listenerProxy.add(l);
    }
    /**
     * Removes a listener. It is okay to call this from inside a connectionlistener callback.
     * @param l listener to remove
     */
    public void removeConnectionListener(ConnectionListener l) {
        listenerProxy.remove(l);
    }

    /**
     * Creates a new or returns a cached CDI representation for the given node.
     */
    public synchronized ConfigRepresentation getConfigForNode(NodeID remoteNode) {
        if (nodeConfigs.containsKey(remoteNode)) {
            return nodeConfigs.get(remoteNode);
        }
        ConfigRepresentation rep = new ConfigRepresentation(getInterface(), remoteNode);
        nodeConfigs.put(remoteNode, rep);
        return rep;
    }

    /**
     * @return the CAN frame hub processing the incoming messages (from the network; sent by
     * other nodes).
     * <p/>
     * This can be used for two purposes:
     * - get a copy of all frames arriving from the network;
     * - inject fake messages as if they were coming from the network (not super useful).
     */
    public CanFrameHub getInputHub() {
        return inputHub;
    }

    /**
     * @return the CAN frame hub processing outgoing messages (to the network; originating from
     * this node).
     * <p/>
     * This can be used for two purposes:
     * - send packets to the network
     * - get a copy of (aka sniff) all outgoing packets before they are sent.
     */
    public CanFrameHub getOutputHub() {
        return outputHub;
    }

    public OlcbInterface getInterface() {
        return canInterface.getInterface();
    }

    public interface ConnectionListener {
        void onConnect();

        void onDisconnect();

        void onStatusChange(String status);

        void onConnectionPending();
    }

    /**
     * Simple registry for connection listeners; proxies all calls to every single entry.
     */
    private class ListenerProxy implements ConnectionListener {
        private List<ConnectionListener> entries = new ArrayList<>(3);

        public synchronized void add(ConnectionListener l) {
            entries.add(l);
        }

        /**
         * Removes a listener. It is okay to call this from inside a connectionlistener callback.
         * @param l listener to remove
         */
        public synchronized void remove(ConnectionListener l) {
            entries.remove(l);
        }

        private synchronized ConnectionListener[] getEntries() {
            ConnectionListener[] ar = new ConnectionListener[entries.size()];
            return entries.toArray(ar);
        }
        @Override
        public void onConnect() {
            ConnectionListener[] ar = getEntries();
            for (ConnectionListener l : ar) {
                l.onConnect();
            }
        }

        @Override
        public void onDisconnect() {
            ConnectionListener[] ar = getEntries();
            for (ConnectionListener l : ar) {
                l.onDisconnect();
            }
        }

        @Override
        public void onStatusChange(String status) {
            ConnectionListener[] ar = getEntries();
            for (ConnectionListener l : ar) {
                l.onStatusChange(status);
            }
        }

        @Override
        public void onConnectionPending() {
            ConnectionListener[] ar = getEntries();
            for (ConnectionListener l : ar) {
                l.onConnectionPending();
            }
        }
    }

    public class CanFrameHub implements CanFrameListener {
        private List<CanFrameListener> entries = new ArrayList<>();

        public void addEntry(CanFrameListener l) {
            addEntry(l, false);
        }

        public void addEntry(CanFrameListener l, boolean atFront) {
            if (atFront) {
                entries.add(0, l);
            } else {
                entries.add(l);
            }
        }

        public void removeEntry(CanFrameListener l) {
            entries.remove(l);
        }

        public void clear() {
            entries.clear();
        }

        @Override
        public void send(CanFrame frame) {
            for (CanFrameListener l : entries) {
                l.send(frame);
            }
        }
    }

}
