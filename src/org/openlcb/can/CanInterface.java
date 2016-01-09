package org.openlcb.can;

import org.openlcb.Connection;
import org.openlcb.Connection.ConnectionListener;
import org.openlcb.Message;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

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
    private final NIDaAlgorithm aliasWatcher;

    boolean initialized = false;

    public CanInterface(NodeID interfaceId, CanFrameListener frameOutput) {
        this.frameOutput = frameOutput;
        this.frameRenderer = new FrameRenderer();
        this.nodeId = interfaceId;

        // Creates high-level OpenLCB interface.
        olcbInterface = new OlcbInterface(nodeId, frameRenderer);

        // Creates CAN-level OpenLCB objects.
        aliasMap = new AliasMap();
        messageBuilder = new MessageBuilder(aliasMap);
        aliasWatcher = new NIDaAlgorithm(interfaceId, frameOutput);

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
        final Semaphore sema = new Semaphore(1, true);
        sema.acquireUninterruptibly();

        aliasWatcher.start(new Runnable() {
            @Override
            public void run() {
                sema.release();
            }
        });
        // Waits for alias allocation to complete.
        sema.acquireUninterruptibly();
        // Stores local node alias.
        aliasMap.insert(aliasWatcher.getNIDa(), nodeId);
        /// @TODO(balazs.racz): If the alias changes, we need to update the local alias map.

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

    public OlcbInterface getInterface() {
        return olcbInterface;
    }

    class FrameParser implements CanFrameListener {
        @Override
        public void send(CanFrame frame) {
            aliasWatcher.send(frame);
            List<Message> l = messageBuilder.processFrame(frame);
            if (l == null) return;
            for (Message m : l) {
                olcbInterface.getInputConnection().put(m, null);
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
