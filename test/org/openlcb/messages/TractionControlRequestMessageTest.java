package org.openlcb.messages;

import org.junit.*;

import org.openlcb.NodeID;
import org.openlcb.Utilities;
import org.openlcb.implementations.throttle.Float16;

/**
 * Created by bracz on 12/30/15.
 */
public class TractionControlRequestMessageTest  {
    protected NodeID src = new NodeID(new byte[]{6,5,5,4,4,3});
    protected NodeID dst = new NodeID(new byte[]{2,2,2,4,4,4});

    public static final double MPH = TractionControlRequestMessage.MPH;

    @Test
    public void testGetSpeed() throws Exception {
        double speed = 13.5;
        TractionControlRequestMessage msg = TractionControlRequestMessage.createSetSpeed(src,
                dst, true, speed);
        Assert.assertEquals(0, msg.getCmd());
        Float16 sp = msg.getSpeed();
        Assert.assertEquals(13.5, sp.getFloat(), 0.01);
    }

    @Test
    public void testCreateSetSpeed() throws Exception {
        TractionControlRequestMessage msg = TractionControlRequestMessage.createSetSpeed(src, dst
                , true, 13 * MPH);
        Assert.assertEquals("00 45 D0", Utilities.toHexSpaceString(msg.getPayload()));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "set speed F 13 mph", msg.toString());
        Assert.assertEquals(13 * MPH, msg.getSpeed().getFloat(), 1e-2);

        msg = TractionControlRequestMessage.createSetSpeed(src, dst, false, 13 * MPH);
        Assert.assertEquals("00 C5 D0", Utilities.toHexSpaceString(msg.getPayload()));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "set speed R 13 mph", msg.toString());
        Assert.assertEquals(-13 * MPH, msg.getSpeed().getFloat(), 1e-2);

        msg = TractionControlRequestMessage.createSetSpeed(src, dst, true, 126 * MPH);
        Assert.assertEquals("00 53 0A", Utilities.toHexSpaceString(msg.getPayload()));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "set speed F 126 mph", msg.toString());
        Assert.assertEquals(126 * MPH, msg.getSpeed().getFloat(), 1e-1);

        msg = TractionControlRequestMessage.createSetSpeed(src, dst, false, 126 * MPH);
        Assert.assertEquals("00 D3 0A", Utilities.toHexSpaceString(msg.getPayload()));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "set speed R 126 mph", msg.toString());
        Assert.assertEquals(-126 * MPH, msg.getSpeed().getFloat(), 1e-1);
    }

    @Test public void testListenerReply() throws Exception {
        TractionControlRequestMessage msg = new TractionControlRequestMessage(src, dst,
                Utilities.bytesFromHexString("80C5D0"));
        Assert.assertEquals(TractionControlRequestMessage.CMD_SET_SPEED, msg.getCmd());
        Assert.assertEquals(-13 * MPH, msg.getSpeed().getFloat(), 1e-2);
        Assert.assertEquals(true,  msg.isListenerMessage());

        msg = new TractionControlRequestMessage(src, dst,
                Utilities.bytesFromHexString("81 BB AA 99 DD BA"));
        Assert.assertEquals(TractionControlRequestMessage.CMD_SET_FN, msg.getCmd());
        Assert.assertEquals(12298905, msg.getFnNumber());
        Assert.assertEquals(56762, msg.getFnVal());
        Assert.assertEquals(true,  msg.isListenerMessage());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "[listener] set fn 12298905 to 56762", msg.toString());

        msg = new TractionControlRequestMessage(src, dst,
                Utilities.bytesFromHexString("01 BB AA 99 DD BA"));
        Assert.assertEquals(TractionControlRequestMessage.CMD_SET_FN, msg.getCmd());
        Assert.assertEquals(12298905, msg.getFnNumber());
        Assert.assertEquals(56762, msg.getFnVal());
        Assert.assertEquals(false,  msg.isListenerMessage());
    }

    @Test
    public void testCreateGetSpeed() throws Exception {
        TractionControlRequestMessage msg = TractionControlRequestMessage.createGetSpeed(src, dst);
        Assert.assertEquals("10", Utilities.toHexSpaceString(msg.getPayload()));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "get speed", msg.toString());
    }

    @Test
    public void testCreateEstop() throws Exception {
        TractionControlRequestMessage msg = TractionControlRequestMessage.createSetEstop(src, dst);
        Assert.assertEquals("02", Utilities.toHexSpaceString(msg.getPayload()));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "set estop", msg.toString());
    }

    @Test
    public void testCreateSetFn() throws Exception {
        TractionControlRequestMessage msg = TractionControlRequestMessage.createSetFn(src, dst,
                11, 1);
        Assert.assertEquals("01 00 00 0B 00 01", Utilities.toHexSpaceString(msg.getPayload()));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "set fn 11 to 1", msg.toString());
        Assert.assertEquals(11, msg.getFnNumber());
        Assert.assertEquals(1, msg.getFnVal());

        msg = TractionControlRequestMessage.createSetFn(src, dst,
                12298905, 56762);
        Assert.assertEquals("01 BB AA 99 DD BA", Utilities.toHexSpaceString(msg.getPayload()));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "set fn 12298905 to 56762", msg.toString());
        Assert.assertEquals(12298905, msg.getFnNumber());
        Assert.assertEquals(56762, msg.getFnVal());
    }

    @Test
    public void testCreateGetFn() throws Exception {
        TractionControlRequestMessage msg = TractionControlRequestMessage.createGetFn(src, dst, 11);
        Assert.assertEquals("11 00 00 0B", Utilities.toHexSpaceString(msg.getPayload()));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "get fn 11", msg.toString());

        msg = TractionControlRequestMessage.createGetFn(src, dst, 12298905);
        Assert.assertEquals("11 BB AA 99", Utilities.toHexSpaceString(msg.getPayload()));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "get fn 12298905", msg.toString());
    }


    @Test
    public void testAssignController() throws Exception {
        TractionControlRequestMessage msg = TractionControlRequestMessage.createAssignController(
                src, dst);
        byte[] payload = msg.getPayload();
        Assert.assertEquals("20 01 00 06 05 05 04 04 03", Utilities.toHexSpaceString(payload));
        Assert.assertEquals(src, msg.getSourceNodeID());
        Assert.assertEquals(dst, msg.getDestNodeID());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "assign controller 06.05.05.04.04.03", msg.toString());
    }

    @Test
    public void testReleaseController() throws Exception {
        TractionControlRequestMessage msg = TractionControlRequestMessage.createReleaseController(
                src, dst);
        byte[] payload = msg.getPayload();
        Assert.assertEquals("20 02 00 06 05 05 04 04 03", Utilities.toHexSpaceString(payload));
        Assert.assertEquals(src, msg.getSourceNodeID());
        Assert.assertEquals(dst, msg.getDestNodeID());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "release controller 06.05.05.04.04.03", msg.toString());
    }

    @Test
    public void testQueryController() throws Exception {
        TractionControlRequestMessage msg = TractionControlRequestMessage.createQueryController(
                src, dst);
        byte[] payload = msg.getPayload();
        Assert.assertEquals("20 03", Utilities.toHexSpaceString(payload));
        Assert.assertEquals(src, msg.getSourceNodeID());
        Assert.assertEquals(dst, msg.getDestNodeID());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "query controller", msg.toString());
    }

    @Test
    public void testNotifyControllerChange() throws Exception {
        TractionControlRequestMessage msg = TractionControlRequestMessage.createControllerChangeNotify(
                src, dst, new NodeID(0x090099112233L));
        byte[] payload = msg.getPayload();
        Assert.assertEquals("20 04 00 09 00 99 11 22 33", Utilities.toHexSpaceString(payload));
        Assert.assertEquals(src, msg.getSourceNodeID());
        Assert.assertEquals(dst, msg.getDestNodeID());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "notify controller change to 09.00.99.11.22.33", msg.toString());
    }

    @Test
    public void testConsistAttach() throws Exception {
        TractionControlRequestMessage msg = TractionControlRequestMessage.createConsistAttach(
                src, dst, new NodeID(0x090099112233L),
                TractionControlRequestMessage.CONSIST_FLAG_HIDE | TractionControlRequestMessage.CONSIST_FLAG_FN0);
        byte[] payload = msg.getPayload();
        Assert.assertEquals("30 01 84 09 00 99 11 22 33", Utilities.toHexSpaceString(payload));
        Assert.assertEquals(src, msg.getSourceNodeID());
        Assert.assertEquals(dst, msg.getDestNodeID());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "listener attach 09.00.99.11.22.33 flags link-f0,hide", msg.toString());
    }

    @Test
    public void testConsistDetach() throws Exception {
        TractionControlRequestMessage msg = TractionControlRequestMessage.createConsistDetach(
                src, dst, new NodeID(0x090099112233L));
        byte[] payload = msg.getPayload();
        Assert.assertEquals("30 02 00 09 00 99 11 22 33", Utilities.toHexSpaceString(payload));
        Assert.assertEquals(src, msg.getSourceNodeID());
        Assert.assertEquals(dst, msg.getDestNodeID());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "listener detach 09.00.99.11.22.33", msg.toString());
    }

    @Test
    public void testConsistLengthQuery() throws Exception {
        TractionControlRequestMessage msg = TractionControlRequestMessage.createConsistLengthQuery(
                src, dst);
        byte[] payload = msg.getPayload();
        Assert.assertEquals("30 03", Utilities.toHexSpaceString(payload));
        Assert.assertEquals(src, msg.getSourceNodeID());
        Assert.assertEquals(dst, msg.getDestNodeID());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "listener query", msg.toString());
    }

    @Test
    public void testConsistIndexQuery() throws Exception {
        TractionControlRequestMessage msg = TractionControlRequestMessage.createConsistIndexQuery(
                src, dst,4);
        byte[] payload = msg.getPayload();
        Assert.assertEquals("30 03 04", Utilities.toHexSpaceString(payload));
        Assert.assertEquals(src, msg.getSourceNodeID());
        Assert.assertEquals(dst, msg.getDestNodeID());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "listener query index 4", msg.toString());
    }

    @Test
    public void testMgmtReserve() throws Exception {
        TractionControlRequestMessage msg = TractionControlRequestMessage.createReserve(src, dst);
        byte[] payload = msg.getPayload();
        Assert.assertEquals("40 01", Utilities.toHexSpaceString(payload));
        Assert.assertEquals(src, msg.getSourceNodeID());
        Assert.assertEquals(dst, msg.getDestNodeID());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "management reserve", msg.toString());
    }

    @Test
    public void testMgmtRelease() throws Exception {
        TractionControlRequestMessage msg = TractionControlRequestMessage.createRelease(src, dst);
        byte[] payload = msg.getPayload();
        Assert.assertEquals("40 02", Utilities.toHexSpaceString(payload));
        Assert.assertEquals(src, msg.getSourceNodeID());
        Assert.assertEquals(dst, msg.getDestNodeID());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "management release", msg.toString());
    }

    @Test
    public void testMgmtNoop() throws Exception {
        TractionControlRequestMessage msg = TractionControlRequestMessage.createNoop(src, dst);
        byte[] payload = msg.getPayload();
        Assert.assertEquals("40 03", Utilities.toHexSpaceString(payload));
        Assert.assertEquals(src, msg.getSourceNodeID());
        Assert.assertEquals(dst, msg.getDestNodeID());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "noop/heartbeat", msg.toString());
    }

}
