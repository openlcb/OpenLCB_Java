package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.*;

/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class MessageTypeIdentifierTest extends TestCase {

    public void testCtor() {
        MessageTypeIdentifier mti1 = MessageTypeIdentifier.InitializationComplete;
    }

    public void testEquals() {
        MessageTypeIdentifier mti1 = MessageTypeIdentifier.InitializationComplete;
        MessageTypeIdentifier mti2 = MessageTypeIdentifier.InitializationComplete;
        Assert.assertEquals(mti1, mti2);
    }

    public void testNotEquals() {
        MessageTypeIdentifier mti1 = MessageTypeIdentifier.InitializationComplete;
        MessageTypeIdentifier mti2 = MessageTypeIdentifier.VerifyNodeIdAddressed;
        Assert.assertTrue(!mti1.equals(mti2));
    }

    public void testToString() {
        MessageTypeIdentifier mti1 = MessageTypeIdentifier.InitializationComplete;
        Assert.assertEquals("InitializationComplete", mti1.toString());
        
    }
    
    public void testMtiValues() {
        MessageTypeIdentifier mti;
        
        Assert.assertEquals(0x1080, MessageTypeIdentifier.InitializationComplete.mti());
        Assert.assertEquals(0x10A4, MessageTypeIdentifier.VerifyNodeIdAddressed.mti());
        Assert.assertEquals(0x18A0, MessageTypeIdentifier.VerifyNodeIdGlobal.mti());
        Assert.assertEquals(0x18B0, MessageTypeIdentifier.VerifiedNodeId.mti());
        Assert.assertEquals(0x10C4, MessageTypeIdentifier.OptionalInteractionRejected.mti());
        Assert.assertEquals(0x10D4, MessageTypeIdentifier.TerminateDueToError.mti());

        Assert.assertEquals(0x12E4, MessageTypeIdentifier.ProtocolSupportInquiry.mti());
        Assert.assertEquals(0x12F4, MessageTypeIdentifier.ProtocolSupportReply.mti());

        Assert.assertEquals(0x1A42, MessageTypeIdentifier.IdentifyConsumer.mti());
        Assert.assertEquals(0x1252, MessageTypeIdentifier.ConsumerIdentifyRange.mti());
        Assert.assertEquals(0x1263, MessageTypeIdentifier.ConsumerIdentified.mti());

        Assert.assertEquals(0x1A82, MessageTypeIdentifier.IdentifyProducer.mti());
        Assert.assertEquals(0x1292, MessageTypeIdentifier.ProducerIdentifyRange.mti());
        Assert.assertEquals(0x12A3, MessageTypeIdentifier.ProducerIdentified.mti());

        Assert.assertEquals(0x12B4, MessageTypeIdentifier.IdentifyEventsAddressed.mti());
        Assert.assertEquals(0x1AB0, MessageTypeIdentifier.IdentifyEventsGlobal.mti());

        Assert.assertEquals(0x1AC2, MessageTypeIdentifier.LearnEvent.mti());
        Assert.assertEquals(0x1AD2, MessageTypeIdentifier.ProducerConsumerEventReport.mti());

        Assert.assertEquals(0x1524, MessageTypeIdentifier.SimpleNodeIdentInfoRequest.mti());
        Assert.assertEquals(0x1534, MessageTypeIdentifier.SimpleNodeIdentInfoReply.mti());

        Assert.assertEquals(0x1404, MessageTypeIdentifier.Datagram.mti());
        Assert.assertEquals(0x14C4, MessageTypeIdentifier.DatagramReceivedOK.mti());
        Assert.assertEquals(0x14D4, MessageTypeIdentifier.DatagramRejected.mti());

        Assert.assertEquals(0x14E4, MessageTypeIdentifier.StreamInitiateRequest.mti());
        Assert.assertEquals(0x14F4, MessageTypeIdentifier.StreamInitiateReply.mti());
        Assert.assertEquals(0x1694, MessageTypeIdentifier.StreamDataSend.mti());
        Assert.assertEquals(0x16A4, MessageTypeIdentifier.StreamDataProceed.mti());
        Assert.assertEquals(0x16B4, MessageTypeIdentifier.StreamDataComplete.mti());
    }

    public void testForOverlaps() {
        ArrayList<Long> mtis = new ArrayList<Long>();
        for (MessageTypeIdentifier item : MessageTypeIdentifier.values()) {
            if (mtis.contains(item.mti())) {
                Assert.fail("MTI "+item.mti()+" is a duplicate, 2nd is "+item.toString());
            }
            mtis.add(item.mti());
        }
    }
    
    // from here down is testing infrastructure
    
    public MessageTypeIdentifierTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MessageTypeIdentifierTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MessageTypeIdentifierTest.class);
        return suite;
    }
}
