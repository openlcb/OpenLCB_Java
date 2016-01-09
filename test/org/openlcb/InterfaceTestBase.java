package org.openlcb;

import junit.framework.TestCase;

import org.openlcb.can.AliasMap;
import org.openlcb.can.CanFrame;
import org.openlcb.can.GridConnect;
import org.openlcb.can.MessageBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by bracz on 1/9/16.
 */
public abstract class InterfaceTestBase extends TestCase {

    protected FakeOlcbInterface iface = new FakeOlcbInterface();
    protected AliasMap aliasMap = new AliasMap();
    private Queue<Message> pendingMessages = new LinkedList<>();

    protected List<Message> outgoingMessages() {
        return iface.fakeOutputConnection.history;
    }

    /** Sends one or more OpenLCB message, as represented by the given CAN frames, to the
     * interface's inbound port. This represents traffic that a far away node is sending. The
     * frame should be specified in the GridConnect protocol.
     * @param frames is one or more CAN frames in the GridConnect protocol format.
     *  */
    protected void sendFrame(String frames) {
        if (aliasMap.getAlias(iface.getNodeId()) < 0) {
            aliasMap.insert(0x333, iface.getNodeId());
        }
        List<CanFrame> parsedFrames = GridConnect.parse(frames);
        MessageBuilder d = new MessageBuilder(aliasMap);
        for (CanFrame f : parsedFrames) {
            List<Message> l = d.processFrame(f);
            if (l != null) {
                for (Message m : l) {
                    iface.getInputConnection().put(m, null);
                }
            }
        }
    }

    /** Moves all outgoing messages to the pending messages queue. */
    private void consumeMessages() {
        pendingMessages.addAll(outgoingMessages());
        outgoingMessages().clear();
    }

    /** Expects that the next outgoing message (not yet matched with an expectation) is the given
     * CAN frame.
     * @param frame GridConnect-formatted CAN frame.
     */
    protected void expectFrame(String expectedFrame) {
        if (aliasMap.getAlias(iface.getNodeId()) < 0) {
            aliasMap.insert(0x333, iface.getNodeId());
        }
        consumeMessages();
        MessageBuilder d = new MessageBuilder(aliasMap);
        List<? extends CanFrame> actualFrames = d.processMessage(pendingMessages.remove());
        StringBuilder b = new StringBuilder();
        for (CanFrame f : actualFrames) {
            b.append(GridConnect.format(f));
        }
        assertEquals(expectedFrame, b.toString());
    }

    /** Expects that there are no unconsumed outgoing messages. */
    protected void expectNoFrames() {
        consumeMessages();
        assertEquals(0, pendingMessages.size());
    }

    protected void sendFrameAndExpectResult(String send, String expect) {
        sendFrame(send);
        expectFrame(expect);
        expectNoFrames();
    }
}
