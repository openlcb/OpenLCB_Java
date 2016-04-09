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
    private final ConnectionListener connectionListener;
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
        this.connectionListener = connectionListener;
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

    private void connect() {
        this.inputHub = new CanFrameHub();
        this.outputHub = new CanFrameHub();
        connectionListener.onConnectionPending();
        connectionListener.onStatusChange("Connecting...");
        BufferedReader reader;
        OutputStream outputStream;
        try {
            socket = new Socket(hostName, portNumber);
            socket.setTcpNoDelay(true);
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            connectionListener.onStatusChange("Connection failed: " + e.toString());
            connectionListener.onDisconnect();
            return;
        }
        input = new GridConnectInput(reader, inputHub);
        output = new GridConnectOutput(outputStream);
        outputHub.addEntry(output);

        // Creates the actual OpenLCB objects and wires up with the interface.
        canInterface = new CanInterface(nodeId, outputHub);
        inputHub.addEntry(canInterface.frameInput());
        canInterface.addStartListener(new Connection.ConnectionListener() {
            @Override
            public void connectionActive(Connection c) {
                connectionListener.onConnect();
            }
        });
        lastConnection = this;
    }

    public void shutdown() {
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
