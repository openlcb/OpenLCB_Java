package org.openlcb.messages;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlcb.*;
import static org.openlcb.messages.TractionControlRequestMessage.MPH;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TractionControlReplyMessageTest {

    NodeID src = new NodeID(new byte[]{6,5,5,4,4,3});
    NodeID dst = new NodeID(new byte[]{2,2,2,4,4,4});

    @Test
    public void testCTor() {
        byte[] payload = new byte[]{0x40,0x01,0x00}; // Traciton Management Reply message
        TractionControlReplyMessage t = new TractionControlReplyMessage(src,dst,payload);
        Assert.assertNotNull("exists",t);
    }
 
    @Test
    public void testGetcommand(){
        byte[] payload = new byte[]{0x40,0x01,0x00}; // Traciton Management Reply message
        TractionControlReplyMessage t = new TractionControlReplyMessage(src,dst,payload);
        Assert.assertEquals("command",0x40,t.getCmd());
    }

    @Test
    public void testSpeedReply(){
        TractionControlReplyMessage t = new TractionControlReplyMessage(src,dst,
                Utilities.bytesFromHexString("10 45 D0 00"));
        Assert.assertEquals("command",0x10,t.getCmd());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlReply " +
                "speed reply F 13 mph",t.toString());
        Assert.assertEquals(13 * MPH, t.getSetSpeed().getFloat(), 0.01);
        t = new TractionControlReplyMessage(src,dst, Utilities.bytesFromHexString("10 00 00 01"));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlReply " +
                "speed reply F 0 mph estop",t.toString());
        t = new TractionControlReplyMessage(src,dst, Utilities.bytesFromHexString("10 80 00 01"));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlReply " +
                "speed reply R 0 mph estop",t.toString());
        t = new TractionControlReplyMessage(src,dst, Utilities.bytesFromHexString("10 80 00 00 " +
                "45 D0 C5 D0"));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlReply " +
                "speed reply R 0 mph commanded speed F 13 mph actual speed R 13 mph",t.toString());
        Assert.assertEquals(-0.0 * MPH, t.getSetSpeed().getFloat(), 0.01);
        Assert.assertEquals(13 * MPH, t.getCommandedSpeed().getFloat(), 0.01);
        Assert.assertEquals(-13 * MPH, t.getActualSpeed().getFloat(), 0.01);
    }

    @Test
    public void testFnReply() {
        TractionControlReplyMessage t = new TractionControlReplyMessage(src, dst,
                Utilities.bytesFromHexString("11 00 00 0B 00 01"));
        Assert.assertEquals("command", 0x11, t.getCmd());
        Assert.assertEquals("function num", 11, t.getFnNumber());
        Assert.assertEquals("function value", 1, t.getFnVal());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlReply " +
                "fn 11 is 1", t.toString());
        t = new TractionControlReplyMessage(src, dst,
                Utilities.bytesFromHexString("11 BB AA 99 DD BA"));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlReply " +
                "fn 12298905 is 56762", t.toString());
        Assert.assertEquals("function num", 12298905, t.getFnNumber());
        Assert.assertEquals("function value", 56762, t.getFnVal());
    }

    @Test
    public void testControllerAssignReply() {
        TractionControlReplyMessage t = new TractionControlReplyMessage(src, dst,
                Utilities.bytesFromHexString("20 01 00"));
        Assert.assertEquals("command", 0x20, t.getCmd());
        Assert.assertEquals("subcommand", 0x01, t.getSubCmd());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlReply " +
                "controller assign OK", t.toString());
        t = new TractionControlReplyMessage(src, dst,
                Utilities.bytesFromHexString("20 01 01"));
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlReply " +
                "controller assign fail 0x01", t.toString());
    }

    @Test
    public void testControllerQueryReply() {
        TractionControlReplyMessage t = new TractionControlReplyMessage(src, dst,
                Utilities.bytesFromHexString("20 03 00 09 00 99 FF EE DD"));
        Assert.assertEquals("command", 0x20, t.getCmd());
        Assert.assertEquals("subcommand", 0x03, t.getSubCmd());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlReply " +
                "controller is 09.00.99.FF.EE.DD", t.toString());
        Assert.assertEquals("controller ID", 0x090099FFEEDDL,
                t.getCurrentControllerReply().toLong());
    }

    @Test
    public void testListenerAttachReply() {
        TractionControlReplyMessage t = new TractionControlReplyMessage(src, dst,
                Utilities.bytesFromHexString("30 01 09 00 99 DD EE FF 00 00"));
        Assert.assertEquals("command", 0x30, t.getCmd());
        Assert.assertEquals("subcommand", 0x01, t.getSubCmd());
        Assert.assertEquals("code", 0, t.getConsistAttachCode());
        Assert.assertEquals("node ID", 0x090099DDEEFFL, t.getConsistAttachNodeID().toLong());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlReply " +
                "listener attach 09.00.99.DD.EE.FF result 0x0000", t.toString());

        t = new TractionControlReplyMessage(src, dst, Utilities.bytesFromHexString("30 01 09 00 " +
                "99 DD EE FF 20 30"));
        Assert.assertEquals("code", 0x2030, t.getConsistAttachCode());
        Assert.assertEquals("node ID", 0x090099DDEEFFL, t.getConsistAttachNodeID().toLong());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlReply " +
                "listener attach 09.00.99.DD.EE.FF result 0x2030", t.toString());
    }

    @Test
    public void testListenerDetachReply() {
        TractionControlReplyMessage t = new TractionControlReplyMessage(src, dst,
                Utilities.bytesFromHexString("30 02 09 00 99 DD EE FF 00 00"));
        Assert.assertEquals("command", 0x30, t.getCmd());
        Assert.assertEquals("subcommand", 0x02, t.getSubCmd());
        Assert.assertEquals("code", 0, t.getConsistAttachCode());
        Assert.assertEquals("node ID", 0x090099DDEEFFL, t.getConsistAttachNodeID().toLong());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlReply " +
                "listener detach 09.00.99.DD.EE.FF result 0x0000", t.toString());

        t = new TractionControlReplyMessage(src, dst, Utilities.bytesFromHexString("30 02 09 00 " +
                "99 DD EE FF 20 30"));
        Assert.assertEquals("code", 0x2030, t.getConsistAttachCode());
        Assert.assertEquals("node ID", 0x090099DDEEFFL, t.getConsistAttachNodeID().toLong());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlReply " +
                "listener detach 09.00.99.DD.EE.FF result 0x2030", t.toString());
    }

    @Test
    public void testListenerQueryReply() {
        TractionControlReplyMessage t = new TractionControlReplyMessage(src, dst,
                Utilities.bytesFromHexString("30 03 13"));
        Assert.assertEquals("command", 0x30, t.getCmd());
        Assert.assertEquals("subcommand", 0x03, t.getSubCmd());
        Assert.assertEquals("count", 0x13, t.getConsistLength());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlReply " +
                "listener is count 19", t.toString());

        t = new TractionControlReplyMessage(src, dst, Utilities.bytesFromHexString("30 03 13 04 " +
                "82 09 00 99 DD EE FF"));
        Assert.assertEquals("command", 0x30, t.getCmd());
        Assert.assertEquals("subcommand", 0x03, t.getSubCmd());
        Assert.assertEquals("count", 0x13, t.getConsistLength());
        Assert.assertEquals("index", 4, t.getConsistIndex());
        Assert.assertEquals("node ID", 0x090099DDEEFFL, t.getConsistQueryNodeID().toLong());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlReply " +
                "listener is count 19 index 4 flags reverse,hide is 09.00.99.DD.EE.FF",
                t.toString());
    }

    @Test
    public void testReserveReply() {
        TractionControlReplyMessage t = new TractionControlReplyMessage(src, dst,
                Utilities.bytesFromHexString("40 01 00"));
        Assert.assertEquals("command", 0x40, t.getCmd());
        Assert.assertEquals("subcommand", 0x01, t.getSubCmd());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlReply " +
                "reserve reply OK", t.toString());
        t = new TractionControlReplyMessage(src, dst,
                Utilities.bytesFromHexString("40 01 5A"));
        Assert.assertEquals("command", 0x40, t.getCmd());
        Assert.assertEquals("subcommand", 0x01, t.getSubCmd());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlReply " +
                "reserve reply error 0x5a", t.toString());
    }

    @Test
    public void testHeartbeatRequest() {
        TractionControlReplyMessage t = new TractionControlReplyMessage(src, dst,
                Utilities.bytesFromHexString("40 03 05"));
        Assert.assertEquals("command", 0x40, t.getCmd());
        Assert.assertEquals("subcommand", 0x03, t.getSubCmd());
        Assert.assertEquals("06.05.05.04.04.03 - 02.02.02.04.04.04 TractionControlReply " +
                "heartbeat request in 5 seconds", t.toString());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

}
