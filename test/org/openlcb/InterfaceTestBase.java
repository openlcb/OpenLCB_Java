package org.openlcb;

import junit.framework.AssertionFailedError;
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
 * Test helper class that instantiates an OlcbInterface and allows making expectations on what is
 * sent to the bus, as well as allows injecting response messages from the bus.
 *
 * Created by bracz on 1/9/16.
 */
public abstract class InterfaceTestBase extends TestCase {

    protected FakeOlcbInterface iface = new FakeOlcbInterface();
    protected AliasMap aliasMap = new AliasMap();
    private Queue<Message> pendingMessages = new LinkedList<>();

    public InterfaceTestBase(String s) {
        super(s);
        expectInit();
    }

    public InterfaceTestBase() {
        expectInit();
    }

    private void expectInit() {
        expectMessage(new InitializationCompleteMessage(iface.getNodeId()));
    }

    @Override
    protected void tearDown() throws Exception {
        expectNoMessages();
        super.tearDown();
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

    /** Sends an OpenLCB message to the interface's inbound port. This represents traffic that a
     * far away node is sending.
     * @param msg inbound message from a far node
     */
    protected void sendMessage(Message msg) {
        iface.getInputConnection().put(msg, null);
    }

    /** Moves all outgoing messages to the pending messages queue. */
    private void consumeMessages() {
        iface.flushSendQueue();
        iface.fakeOutputConnection.transferAll(pendingMessages);
    }

    /** Expects that the next outgoing message (not yet matched with an expectation) is the given
     * CAN frame.
     * @param expectedFrame GridConnect-formatted CAN frame.
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

    /** Expects that the next outgoing message (not yet matched with an expectation) is the given
     * message.
     * @param expectedMessage message that should have been sent to the bus from the local stack.
     */
    protected void expectMessage(Message expectedMessage) {
        consumeMessages();
        Message m = pendingMessages.remove();
        assertEquals(expectedMessage, m);
    }

    protected void expectMessageAndNoMore(Message expectedMessage) {
        expectMessage(expectedMessage);
        expectNoMessages();
    }

    /** Expects that there are no unconsumed outgoing messages. */
    protected void expectNoFrames() {
        consumeMessages();
        assertEquals(0, pendingMessages.size());
    }

    /** Expects that there are no unconsumed outgoing messages. */
    protected void expectNoMessages() {
        consumeMessages();
        assertEquals("no more outgoing messages", new ArrayList<Message>(), pendingMessages);
        //expectNoFrames();
    }

    protected void sendFrameAndExpectResult(String send, String expect) {
        sendFrame(send);
        expectFrame(expect);
        expectNoFrames();
    }

    protected void sendMessageAndExpectResult(Message send, Message expect) {
        sendMessage(send);
        expectMessage(expect);
    }
}
