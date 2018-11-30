package org.openlcb.implementations.throttle;

import org.junit.*;

import org.openlcb.CommonIdentifiers;
import org.openlcb.EventID;
import org.openlcb.EventState;
import org.openlcb.FakeOlcbInterface;
import org.openlcb.Message;
import org.openlcb.NodeID;
import org.openlcb.OptionalIntRejectedMessage;
import org.openlcb.ProducerConsumerEventReportMessage;
import org.openlcb.ProducerIdentifiedMessage;

/**
 * @author Bob Jacobsen   Copyright 2012
 */
public class RemoteTrainNodeCacheTest {
    TrainNodeCache cache;
    FakeOlcbInterface fakeInterface;

    @Test
    public void testSetup() {
        Assert.assertTrue(cache != null);
    }

    @Test
    public void testEmpty() {
        Assert.assertTrue(cache.getList() != null);
        Assert.assertEquals(0, cache.getList().size());
    }

    @Test
    public void testIgnoresMessage() {

        Message m = new OptionalIntRejectedMessage(null, null, 0, 0);

        cache.put(m, null);
        Assert.assertTrue(cache.getList() != null);
        Assert.assertEquals(0, cache.getList().size());
    }

    @Test
    public void testIgnoresEvent() {

        Message m = new ProducerConsumerEventReportMessage(null, new EventID("01.02.03.04.05.06" +
                ".07.08"));

        cache.put(m, null);
        Assert.assertTrue(cache.getList() != null);
        Assert.assertEquals(0, cache.getList().size());
    }

    @Test
    public void testSeesIsTrainEvent() {
        Message m = new ProducerConsumerEventReportMessage(new NodeID(new byte[]{1, 1, 0, 0, 4,
                4}), CommonIdentifiers.IS_TRAIN);

        cache.put(m, null);
        Assert.assertTrue(cache.getList() != null);
        Assert.assertEquals(1, cache.getList().size());

        RemoteTrainNode tn = cache.getList().get(0);

        Assert.assertTrue(tn.getNodeId().equals(new NodeID(new byte[]{1, 1, 0, 0, 4, 4})));
    }

    @Test
    public void testNoDuplicates() {
        Message m = new ProducerConsumerEventReportMessage(new NodeID(new byte[]{1, 1, 0, 0, 4,
                4}), CommonIdentifiers.IS_TRAIN);

        cache.put(m, null);
        Assert.assertTrue(cache.getList() != null);
        Assert.assertEquals(1, cache.getList().size());

        m = new ProducerIdentifiedMessage(new NodeID(new byte[]{1, 1, 0, 0, 4, 4}),
                CommonIdentifiers.IS_TRAIN, EventState.Unknown);
        cache.put(m, null);

        Assert.assertEquals(1, cache.getList().size());

        m = new ProducerIdentifiedMessage(new NodeID(new byte[]{1, 1, 0, 0, 4, 5}), new EventID
                ("01.01.00.00.00.00.03.03"), EventState.Unknown);
        cache.put(m, null);

        Assert.assertEquals(2, cache.getList().size());

        Assert.assertEquals(new NodeID(new byte[]{1, 1, 0, 0, 4, 4}), cache.getCache(0).getNodeId());
        Assert.assertEquals(new NodeID(new byte[]{1, 1, 0, 0, 4, 5}), cache.getCache(1).getNodeId());
    }

    @Before
    public void setUp() {
        fakeInterface = new FakeOlcbInterface();
        cache = new TrainNodeCache(fakeInterface);
    }

    @After
    public void tearDown() {
        fakeInterface.dispose();
	fakeInterface = null;
	cache = null;
    }
}
