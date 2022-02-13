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

        msg = TractionControlRequestMessage.createSetSpeed(src, dst, false, 13 * MPH);
        Assert.assertEquals("00 C5 D0", Utilities.toHexSpaceString(msg.getPayload()));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "set speed R 13 mph", msg.toString());

        msg = TractionControlRequestMessage.createSetSpeed(src, dst, true, 126 * MPH);
        Assert.assertEquals("00 53 0A", Utilities.toHexSpaceString(msg.getPayload()));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "set speed F 126 mph", msg.toString());

        msg = TractionControlRequestMessage.createSetSpeed(src, dst, false, 126 * MPH);
        Assert.assertEquals("00 D3 0A", Utilities.toHexSpaceString(msg.getPayload()));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "set speed R 126 mph", msg.toString());
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

        msg = TractionControlRequestMessage.createSetFn(src, dst,
                12298905, 56762);
        Assert.assertEquals("01 BB AA 99 DD BA", Utilities.toHexSpaceString(msg.getPayload()));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlRequest " +
                "set fn 12298905 to 56762", msg.toString());
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
        TractionControlRequestMessage msg = TractionControlRequestMessage.createAssignController
                (src,
                dst);
        byte[] payload = msg.getPayload();
        Assert.assertEquals("20 01 00 06 05 05 04 04 03", Utilities.toHexSpaceString(payload));
        Assert.assertEquals(src, msg.getSourceNodeID());
        Assert.assertEquals(dst, msg.getDestNodeID());
    }

}
