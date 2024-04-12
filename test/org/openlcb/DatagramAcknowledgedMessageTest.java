package org.openlcb;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlcb.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DatagramAcknowledgedMessageTest {

    @Test
    public void testCTor() {
        NodeID id1 = new NodeID(new byte[]{1, 1, 0, 0, 0, 4});
        NodeID id2 = new NodeID(new byte[]{1, 1, 0, 0, 4, 4});
        DatagramAcknowledgedMessage t = new DatagramAcknowledgedMessage(id1,id2);
        Assert.assertNotNull("exists",t);
    }    
    
    @Test
    public void testToString() {
        NodeID id1 = new NodeID(new byte[]{1, 1, 0, 0, 0, 4});
        NodeID id2 = new NodeID(new byte[]{1, 1, 0, 0, 4, 4});
        DatagramAcknowledgedMessage t = new DatagramAcknowledgedMessage(id1,id2, 0x80);

        Assert.assertEquals("01.01.00.00.00.04 - 01.01.00.00.04.04 DatagramReceivedOK with payload 80 flags 0x80", t.toString());
    }

    @Test
    public void testToStringZeroFlag() {
        NodeID id1 = new NodeID(new byte[]{1, 1, 0, 0, 0, 4});
        NodeID id2 = new NodeID(new byte[]{1, 1, 0, 0, 4, 4});
        DatagramAcknowledgedMessage t = new DatagramAcknowledgedMessage(id1,id2, 0x00);

        Assert.assertEquals("01.01.00.00.00.04 - 01.01.00.00.04.04 DatagramReceivedOK with payload 00 flags 0x0", t.toString());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

}
