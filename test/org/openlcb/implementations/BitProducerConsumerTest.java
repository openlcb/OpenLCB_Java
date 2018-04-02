package org.openlcb.implementations;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.openlcb.EventID;
import org.openlcb.NodeID;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Created by bracz on 1/8/16.
 */

public class BitProducerConsumerTest extends org.openlcb.InterfaceTestBase {


    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    public static final EventID onEvent = new EventID(new byte[]{5, 4, 3, 2, 1, 0, 7, 8});
    public static final EventID offEvent = new EventID(new byte[]{5, 4, 3, 2, 1, 0, 7, 9});
    BitProducerConsumer pc;

    @Test
    public void testHandleIdentifyConsumers() throws Exception {
        createWithDefaults();
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

    @Test
    public void testHandleIdentifyProducers() throws Exception {
        createWithDefaults();
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

    @Test
    public void testHandleProducerIdentified() throws Exception {
        createWithDefaults();
        helperInputSetClear(":X19544333N0504030201000708;", ":X19545333N0504030201000708;");
    }

    @Test
    public void testHandleProducerIdentifiedOff() throws Exception {
        createWithDefaults();
        helperInputSetClear(":X19545333N0504030201000709;", ":X19544333N0504030201000709;");
    }

    @Test
    public void testHandleProducerIdentifiedOnOff() throws Exception {
        createWithDefaults();
        helperInputSetClear(":X19544333N0504030201000708;", ":X19544333N0504030201000709;");
    }

    public void helperInputSetClear(String frameOn, String frameOff) {
        //assertTrue(pc.isValueAtDefault());
        sendFrame(frameOff);
        Assert.assertFalse(pc.getValue().getLatestData());
        sendFrame(frameOn);
        Assert.assertFalse(pc.isValueAtDefault());
        Assert.assertTrue(pc.getValue().getLatestData());
        sendFrame(frameOff);
        Assert.assertFalse(pc.getValue().getLatestData());

        MockVersionedValueListener<Boolean> listener = new MockVersionedValueListener<>(pc
                .getValue());

        verifyNoMoreInteractions(listener.stub);
        reset(listener.stub);

        sendFrame(frameOn);
        Assert.assertTrue(pc.getValue().getLatestData());
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
        Assert.assertFalse(pc.getValue().getLatestData());
    }

    public void helperInputNoChange(String frameOn, String frameOff) {
        Boolean data = pc.getValue().getLatestData();
        boolean atDefault = pc.isValueAtDefault();

        sendFrame(frameOn);
        Assert.assertEquals(data, pc.getValue().getLatestData());
        Assert.assertEquals(atDefault, pc.isValueAtDefault());
        expectNoFrames();

        sendFrame(frameOff);
        Assert.assertEquals(data, pc.getValue().getLatestData());
        Assert.assertEquals(atDefault, pc.isValueAtDefault());
        expectNoFrames();

        sendFrame(frameOn);
        Assert.assertEquals(data, pc.getValue().getLatestData());
        Assert.assertEquals(atDefault, pc.isValueAtDefault());
        expectNoFrames();

        sendFrame(frameOff);
        Assert.assertEquals(data, pc.getValue().getLatestData());
        Assert.assertEquals(atDefault, pc.isValueAtDefault());
        expectNoFrames();
    }

    public void helperNotProducing() {
        VersionedValue<Boolean> v = pc.getValue();
        v.set(false);
        expectNoFrames();
        v.set(true);
        expectNoFrames();
        v.set(false);
        expectNoFrames();
    }

    void helperNotConsuming() {
        helperInputNoChange(":X195B4333N0504030201000708;",
                ":X195B4333N0504030201000709;");
    }

    @Test
    public void testHandleConsumerIdentified() throws Exception {
        createWithDefaults();
        helperInputSetClear(":X194C4333N0504030201000708;", ":X194C5333N0504030201000708;");
    }

    @Test
    public void testHandleConsumerIdentifiedOff() throws Exception {
        createWithDefaults();
        helperInputSetClear(":X194C5333N0504030201000709;", ":X194C4333N0504030201000709;");
    }

    @Test
    public void testHandleConsumerIdentifiedOnOff() throws Exception {
        createWithDefaults();
        helperInputSetClear(":X194C4333N0504030201000708;", ":X194C4333N0504030201000709;");
    }

    @Test
    public void testHandleProducerConsumerEventReport() throws Exception {
        createWithDefaults();
        helperInputSetClear(":X195B4333N0504030201000708;", ":X195B4333N0504030201000709;");
    }

    @Test
    public void testHandleIdentifyEventsUnknown() throws Exception {
        createWithDefaults();
        sendFrame(":X19968444N0333;");
        //sendFrame(":X19970444N;");
        expectFrame(":X19547333N0504030201000708;");
        expectFrame(":X19547333N0504030201000709;");
        expectFrame(":X194C7333N0504030201000708;");
        expectFrame(":X194C7333N0504030201000709;");
    }

    @Test
    public void testHandleIdentifyEventsKnown() throws Exception {
        createWithDefaults();
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

    @Test
    public void testGenerateEvents() throws Exception {
        createWithDefaults();
        VersionedValue<Boolean> v = pc.getValue();
        sendFrameAndExpectResult( //
                ":X19914444N0504030201000708;",
                ":X19547333N0504030201000708;");

        expectNoFrames();

        v.set(false);
        expectFrame(":X195B4333N0504030201000709;");

        expectNoFrames();
        sendFrameAndExpectResult( //
                ":X19914444N0504030201000708;",
                ":X19545333N0504030201000708;");
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

    @Test
    public void testGenerateUnknown() throws Exception {
        pc = new BitProducerConsumer(iface, onEvent, offEvent, BitProducerConsumer.IS_PRODUCER | BitProducerConsumer.IS_CONSUMER | BitProducerConsumer.SEND_UNKNOWN_EVENT_IDENTIFIED);

        expectFrame(":X19547333N0504030201000708;", times(1));
        expectFrame(":X19547333N0504030201000709;");
        expectFrame(":X194C7333N0504030201000708;", times(1));
        expectFrame(":X194C7333N0504030201000709;");

        expectNoFrames();
        sendFrameAndExpectResult( //
                ":X19914444N0504030201000708;",
                ":X19547333N0504030201000708;");
        expectNoFrames();

        // set the value
        VersionedValue<Boolean> v = pc.getValue();
        v.set(false);
        expectFrame(":X195B4333N0504030201000709;");
        // now a query tell us NO different
        sendFrameAndExpectResult( //
                ":X19914444N0504030201000708;",
                ":X19547333N0504030201000708;");
        expectNoFrames();
        sendFrameAndExpectResult( //
                ":X19914444N0504030201000709;",
                ":X19547333N0504030201000709;");
        expectNoFrames();

        v.set(true);
        expectFrame(":X195B4333N0504030201000708;");
        // still a query tell us nothing
        sendFrameAndExpectResult( //
                ":X19914444N0504030201000708;",
                ":X19547333N0504030201000708;");
        expectNoFrames();
        sendFrameAndExpectResult( //
                ":X19914444N0504030201000709;",
                ":X19547333N0504030201000709;");
        expectNoFrames();
    }

    @Test
    public void testProducerOnlyNoListen() throws Exception {
        pc = new BitProducerConsumer(iface, onEvent, offEvent, BitProducerConsumer.IS_PRODUCER);
        // startup
        expectFrame(":X19547333N0504030201000708;");
        expectFrame(":X19547333N0504030201000709;");

        expectNoFrames();

        // send query and get back the same
        sendFrame(":X19968444N0333;");
        expectFrame(":X19547333N0504030201000708;");
        expectFrame(":X19547333N0504030201000709;");
        expectNoFrames();

        helperInputNoChange(":X19545333N0504030201000708;",
                ":X19545333N0504030201000709;");
        helperInputNoChange(":X195B4333N0504030201000708;",
                ":X195B4333N0504030201000709;");


        // set the value
        VersionedValue<Boolean> v = pc.getValue();
        v.set(false);
        expectFrame(":X195B4333N0504030201000709;");
        // now a query tell us different
        expectNoFrames();
        Assert.assertFalse(pc.isValueAtDefault());

        sendFrameAndExpectResult( //
                ":X19914444N0504030201000708;",
                ":X19545333N0504030201000708;");
        expectNoFrames();

        Assert.assertFalse(v.getLatestData());
        v.set(true);
        expectFrame(":X195B4333N0504030201000708;");
        expectNoFrames();

        Assert.assertTrue(v.getLatestData());

        helperInputNoChange(":X19545333N0504030201000708;",
                ":X19545333N0504030201000709;");
        helperInputNoChange(":X195B4333N0504030201000708;",
                ":X195B4333N0504030201000709;");
    }

    @Test
    public void testConsumerOnlyNoListen() throws Exception {
        pc = new BitProducerConsumer(iface, onEvent, offEvent, BitProducerConsumer.IS_CONSUMER);
        expectFrame(":X194C7333N0504030201000708;");
        expectFrame(":X194C7333N0504030201000709;");

        expectNoFrames();

        // send query and get back the same
        sendFrame(":X19968444N0333;");
        expectFrame(":X194C7333N0504030201000708;");
        expectFrame(":X194C7333N0504030201000709;");
        expectNoFrames();

        helperInputNoChange(":X19545333N0504030201000708;",
                ":X19545333N0504030201000709;");
        helperInputNoChange(":X194C5333N0504030201000708;",
                ":X194C5333N0504030201000709;");

        helperInputSetClear(":X195B4333N0504030201000708;",
                ":X195B4333N0504030201000709;");

        // set the value
        helperNotProducing();

        sendFrame(":X19968444N0333;");
        expectFrame(":X194C5333N0504030201000708;");
        expectFrame(":X194C4333N0504030201000709;");
    }

    @Test
    public void testConsumerOnlyListenFirst() throws Exception {
        pc = new BitProducerConsumer(iface, onEvent, offEvent, BitProducerConsumer.IS_CONSUMER | BitProducerConsumer.QUERY_AT_STARTUP);

        expectFrame(":X194C7333N0504030201000708;");
        expectFrame(":X194C7333N0504030201000709;");

        expectFrame(":X19914333N0504030201000709;");
        expectFrame(":X198F4333N0504030201000709;");
        expectFrame(":X19914333N0504030201000708;");
        expectFrame(":X198F4333N0504030201000708;");

        expectNoFrames();

        sendFrame(":X19968444N0333;");
        expectFrame(":X194C7333N0504030201000708;");
        expectFrame(":X194C7333N0504030201000709;");
        expectNoFrames();

        Assert.assertTrue(pc.isValueAtDefault());
        sendFrame(":X19544333N0504030201000708;");
        expectNoFrames();
        Assert.assertFalse(pc.isValueAtDefault());
        Assert.assertTrue(pc.getValue().getLatestData());

        sendFrame(":X19968444N0333;");
        expectFrame(":X194C4333N0504030201000708;");
        expectFrame(":X194C5333N0504030201000709;");
        expectNoFrames();

        // after the first query we are not listening anymore.
        helperInputNoChange(":X19545333N0504030201000708;",
                ":X19545333N0504030201000709;");
        helperInputNoChange(":X194C5333N0504030201000708;",
                ":X194C5333N0504030201000709;");

        helperInputSetClear(":X195B4333N0504030201000708;",
                ":X195B4333N0504030201000709;");

        helperNotProducing();
    }

    @Test
    public void testConsumerOnlyListenFirstSetState() throws Exception {
        pc = new BitProducerConsumer(iface, onEvent, offEvent, BitProducerConsumer.IS_CONSUMER | BitProducerConsumer.QUERY_AT_STARTUP);

        expectFrame(":X194C7333N0504030201000708;");
        expectFrame(":X194C7333N0504030201000709;");

        expectFrame(":X19914333N0504030201000709;");
        expectFrame(":X198F4333N0504030201000709;");
        expectFrame(":X19914333N0504030201000708;");
        expectFrame(":X198F4333N0504030201000708;");

        expectNoFrames();
        // If we set the internal state first, then
        helperNotProducing();
        // we will not be listening for initial state anymore
        helperInputNoChange(":X194C5333N0504030201000708;",
                ":X194C5333N0504030201000709;");
    }

    @Test
    public void testConsumerOnlyListenAlways() throws Exception {
        pc = new BitProducerConsumer(iface, onEvent, offEvent, BitProducerConsumer.IS_CONSUMER | BitProducerConsumer.LISTEN_EVENT_IDENTIFIED | BitProducerConsumer.LISTEN_INVALID_STATE);

        expectFrame(":X194C7333N0504030201000708;");
        expectFrame(":X194C7333N0504030201000709;");

        expectNoFrames();
        helperNotProducing();
        helperInputSetClear(":X194C5333N0504030201000709;",
                ":X194C5333N0504030201000708;");
        helperInputSetClear(":X195B4333N0504030201000708;",
                ":X195B4333N0504030201000709;");
        helperInputSetClear(":X19544333N0504030201000708;",
                ":X19544333N0504030201000709;");
    }

    @Test
    public void testListenNoInvalid() throws Exception {
        pc = new BitProducerConsumer(iface, onEvent, offEvent, BitProducerConsumer.IS_CONSUMER | BitProducerConsumer.LISTEN_EVENT_IDENTIFIED);

        expectFrame(":X194C7333N0504030201000708;");
        expectFrame(":X194C7333N0504030201000709;");

        expectNoFrames();
        helperNotProducing();

        helperInputSetClear(":X195B4333N0504030201000708;",
                ":X195B4333N0504030201000709;");
        helperInputSetClear(":X19544333N0504030201000708;",
                ":X19544333N0504030201000709;");
        helperInputNoChange(":X194C5333N0504030201000709;",
                ":X194C5333N0504030201000708;");
    }

    @Test
    public void testOneEventNull() throws Exception {
        pc = new BitProducerConsumer(iface, onEvent, pc.nullEvent, BitProducerConsumer.IS_PRODUCER | BitProducerConsumer.IS_CONSUMER | BitProducerConsumer.LISTEN_EVENT_IDENTIFIED |
                BitProducerConsumer.LISTEN_INVALID_STATE);

        expectFrame(":X19547333N0504030201000708;");
        expectFrame(":X194C7333N0504030201000708;");

        expectNoFrames();

        VersionedValue<Boolean> v = pc.getValue();
        v.set(false);
        expectNoFrames();
        v.set(true);
        expectFrame(":X195B4333N0504030201000708;");
        expectNoFrames();
        v.set(false);
        expectNoFrames();
        v.set(true);
        expectFrame(":X195B4333N0504030201000708;");
        expectNoFrames();

        // on one event we can flipflop
        helperInputSetClear(":X194C4333N0504030201000708;",
                ":X194C5333N0504030201000708;");
        helperInputSetClear(":X19544333N0504030201000708;",
                ":X19545333N0504030201000708;");
        // but the other is ignored
        helperInputNoChange(":X194C4333N0504030201000709;",
                ":X194C5333N0504030201000709;");

        v.set(true);
        expectFrame(":X195B4333N0504030201000708;");
        expectNoFrames();

        // send query and get back the same
        sendFrame(":X19968444N0333;");
        expectFrame(":X19544333N0504030201000708;");
        expectFrame(":X194C4333N0504030201000708;");

    }

    @Test
    public void testSendQuery() {
        createWithDefaults();

        pc.sendQuery();
        expectFrame(":X19914333N0504030201000709;");
        expectFrame(":X198F4333N0504030201000709;");
        expectFrame(":X19914333N0504030201000708;");
        expectFrame(":X198F4333N0504030201000708;");
        expectNoFrames();
    }

    @Test
    public void testResetToDefault() {
        createWithDefaults();
        MockVersionedValueListener<Boolean> listener = new MockVersionedValueListener<>(pc
                .getValue());

        // baseline: at default.
        sendFrameAndExpectResult( //
                ":X198F4444N0504030201000709;",
                ":X194C7333N0504030201000709;");
        sendFrameAndExpectResult( //
                ":X198F4444N0504030201000708;",
                ":X194C7333N0504030201000708;");

        verifyNoMoreInteractions(listener.stub);

        // Sets to off. Callback comes.
        sendFrame(":X195B4444N0504030201000709;");

        verify(listener.stub).update(false);
        verifyNoMoreInteractions(listener.stub);
        reset(listener.stub);
        // queries return definite state
        sendFrameAndExpectResult( //
                ":X198F4444N0504030201000709;",
                ":X194C4333N0504030201000709;");
        sendFrameAndExpectResult( //
                ":X198F4444N0504030201000708;",
                ":X194C5333N0504030201000708;");
        // fun starts here
        pc.resetToDefault();
        // queries return unknown
        sendFrameAndExpectResult( //
                ":X198F4444N0504030201000709;",
                ":X194C7333N0504030201000709;");
        sendFrameAndExpectResult( //
                ":X198F4444N0504030201000708;",
                ":X194C7333N0504030201000708;");

        verifyNoMoreInteractions(listener.stub);
        reset(listener.stub);

        // Sets to off with a PCER
        sendFrame(":X195B4444N0504030201000709;");
        // queries return definite state
        sendFrameAndExpectResult( //
                ":X198F4444N0504030201000709;",
                ":X194C4333N0504030201000709;");
        sendFrameAndExpectResult( //
                ":X198F4444N0504030201000708;",
                ":X194C5333N0504030201000708;");

        verify(listener.stub).update(false);
        verifyNoMoreInteractions(listener.stub);
        reset(listener.stub);

        pc.resetToDefault();
        // queries return unknown
        sendFrameAndExpectResult( //
                ":X198F4444N0504030201000709;",
                ":X194C7333N0504030201000709;");
        sendFrameAndExpectResult( //
                ":X198F4444N0504030201000708;",
                ":X194C7333N0504030201000708;");
        // Sets to off with a consumer identified. This usually happens when sendQuery() is
        // invoked and the layout responds. We will get a callback too.
        sendFrame(":X19545333N0504030201000708;");
        verify(listener.stub).update(false);
        verifyNoMoreInteractions(listener.stub);
        reset(listener.stub);
        // queries return definite state
        sendFrameAndExpectResult( //
                ":X198F4444N0504030201000709;",
                ":X194C4333N0504030201000709;");
        sendFrameAndExpectResult( //
                ":X198F4444N0504030201000708;",
                ":X194C5333N0504030201000708;");
    }

    private void createWithDefaults() {
        pc = new BitProducerConsumer(iface, onEvent, offEvent, false);
        expectFrame(":X19547333N0504030201000708;", times(1));
        expectFrame(":X19547333N0504030201000709;");
        expectFrame(":X194C7333N0504030201000708;", times(1));
        expectFrame(":X194C7333N0504030201000709;");

        expectFrame(":X19914333N0504030201000709;");
        expectFrame(":X198F4333N0504030201000709;");
        expectFrame(":X19914333N0504030201000708;");
        expectFrame(":X198F4333N0504030201000708;");
        expectNoFrames();
    }

    @Before
    public void setUp() {
        super.setUp();
        aliasMap.insert(0x444, new NodeID(new byte[]{1,2,3,1,2,3}));
    }

    @After
    public void tearDown() {
        super.tearDown();
    }
}
