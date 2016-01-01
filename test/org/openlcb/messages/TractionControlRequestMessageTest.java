package org.openlcb.messages;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openlcb.NodeID;
import org.openlcb.Utilities;
import org.openlcb.implementations.throttle.Float16;

/**
 * Created by bracz on 12/30/15.
 */
public class TractionControlRequestMessageTest extends TestCase {
    protected NodeID src = new NodeID(new byte[]{6,5,5,4,4,3});
    protected NodeID dst = new NodeID(new byte[]{2,2,2,4,4,4});

    public void testGetSpeed() throws Exception {
        double speed = 13.5;
        TractionControlRequestMessage msg = TractionControlRequestMessage.createSetSpeed(src,
                dst, true, speed);
        assertEquals(0, msg.getCmd());
        Float16 sp = msg.getSpeed();
        assertEquals(13.5, sp.getFloat(), 0.01);
    }

    public void testAssignController() throws Exception {
        TractionControlRequestMessage msg = TractionControlRequestMessage.createAssignController
                (src,
                dst);
        byte[] payload = msg.getPayload();
        assertEquals("20 01 00 06 05 05 04 04 03", Utilities.toHexSpaceString(payload));
        assertEquals(src, msg.getSourceNodeID());
        assertEquals(dst, msg.getDestNodeID());
    }

    // from here down is testing infrastructure

    public TractionControlRequestMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {TractionControlRequestMessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TractionControlRequestMessageTest.class);
        return suite;
    }
}