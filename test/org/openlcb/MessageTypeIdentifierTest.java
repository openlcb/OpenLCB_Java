package org.openlcb;

import org.junit.*;

import java.util.*;

/**
 * @author  Bob Jacobsen   Copyright 2012
 */
public class MessageTypeIdentifierTest  {

    @Test
    public void testCtor() {
        MessageTypeIdentifier mti1 = MessageTypeIdentifier.InitializationComplete;
    }

    @Test
    public void testEquals() {
        MessageTypeIdentifier mti1 = MessageTypeIdentifier.InitializationComplete;
        MessageTypeIdentifier mti2 = MessageTypeIdentifier.InitializationComplete;
        Assert.assertEquals(mti1, mti2);
    }

    @Test
    public void testNotEquals() {
        MessageTypeIdentifier mti1 = MessageTypeIdentifier.InitializationComplete;
        MessageTypeIdentifier mti2 = MessageTypeIdentifier.VerifyNodeIdAddressed;
        Assert.assertTrue(!mti1.equals(mti2));
    }

    @Test
    public void testToString() {
        MessageTypeIdentifier mti1 = MessageTypeIdentifier.InitializationComplete;
        Assert.assertEquals("InitializationComplete", mti1.toString());
        
    }
    
    @Test
    public void testMtiValues() {
        MessageTypeIdentifier mti;
        
        Assert.assertEquals(0x0100, MessageTypeIdentifier.InitializationComplete.mti());
        Assert.assertEquals(0x0488, MessageTypeIdentifier.VerifyNodeIdAddressed.mti());
        Assert.assertEquals(0x0490, MessageTypeIdentifier.VerifyNodeIdGlobal.mti());
        Assert.assertEquals(0x0170, MessageTypeIdentifier.VerifiedNodeId.mti());
        Assert.assertEquals(0x0068, MessageTypeIdentifier.OptionalInteractionRejected.mti());
        Assert.assertEquals(0x00A8, MessageTypeIdentifier.TerminateDueToError.mti());

        Assert.assertEquals(0x0828, MessageTypeIdentifier.ProtocolSupportInquiry.mti());
        Assert.assertEquals(0x0668, MessageTypeIdentifier.ProtocolSupportReply.mti());

        Assert.assertEquals(0x08F4, MessageTypeIdentifier.IdentifyConsumer.mti());
        Assert.assertEquals(0x04A4, MessageTypeIdentifier.ConsumerIdentifyRange.mti());
        Assert.assertEquals(0x04C7, MessageTypeIdentifier.ConsumerIdentifiedUnknown.mti());
        Assert.assertEquals(0x04C4, MessageTypeIdentifier.ConsumerIdentifiedValid.mti());
        Assert.assertEquals(0x04C5, MessageTypeIdentifier.ConsumerIdentifiedInvalid.mti());

        Assert.assertEquals(0x0914, MessageTypeIdentifier.IdentifyProducer.mti());
        Assert.assertEquals(0x0524, MessageTypeIdentifier.ProducerIdentifyRange.mti());
        Assert.assertEquals(0x0547, MessageTypeIdentifier.ProducerIdentifiedUnknown.mti());
        Assert.assertEquals(0x0544, MessageTypeIdentifier.ProducerIdentifiedValid.mti());
        Assert.assertEquals(0x0545, MessageTypeIdentifier.ProducerIdentifiedInvalid.mti());

        Assert.assertEquals(0x0968, MessageTypeIdentifier.IdentifyEventsAddressed.mti());
        Assert.assertEquals(0x0970, MessageTypeIdentifier.IdentifyEventsGlobal.mti());

        Assert.assertEquals(0x0594, MessageTypeIdentifier.LearnEvent.mti());
        Assert.assertEquals(0x05B4, MessageTypeIdentifier.ProducerConsumerEventReport.mti());

        Assert.assertEquals(0x05EB, MessageTypeIdentifier.TractionControlRequest.mti());
        Assert.assertEquals(0x01E9, MessageTypeIdentifier.TractionControlReply.mti());
        Assert.assertEquals(0x05EA, MessageTypeIdentifier.TractionProxyRequest.mti());
        Assert.assertEquals(0x01E8, MessageTypeIdentifier.TractionProxyReply.mti());

        Assert.assertEquals(0x0DE8, MessageTypeIdentifier.SimpleNodeIdentInfoRequest.mti());
        Assert.assertEquals(0x0A08, MessageTypeIdentifier.SimpleNodeIdentInfoReply.mti());

        Assert.assertEquals(0x1C48, MessageTypeIdentifier.Datagram.mti());
        Assert.assertEquals(0x0A28, MessageTypeIdentifier.DatagramReceivedOK.mti());
        Assert.assertEquals(0x0A48, MessageTypeIdentifier.DatagramRejected.mti());

        Assert.assertEquals(0x0CC8, MessageTypeIdentifier.StreamInitiateRequest.mti());
        Assert.assertEquals(0x0868, MessageTypeIdentifier.StreamInitiateReply.mti());
        Assert.assertEquals(0x1F88, MessageTypeIdentifier.StreamDataSend.mti());
        Assert.assertEquals(0x0888, MessageTypeIdentifier.StreamDataProceed.mti());
        Assert.assertEquals(0x08A8, MessageTypeIdentifier.StreamDataComplete.mti());
    }

    @Test
    public void testForOverlaps() {
        ArrayList<Integer> mtis = new ArrayList<Integer>();
        for (MessageTypeIdentifier item : MessageTypeIdentifier.values()) {
            if (mtis.contains(item.mti())) {
                Assert.fail("MTI "+item.mti()+" is a duplicate, 2nd is "+item.toString());
            }
            mtis.add(item.mti());
        }
    }
    
    @Test
    public void testMap() {
        Assert.assertEquals(MessageTypeIdentifier.InitializationComplete, 
            MessageTypeIdentifier.get(MessageTypeIdentifier.InitializationComplete.mti()));

        Assert.assertEquals(MessageTypeIdentifier.SimpleNodeIdentInfoRequest, 
            MessageTypeIdentifier.get(MessageTypeIdentifier.SimpleNodeIdentInfoRequest.mti()));

        Assert.assertEquals(MessageTypeIdentifier.Datagram, 
            MessageTypeIdentifier.get(MessageTypeIdentifier.Datagram.mti()));
    }
}
