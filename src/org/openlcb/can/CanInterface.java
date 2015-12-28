package org.openlcb.can;

import org.openlcb.Connection;
import org.openlcb.Connection.ConnectionListener;
import org.openlcb.Message;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * CanInterface collects all objects necessary to operate a standards-compliant node that connects
 * via CAN-bus.
 *
 * Created by bracz on 12/27/15.
 */
public class CanInterface {
    /// Keeps tracks of aliases.
    private final AliasMap aliasMap;
    /// State machines for frame reassembly.
    private final MessageBuilder messageBuilder;
    /// CAN adapter to send outgoing messages to.
    private final CanFrameListener frameOutput;
    /// Converts incoming farmes to messages and forwards them to the olcbInterface.
    private final FrameParser frameInput;
    /// All high-level (i.e. mesage level and above) Olcb objects.
    private final OlcbInterface olcbInterface;
    /// Converts outgoing messages to frame sequences and sends to the network.
    private final FrameRenderer frameRenderer;
    /// Objects waiting for startup.
    private final List<ConnectionListener> listeners = new ArrayList<>();
    private final NodeID nodeId;

    boolean initialized = false;

    public CanInterface(NodeID interfaceId, CanFrameListener frameOutput) {
        this.frameOutput = frameOutput;
        this.frameRenderer = new FrameRenderer();
        this.nodeId = interfaceId;

        // Creates high-level OpenLCB interface.
        olcbInterface = new OlcbInterface(frameRenderer);

        // Creates CAN-level OpenLCB objects.
        aliasMap = new AliasMap();
        messageBuilder = new MessageBuilder(aliasMap);

        this.frameInput = new FrameParser();
        new Thread(new Runnable() {
            @Override
            public void run() {
                initialize();
            }
        }).start();
    }

    public CanFrameListener frameInput() { return frameInput; }

    public void addStartListener(ConnectionListener l) {
        synchronized (listeners) {
            if (!initialized) {
                listeners.add(l);
                return;
            }
        }
        // Must ensure to call back outside of the lock.
        l.connectionActive(frameRenderer);
    }

    public void initialize() {
        // Do initialization tasks here.

        // Notify all listeners waiting for init. Call them outside of the lock.
        List<ConnectionListener> listeners_copy = new ArrayList<>();
        synchronized (listeners) {
            initialized = true;
            listeners_copy.addAll(listeners);
            listeners.clear();
        }
        for (ConnectionListener l : listeners_copy) {
            l.connectionActive(frameRenderer);
        }
    }

    class FrameParser implements CanFrameListener {
        @Override
        public void send(CanFrame frame) {
            List<Message> l = messageBuilder.processFrame(frame);
            for (Message m : l) {
                olcbInterface.inputConnection().put(m, null);
            }
        }
    }



    class FrameRenderer implements Connection {
        @Override
        public void put(Message msg, Connection sender) {
            List<OpenLcbCanFrame> l = messageBuilder.processMessage(msg);
            for (CanFrame f : l) {
                frameOutput.send(f);
            }
        }

        @Override
        public void registerStartNotification(ConnectionListener c) {
            addStartListener(c);
        }
    }
}
