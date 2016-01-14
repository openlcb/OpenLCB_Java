package org.openlcb.implementations;

import org.openlcb.EventID;
import org.openlcb.NodeID;

import static org.mockito.Mockito.*;

/**
 * Created by bracz on 1/8/16.
 */
public class BitProducerConsumerTest extends org.openlcb.InterfaceTestBase {

    public static final EventID onEvent = new EventID(new byte[]{5, 4, 3, 2, 1, 0, 7, 8});
    public static final EventID offEvent = new EventID(new byte[]{5, 4, 3, 2, 1, 0, 7, 9});
    BitProducerConsumer pc;

    public void testHandleIdentifyConsumers() throws Exception {
        sendFrameAndExpectResult( //
                ":X198F4444N0504030201000709;",
                ":X194C7333N0504030201000709;");
        sendFrameAndExpectResult( //
                ":X198F4444N0504030201000708;",
                ":X194C7333N0504030201000708;");
        // Sets to on.
        sendFrame(":X195B4444N0504030201000708;");

        sendFrameAndExpectResult( //
                ":X198F4444N0504030201000709;",
                ":X194C5333N0504030201000709;");
        sendFrameAndExpectResult( //
                ":X198F4444N0504030201000708;",
                ":X194C4333N0504030201000708;");

        // Sets to off.
        sendFrame(":X195B4444N0504030201000709;");

        sendFrameAndExpectResult( //
                ":X198F4444N0504030201000709;",
                ":X194C4333N0504030201000709;");
        sendFrameAndExpectResult( //
                ":X198F4444N0504030201000708;",
                ":X194C5333N0504030201000708;");
    }

    public void testHandleIdentifyProducers() throws Exception {
        sendFrameAndExpectResult( //
                ":X19914444N0504030201000708;",
                ":X19547333N0504030201000708;");
        sendFrameAndExpectResult( //
                ":X19914444N0504030201000709;",
                ":X19547333N0504030201000709;");

        // Sets to on.
        sendFrame(":X195B4444N0504030201000708;");
        sendFrameAndExpectResult( //
                ":X19914444N0504030201000708;",
                ":X19544333N0504030201000708;");
        sendFrameAndExpectResult( //
                ":X19914444N0504030201000709;",
                ":X19545333N0504030201000709;");

        // Sets to off.
        sendFrame(":X195B4444N0504030201000709;");
        sendFrameAndExpectResult( //
                ":X19914444N0504030201000708;",
                ":X19545333N0504030201000708;");
        sendFrameAndExpectResult( //
                ":X19914444N0504030201000709;",
                ":X19544333N0504030201000709;");
    }

    public void testHandleProducerIdentified() throws Exception {
        helperInputSetClear(":X19544333N0504030201000708;", ":X19545333N0504030201000708;");
    }

    public void testHandleProducerIdentifiedOff() throws Exception {
        helperInputSetClear(":X19545333N0504030201000709;", ":X19544333N0504030201000709;");
    }

    public void testHandleProducerIdentifiedOnOff() throws Exception {
        helperInputSetClear(":X19544333N0504030201000708;", ":X19544333N0504030201000709;");
    }

    public void helperInputSetClear(String frameOn, String frameOff) {
        assertNull(pc.getValue());
        sendFrame(frameOn);
        assertNotNull(pc.getValue());
        assertTrue(pc.getValue().getLatestData());
        sendFrame(frameOff);
        assertFalse(pc.getValue().getLatestData());

        MockVersionedValueListener<Boolean> listener = new MockVersionedValueListener<>(pc
                .getValue());

        verifyNoMoreInteractions(listener.stub);
        reset(listener.stub);

        sendFrame(frameOn);
        assertTrue(pc.getValue().getLatestData());
        verify(listener.stub).update(true);
        verifyNoMoreInteractions(listener.stub);
        reset(listener.stub);

        // Sending the same thing again shall not update.
        sendFrame(frameOn);
        verifyNoMoreInteractions(listener.stub);
        reset(listener.stub);

        sendFrame(frameOff);
        verify(listener.stub).update(false);
        verifyNoMoreInteractions(listener.stub);
        reset(listener.stub);
        assertFalse(pc.getValue().getLatestData());
    }

    public void testHandleConsumerIdentified() throws Exception {
        helperInputSetClear(":X194C4333N0504030201000708;", ":X194C5333N0504030201000708;");
    }

    public void testHandleConsumerIdentifiedOff() throws Exception {
        helperInputSetClear(":X194C5333N0504030201000709;", ":X194C4333N0504030201000709;");
    }

    public void testHandleConsumerIdentifiedOnOff() throws Exception {
        helperInputSetClear(":X194C4333N0504030201000708;", ":X194C4333N0504030201000709;");
    }

    public void testHandleProducerConsumerEventReport() throws Exception {
        helperInputSetClear(":X195B4333N0504030201000708;", ":X195B4333N0504030201000709;");
    }

    public void testHandleIdentifyEventsUnknown() throws Exception {
        sendFrame(":X19968444N0333;");
        //sendFrame(":X19970444N;");
        expectFrame(":X19547333N0504030201000708;");
        expectFrame(":X19547333N0504030201000709;");
        expectFrame(":X194C7333N0504030201000708;");
        expectFrame(":X194C7333N0504030201000709;");
    }

    public void testHandleIdentifyEventsKnown() throws Exception {
        sendFrame(":X194C5333N0504030201000709;");
        sendFrame(":X19968444N0333;");

        expectFrame(":X19544333N0504030201000708;");
        expectFrame(":X19545333N0504030201000709;");
        expectFrame(":X194C4333N0504030201000708;");
        expectFrame(":X194C5333N0504030201000709;");

        sendFrame(":X194C4333N0504030201000709;");
        sendFrame(":X19968444N0333;");

        expectFrame(":X19545333N0504030201000708;");
        expectFrame(":X19544333N0504030201000709;");
        expectFrame(":X194C5333N0504030201000708;");
        expectFrame(":X194C4333N0504030201000709;");

        // Other target
        sendFrame(":X19968444N0444;");
        expectNoFrames();
    }

    public void testGenerateEvents() throws Exception {
        VersionedValue<Boolean> v = pc.getValue(false);
        sendFrameAndExpectResult( //
                ":X19914444N0504030201000708;",
                ":X19545333N0504030201000708;");

        expectNoFrames();

        v.set(false);
        expectNoFrames();

        v.set(true);
        expectFrame(":X195B4333N0504030201000708;");
        expectNoFrames();

        v.set(true);
        expectNoFrames();

        v.set(false);
        expectFrame(":X195B4333N0504030201000709;");
        expectNoFrames();
    }

    @Override
    protected void tearDown() throws Exception {
        expectNoFrames();
        super.tearDown();
    }

    @Override
    public void setUp() {
        iface.fakeOutputConnection.history.clear();
        aliasMap.insert(0x444, new NodeID(new byte[]{1,2,3,1,2,3}));
        pc = new BitProducerConsumer(iface, onEvent, offEvent);
        expectFrame(":X19547333N0504030201000708;");
        expectFrame(":X19547333N0504030201000709;");
        expectFrame(":X194C7333N0504030201000708;");
        expectFrame(":X194C7333N0504030201000709;");

        expectFrame(":X19547333N0504030201000708;");
        expectFrame(":X19914333N0504030201000708;");
        expectFrame(":X194C7333N0504030201000708;");
        expectFrame(":X198F4333N0504030201000708;");
        expectNoFrames();
    }
}