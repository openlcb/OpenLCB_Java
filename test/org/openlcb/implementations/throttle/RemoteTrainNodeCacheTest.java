package org.openlcb.implementations.throttle;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
 * @version $Revision$
 */
public class RemoteTrainNodeCacheTest extends TestCase {
    TrainNodeCache cache;
    FakeOlcbInterface fakeInterface;

    public RemoteTrainNodeCacheTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {RemoteTrainNodeCacheTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RemoteTrainNodeCacheTest.class);
        return suite;
    }

    public void testSetup() {
        Assert.assertTrue(cache != null);
    }

    public void testEmpty() {
        Assert.assertTrue(cache.getList() != null);
        Assert.assertEquals(0, cache.getList().size());
    }

    public void testIgnoresMessage() {

        Message m = new OptionalIntRejectedMessage(null, null, 0, 0);

        cache.put(m, null);
        Assert.assertTrue(cache.getList() != null);
        Assert.assertEquals(0, cache.getList().size());
    }

    public void testIgnoresEvent() {

        Message m = new ProducerConsumerEventReportMessage(null, new EventID("01.02.03.04.05.06" +
                ".07.08"));

        cache.put(m, null);
        Assert.assertTrue(cache.getList() != null);
        Assert.assertEquals(0, cache.getList().size());
    }

    // from here down is testing infrastructure

    public void testSeesIsTrainEvent() {
        Message m = new ProducerConsumerEventReportMessage(new NodeID(new byte[]{1, 1, 0, 0, 4,
                4}), CommonIdentifiers.IS_TRAIN);

        cache.put(m, null);
        Assert.assertTrue(cache.getList() != null);
        Assert.assertEquals(1, cache.getList().size());

        RemoteTrainNode tn = cache.getList().get(0);

        Assert.assertTrue(tn.getNodeId().equals(new NodeID(new byte[]{1, 1, 0, 0, 4, 4})));
    }

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

    public void setUp() {
        fakeInterface = new FakeOlcbInterface();
        cache = new TrainNodeCache(fakeInterface);
    }
}
