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
