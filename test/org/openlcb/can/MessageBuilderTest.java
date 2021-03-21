package org.openlcb.can;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlcb.AddressedMessage;
import org.openlcb.AddressedPayloadMessage;
import org.openlcb.DatagramAcknowledgedMessage;
import org.openlcb.DatagramMessage;
import org.openlcb.EventID;
import org.openlcb.IdentifyEventsMessage;
import org.openlcb.InitializationCompleteMessage;
import org.openlcb.Message;
import org.openlcb.NodeID;
import org.openlcb.OptionalIntRejectedMessage;
import org.openlcb.ProducerConsumerEventReportMessage;
import org.openlcb.ProtocolIdentificationReplyMessage;
import org.openlcb.SimpleNodeIdentInfoReplyMessage;
import org.openlcb.StreamDataCompleteMessage;
import org.openlcb.StreamDataProceedMessage;
import org.openlcb.StreamInitiateReplyMessage;
import org.openlcb.StreamInitiateRequestMessage;
import org.openlcb.Utilities;
import org.openlcb.VerifiedNodeIDNumberMessage;
import org.openlcb.VerifyNodeIDNumberMessage;
import org.openlcb.implementations.DatagramUtils;
import org.openlcb.messages.TractionControlReplyMessage;
import org.openlcb.messages.TractionControlRequestMessage;
import org.openlcb.messages.TractionProxyReplyMessage;
import org.openlcb.messages.TractionProxyRequestMessage;

/**
 * @author  Bob Jacobsen   Copyright 2010
 */
public class MessageBuilderTest  {
   
    @Test	
    public void testCtor() {
        Assert.assertNotNull("exists",new MessageBuilder(map));
    }
    
    /** ****************************************************
     * Tests of messages into frames
     ***************************************************** */
     
    @Test	
    public void testInitializationCompleteMessage() {
        Message m = new InitializationCompleteMessage(source);
        MessageBuilder b = new MessageBuilder(map);
        
        List<OpenLcbCanFrame> list = b.processMessage(m);
        
        // looking for [19100123] 01 02 03 04 05 06
        
        Assert.assertEquals("count", 1, list.size()); 
        CanFrame f0 = list.get(0);
        Assert.assertEquals("header", toHexString(0x19100123), toHexString(f0.getHeader()));
        compareContent(source.getContents(), f0);
    }
    
    @Test	
    public void testVerifyNodeIDNumberMessageEmpty() {
        Message m = new VerifyNodeIDNumberMessage(source);
        MessageBuilder b = new MessageBuilder(map);
        
        List<OpenLcbCanFrame> list = b.processMessage(m);
        
        // looking for [19490123]
        
        Assert.assertEquals("count", 1, list.size()); 
        CanFrame f0 = list.get(0);
        Assert.assertEquals("header", toHexString(0x19490123), toHexString(f0.getHeader()));
        compareContent(null, f0);
    }
    
    @Test	
    public void testVerifyNodeIDNumberMessageWithContent() {
        Message m = new VerifyNodeIDNumberMessage(source, source);
        MessageBuilder b = new MessageBuilder(map);
        
        List<OpenLcbCanFrame> list = b.processMessage(m);
        
        // looking for [19490123]
        
        Assert.assertEquals("count", 1, list.size()); 
        CanFrame f0 = list.get(0);
        Assert.assertEquals("header", toHexString(0x19490123), toHexString(f0.getHeader()));
        compareContent(source.getContents(), f0);
    }

    @Test	
    public void testIdentifyEventsGlobal() {
        Message m = new IdentifyEventsMessage(source, null);
        MessageBuilder b = new MessageBuilder(map);

        List<OpenLcbCanFrame> list = b.processMessage(m);

        // looking for [19970123]

        Assert.assertEquals("count", 1, list.size());
        CanFrame f0 = list.get(0);
        Assert.assertEquals("header", toHexString(0x19970123), toHexString(f0.getHeader()));
        Assert.assertEquals("payload", 0, f0.getNumDataElements());
        compareContent(new byte[]{}, f0);
    }

    @Test	
    public void testIdentifyEventsAddressed() {
        Message m = new IdentifyEventsMessage(source, destination);
        MessageBuilder b = new MessageBuilder(map);

        List<OpenLcbCanFrame> list = b.processMessage(m);

        // looking for [19970123]

        Assert.assertEquals("count", 1, list.size());
        CanFrame f0 = list.get(0);
        Assert.assertEquals("header", toHexString(0x19968123), toHexString(f0.getHeader()));
        Assert.assertEquals("payload", 2, f0.getNumDataElements());
        compareContent(Utilities.bytesFromHexString("03 21"), f0);
    }

    @Test	
    public void testVerifiedNodeIDNumberMessage() {
        Message m = new VerifiedNodeIDNumberMessage(source);
        MessageBuilder b = new MessageBuilder(map);
        
        List<OpenLcbCanFrame> list = b.processMessage(m);
        
        // looking for [19170123] 01 02 03 04 05 06
        
        Assert.assertEquals("count", 1, list.size()); 
        CanFrame f0 = list.get(0);
        Assert.assertEquals("header", toHexString(0x19170123), toHexString(f0.getHeader()));
        compareContent(source.getContents(), f0);
    }

    @Test	
    public void testProducerConsumerEventReportMessage() {
        Message m = new ProducerConsumerEventReportMessage(source, event);
        MessageBuilder b = new MessageBuilder(map);
        
        List<OpenLcbCanFrame> list = b.processMessage(m);
        
        // looking for [195B4123] 11 12 13 14 15 16 17 18
        
        Assert.assertEquals("count", 1, list.size()); 
        CanFrame f0 = list.get(0);
        Assert.assertEquals("header", toHexString(0x195B4123), toHexString(f0.getHeader()));
        compareContent(event.getContents(), f0);
    }

    @Test	
    public void testTractionControlRequestMessageSingle() {
        Message m = new TractionControlRequestMessage(source, destination, new byte[]{(byte)0xCC,
                (byte)0xAA, 0x55, 4, 5, 6});
        MessageBuilder b = new MessageBuilder(map);

        List<OpenLcbCanFrame> list = b.processMessage(m);

        Assert.assertEquals("count", 1, list.size());
        CanFrame f0 = list.get(0);
        Assert.assertEquals("header", toHexString(0x195EB123), toHexString(f0.getHeader()));
        compareContent(Utilities.bytesFromHexString("03 21 CC AA 55 04 05 06"), f0);
    }

    @Test	
    public void testTractionControlRequestMessageMulti() {
        Message m = new TractionControlRequestMessage(source, destination, new byte[]{(byte)0xCC,
                (byte)0xAA, 0x55, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14});
        MessageBuilder b = new MessageBuilder(map);

        List<OpenLcbCanFrame> list = b.processMessage(m);

        Assert.assertEquals("count", 3, list.size());
        Assert.assertEquals("header", toHexString(0x195EB123), toHexString(list.get(0).getHeader()));
        Assert.assertEquals("header", toHexString(0x195EB123), toHexString(list.get(1).getHeader()));
        Assert.assertEquals("header", toHexString(0x195EB123), toHexString(list.get(2).getHeader()));
        compareContent(Utilities.bytesFromHexString("13 21 CC AA 55 04 05 06"), list.get(0));
        compareContent(Utilities.bytesFromHexString("33 21 07 08 09 0a 0b 0c"), list.get(1));
        compareContent(Utilities.bytesFromHexString("23 21 0d 0e"), list.get(2));
    }

    @Test	
    public void testTractionControlReplyMessage() {
        Message m = new TractionControlReplyMessage(source, destination, new byte[]{(byte)0xCC,
                (byte)0xAA, 0x55, 4, 5, 6});
        MessageBuilder b = new MessageBuilder(map);

        List<OpenLcbCanFrame> list = b.processMessage(m);

        Assert.assertEquals("count", 1, list.size());
        CanFrame f0 = list.get(0);
        Assert.assertEquals("header", toHexString(0x191E9123), toHexString(f0.getHeader()));
        compareContent(Utilities.bytesFromHexString("03 21 CC AA 55 04 05 06"), f0);
    }

    @Test	
    public void testTractionProxyRequestMessage() {
        Message m = new TractionProxyRequestMessage(source, destination, new byte[]{(byte)0xCC,
                (byte)0xAA, 0x55, 4, 5, 6});
        MessageBuilder b = new MessageBuilder(map);

        List<OpenLcbCanFrame> list = b.processMessage(m);

        Assert.assertEquals("count", 1, list.size());
        CanFrame f0 = list.get(0);
        Assert.assertEquals("header", toHexString(0x195EA123), toHexString(f0.getHeader()));
        compareContent(Utilities.bytesFromHexString("03 21 CC AA 55 04 05 06"), f0);
    }

    @Test	
    public void testTractionProxyReplyMessage() {
        Message m = new TractionProxyReplyMessage(source, destination, new byte[]{(byte)0xCC,
                (byte)0xAA, 0x55, 4, 5, 6});
        MessageBuilder b = new MessageBuilder(map);

        List<OpenLcbCanFrame> list = b.processMessage(m);

        Assert.assertEquals("count", 1, list.size());
        CanFrame f0 = list.get(0);
        Assert.assertEquals("header", toHexString(0x191E8123), toHexString(f0.getHeader()));
        compareContent(Utilities.bytesFromHexString("03 21 CC AA 55 04 05 06"), f0);
    }

    @Test	
    public void testDatagramMessageShort() {
        int[] data = new int[]{21,22,23};
        Message m = new DatagramMessage(source, destination, data);
        MessageBuilder b = new MessageBuilder(map);
        
        List<OpenLcbCanFrame> list = b.processMessage(m);
        
        Assert.assertEquals("count", 1, list.size()); 
        CanFrame f0 = list.get(0);

        Assert.assertEquals("header", toHexString(0x1A321123), toHexString(f0.getHeader()));
        compareContent(new byte[]{21,22,23}, f0);
    }

    @Test	
    public void testDatagramMessageEight() {
        int[] data = new int[]{21,22,23,24,25,26,27,28};
        Message m = new DatagramMessage(source, destination, data);
        MessageBuilder b = new MessageBuilder(map);
        
        List<OpenLcbCanFrame> list = b.processMessage(m);
        
        Assert.assertEquals("count", 1, list.size()); 
        CanFrame f0 = list.get(0);

        Assert.assertEquals("header", toHexString(0x1A321123), toHexString(f0.getHeader()));
        compareContent(new byte[]{21,22,23,24,25,26,27,28}, f0);
    }

    @Test	
    public void testDatagramMessageTwoFrame() {
        int[] data = new int[]{21,22,23,24,25,26,27,28, 
                               31,32,33,34,35,36,37,38};
                               
        Message m = new DatagramMessage(source, destination, data);
        MessageBuilder b = new MessageBuilder(map);
        
        List<OpenLcbCanFrame> list = b.processMessage(m);
        
        
        Assert.assertEquals("count", 2, list.size()); 

        CanFrame f0 = list.get(0);
        Assert.assertEquals("f0 header", toHexString(0x1B321123), toHexString(f0.getHeader()));
        compareContent(new byte[]{21,22,23,24,25,26,27,28}, f0);

        CanFrame f1 = list.get(1);
        Assert.assertEquals("f1 header", toHexString(0x1D321123), toHexString(f1.getHeader()));
        compareContent(new byte[]{31,32,33,34,35,36,37,38}, f1);
    }

    @Test	
    public void testDatagramMessageNine() {
        int[] data = new int[]{21,22,23,24,25,26,27,28,31};
                               
        Message m = new DatagramMessage(source, destination, data);
        MessageBuilder b = new MessageBuilder(map);
        
        List<OpenLcbCanFrame> list = b.processMessage(m);
        
        
        Assert.assertEquals("count", 2, list.size()); 

        CanFrame f0 = list.get(0);
        Assert.assertEquals("f0 header", toHexString(0x1B321123), toHexString(f0.getHeader()));
        compareContent(new byte[]{21,22,23,24,25,26,27,28}, f0);

        CanFrame f1 = list.get(1);
        Assert.assertEquals("f1 header", toHexString(0x1D321123), toHexString(f1.getHeader()));
        compareContent(new byte[]{31}, f1);
    }

    @Test	
    public void testDatagramOKMessage() {
        Message m = new DatagramAcknowledgedMessage(source, destination, 0x0);
        MessageBuilder b = new MessageBuilder(map);

        List<OpenLcbCanFrame> list = b.processMessage(m);

        // looking for [19A28123] 03 21 80

        Assert.assertEquals("count", 1, list.size());
        CanFrame f0 = list.get(0);
        Assert.assertEquals("header", toHexString(0x19A28123), toHexString(f0.getHeader()));
        compareContent(new byte[]{0x03, 0x21}, f0);
    }

    @Test	
    public void testDatagramOKMessageWithPayload() {
        Message m = new DatagramAcknowledgedMessage(source, destination, 0x80);
        MessageBuilder b = new MessageBuilder(map);

        List<OpenLcbCanFrame> list = b.processMessage(m);

        // looking for [19A28123] 03 21 80

        Assert.assertEquals("count", 1, list.size());
        CanFrame f0 = list.get(0);
        Assert.assertEquals("header", toHexString(0x19A28123), toHexString(f0.getHeader()));
        compareContent(new byte[]{0x03, 0x21, (byte)0x80}, f0);
    }

    /** ****************************************************
     * Tests of messages into frames
     ***************************************************** */

    @Test	
    public void testInitializationCompleteFrame() {
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x19100123);
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(frame);
        
        Assert.assertEquals("count", 1, list.size()); 
        Message msg = list.get(0);
        
        Assert.assertTrue(msg instanceof InitializationCompleteMessage);        
    }
    
    @Test	
    public void testVerifyNodeEmptyFrame() {
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x19490123);
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(frame);
        
        Assert.assertEquals("count", 1, list.size()); 
        Message msg = list.get(0);
        
        Assert.assertTrue(msg instanceof VerifyNodeIDNumberMessage); 
        Assert.assertEquals(new VerifyNodeIDNumberMessage(source), msg);
    }
    
    @Test	
    public void testVerifyNodeContentFrame() {
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x19490123);
        frame.setData(new byte[]{1,2,3,4,5,6});
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(frame);
        
        Assert.assertEquals("count", 1, list.size()); 
        Message msg = list.get(0);
        
        Assert.assertTrue(msg instanceof VerifyNodeIDNumberMessage);        
        Assert.assertEquals(new VerifyNodeIDNumberMessage(source, source), msg);
    }

    @Test	
    public void testSingleFrameDatagram() {
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x1A321123);
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(frame);
        
        Assert.assertEquals("count", 1, list.size()); 
        Message msg = list.get(0);
        
        Assert.assertTrue(msg instanceof DatagramMessage);        
        Assert.assertEquals("source", source, msg.getSourceNodeID());
        Assert.assertEquals("destination", destination, ((AddressedMessage)msg).getDestNodeID());
    }
    
    @Test	
    public void testTwoFrameDatagram() {
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x1B321123);
        frame.setData(new byte[]{1,2});
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(frame);
        
        Assert.assertNull(list); 
        
        frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x1D321123);
        frame.setData(new byte[]{11,12,13});
                
        list = b.processFrame(frame);

        Assert.assertEquals("count", 1, list.size()); 
        Message msg = list.get(0);
        Assert.assertTrue(msg instanceof DatagramMessage);  
        int[] data =  ((DatagramMessage)msg).getData();   
        Assert.assertEquals(5, data.length);
        Assert.assertEquals(1,data[0]);
        Assert.assertEquals(2,data[1]);
        Assert.assertEquals(11,data[2]);
        Assert.assertEquals(12,data[3]);
        Assert.assertEquals(13,data[4]);
    }
    
    @Test	
    public void testPipReplyFrame() {
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x19668071); 
        frame.setData(new byte[]{0x02, (byte)0xB4, (byte)0xD5, 0x00, 0x00, 0x00, 0x00, 0x00});
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(frame);
        
        Assert.assertEquals("count", 1, list.size()); 
        Message msg = list.get(0);
        
        Assert.assertTrue(msg instanceof ProtocolIdentificationReplyMessage);  
        
        Assert.assertTrue(((ProtocolIdentificationReplyMessage)msg).getValue() == 0xD50000000000L);    
    }
    
    @Test	
    public void testOptionalRejectFrame1() {
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x19068071); 
        frame.setData(new byte[]{0x02, 0x02, (byte)0x12, 0x34, 0x56, 0x78, 0x00, 0x00});
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(frame);
        
        Assert.assertEquals("count", 1, list.size()); 
        Message msg = list.get(0);
        
        Assert.assertTrue(msg instanceof OptionalIntRejectedMessage);  
        
        Assert.assertEquals(0x1234, ((OptionalIntRejectedMessage)msg).getRejectMTI());    
        Assert.assertEquals(0x5678, ((OptionalIntRejectedMessage)msg).getCode());    
    }
    
    @Test	
    public void testOptionalRejectFrame2() {
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x19068071); 
        frame.setData(new byte[]{0x02, 0x02, (byte)0x12, 0x34, 0x56, 0x78});
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(frame);
        
        Assert.assertEquals("count", 1, list.size()); 
        Message msg = list.get(0);
        
        Assert.assertTrue(msg instanceof OptionalIntRejectedMessage);  
        
        Assert.assertEquals(0x1234, ((OptionalIntRejectedMessage)msg).getRejectMTI());    
        Assert.assertEquals(0x5678, ((OptionalIntRejectedMessage)msg).getCode());    
    }
    
    @Test	
    public void testOptionalRejectFrame3() {
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x19068071); 
        frame.setData(new byte[]{0x02, 0x02, (byte)0x12, 0x34});
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(frame);
        
        Assert.assertEquals("count", 1, list.size()); 
        Message msg = list.get(0);
        
        Assert.assertTrue(msg instanceof OptionalIntRejectedMessage);  
        
        Assert.assertEquals(0x1234, ((OptionalIntRejectedMessage)msg).getRejectMTI());    
        Assert.assertEquals(0, ((OptionalIntRejectedMessage)msg).getCode());    
    }
    
    @Test	
    public void testBogusMti() {
        // should emit "failed to parse MTI 0x541"
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x19541071); 
        frame.setData(new byte[]{0x02, 0x02, (byte)0x12, 0x34});
        
        MessageBuilder b = new MessageBuilder(map);
        
        System.out.println("Expect next line to be \" failed to parse MTI 0x541\"");
        List<Message> list = b.processFrame(frame);
        
        Assert.assertEquals("count", 0, list.size()); 
    }

    @Test	
    public void testAccumulateSniipReply() {
        // start frame
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x19A08071); 
        frame.setData(new byte[]{0x12, 0x02, 0x12, 0x34});
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(frame);
        
        Assert.assertEquals("count", 0, list.size()); 
        
        // end frame
        frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x19A08071); 
        frame.setData(new byte[]{0x22, 0x02, 0x56, 0x78});
                
        list = b.processFrame(frame);
        
        Assert.assertEquals("count", 1, list.size()); 
        Message msg = list.get(0);
        Assert.assertTrue(msg instanceof SimpleNodeIdentInfoReplyMessage);  
        
        Assert.assertEquals(0x12, ((SimpleNodeIdentInfoReplyMessage)msg).getData()[0]);    
        Assert.assertEquals(0x34, ((SimpleNodeIdentInfoReplyMessage)msg).getData()[1]);    
        Assert.assertEquals(0x56, ((SimpleNodeIdentInfoReplyMessage)msg).getData()[2]);    
        Assert.assertEquals(0x78, ((SimpleNodeIdentInfoReplyMessage)msg).getData()[3]);  
        
        // check for no stored state
        frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x19A08071); 
        frame.setData(new byte[]{0x02, 0x02, 0x12, 0x34});

        list = b.processFrame(frame);
        
        Assert.assertEquals("count", 1, list.size()); 
        msg = list.get(0);
        Assert.assertTrue(msg instanceof SimpleNodeIdentInfoReplyMessage);  
        
        Assert.assertEquals(2, ((SimpleNodeIdentInfoReplyMessage)msg).getData().length);    
        Assert.assertEquals(0x12, ((SimpleNodeIdentInfoReplyMessage)msg).getData()[0]);    
        Assert.assertEquals(0x34, ((SimpleNodeIdentInfoReplyMessage)msg).getData()[1]);    
    }

    @Test	
    public void testTractionControlRequestParseSingle() {
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x195EB123);
        frame.setData(new byte[]{0x03, 0x21, 0x12, 0x34});

        MessageBuilder b = new MessageBuilder(map);
        List<Message> list = b.processFrame(frame);

        Assert.assertEquals("count", 1, list.size());

        Message msg = list.get(0);
        Assert.assertTrue(msg instanceof TractionControlRequestMessage);
        Assert.assertEquals("payload", "12 34", Utilities.toHexSpaceString(((AddressedPayloadMessage)msg).getPayload()));
    }

    @Test	
    public void testTractionProxyReplyParseMulti() {
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x191E8123);
        frame.setData(new byte[]{0x13, 0x21, 0x12, 0x34, 3, 4, 5, 6});

        MessageBuilder b = new MessageBuilder(map);
        List<Message> list = b.processFrame(new OpenLcbCanFrame(frame));

        if (list != null) {
            Assert.assertEquals("count", 0, list.size());
        }

        frame.setData(new byte[]{0x33, 0x21, 0x5, 0x4, 13, 14, 15, 16});
        list = b.processFrame(new OpenLcbCanFrame(frame));

        if (list != null) {
            Assert.assertEquals("count", 0, list.size());
        }

        frame.setData(new byte[]{0x23, 0x21, 17, 18});
        list = b.processFrame(new OpenLcbCanFrame(frame));

        Assert.assertNotNull(list);
        Assert.assertEquals("count", 1, list.size());

        Message msg = list.get(0);
        Assert.assertTrue(msg instanceof TractionProxyReplyMessage);
        Assert.assertEquals("payload", "12 34 03 04 05 06 05 04 0D 0E 0F 10 11 12", Utilities
                .toHexSpaceString(((AddressedPayloadMessage) msg).getPayload()));
        Assert.assertEquals("srcnode", source, msg.getSourceNodeID());
        Assert.assertEquals("dstnode", destination, ((AddressedPayloadMessage) msg).getDestNodeID());
    }

    @Test	
    public void testAliasExtraction() {
    
        NodeID high = new NodeID(new byte[]{11,12,13,14,15,16});
        map.insert(0x0FFF, high);
        
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x1AFFF123);
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(frame);
        
        Assert.assertEquals("count", 1, list.size()); 
        Message msg = list.get(0);
        
        Assert.assertTrue(msg instanceof DatagramMessage);        

        Assert.assertEquals("source", source, msg.getSourceNodeID());
        Assert.assertEquals("destination", high, ((AddressedMessage)msg).getDestNodeID());
    }

    @Test	
    public void testDatagramOKFrame() {
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x19A28123);
        frame.setData(new byte[]{0x03, 0x21, (byte) 0x80});

        MessageBuilder b = new MessageBuilder(map);

        List<Message> list = b.processFrame(frame);

        Assert.assertEquals("count", 1, list.size());
        Message msg = list.get(0);

        Assert.assertTrue(msg instanceof DatagramAcknowledgedMessage);
        DatagramAcknowledgedMessage dmsg = (DatagramAcknowledgedMessage) msg;

        Assert.assertEquals(source, dmsg.getSourceNodeID());
        Assert.assertEquals(destination, dmsg.getDestNodeID());
        Assert.assertEquals(0x80, dmsg.getFlags());
    }

    @Test	
    public void testAddressedMessageAliasExtraction() {
        NodeID high = new NodeID(new byte[]{11,12,13,14,15,16});
        map.insert(0x0FFF, high);
        
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x19A28123);
        frame.setData(new byte[]{(byte)0x0F, (byte)0xFF});
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(frame);
        
        Assert.assertEquals("count", 1, list.size()); 
        Message msg = list.get(0);
        
        Assert.assertTrue(msg instanceof AddressedMessage);        

        Assert.assertEquals("source", source, msg.getSourceNodeID());
        Assert.assertEquals("destination", high, ((AddressedMessage)msg).getDestNodeID());
    }
    
    // dph Stream tests
    @Test	
    public void testStreamInitiateRequestMessage() {
        NodeID high = new NodeID(new byte[]{11,12,13,14,15,16});
        map.insert(0x0FFF, high);
        OpenLcbCanFrame f = new OpenLcbCanFrame(0x123);
        f.setHeader(0x19CC8123);
        // dest(2), maxBuffer(2),flags(2),sourceStream, reserved
        f.setData(new byte[]{(byte)0x0F, (byte)0xFF, 0, 6, 0, 0, 4, 0});
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(f);
        
        Assert.assertEquals("count", 1, list.size());
        Message msg = list.get(0);
        
        Assert.assertTrue(msg instanceof StreamInitiateRequestMessage);
        
        Assert.assertEquals("source", source, msg.getSourceNodeID());
        Assert.assertEquals("destination", high, ((StreamInitiateRequestMessage)msg).getDestNodeID());
        Assert.assertEquals("max buffer ",6,(f.getElement(2)<<8)+f.getElement(3));
        Assert.assertEquals("flags ",0,(f.getElement(4)<<8)+f.getElement(5));
        Assert.assertEquals("sourceStreamID ",4,f.getElement(6));
    }
    
    @Test	
    public void testStreamInitiateReplyMessage() {
        NodeID high = new NodeID(new byte[]{11,12,13,14,15,16});
        map.insert(0x0FFF, high);
        OpenLcbCanFrame f = new OpenLcbCanFrame(0x123);
        f.setHeader(0x19868123);
        // dest(2), bufferesize(2), flags(2),sourceStream, destinationStream
        f.setData(new byte[]{(byte)0x0F, (byte)0xFF, 0, 64, 0, 0, 4, 6});
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(f);
        
        Assert.assertEquals("count", 1, list.size());
        Message msg = list.get(0);
        
        Assert.assertTrue(msg instanceof StreamInitiateReplyMessage);
        //int[] data =  ((DatagramMessage)msg).getData();
        Assert.assertEquals("source", source, msg.getSourceNodeID());
        Assert.assertEquals("destination", high, ((StreamInitiateReplyMessage)msg).getDestNodeID());
        Assert.assertEquals("max buffer ",64,(f.getElement(2)<<8)+f.getElement(3));
        Assert.assertEquals("flags ",0,(f.getElement(4)<<8)+f.getElement(5));
        Assert.assertEquals("sourceStreamID ",4,f.getElement(6));
        Assert.assertEquals("destinationStreamID ",6,f.getElement(7));
    }

    @Test
    @Ignore("commented out in JUnit 3")
    public void testTwoStreamData() {
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x1F321123);
        frame.setData(new byte[]{1,2,3,4,5});
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(frame);
        
        Assert.assertNull(list);
        
        frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x1F321123);
        frame.setData(new byte[]{11,12,13,14,15});
        
        list = b.processFrame(frame);
        
        Assert.assertEquals("count", 1, list.size());
        Message msg = list.get(0);
        Assert.assertTrue(msg instanceof DatagramMessage);
        int[] data =  ((DatagramMessage)msg).getData();
        Assert.assertEquals(5, data.length);
        Assert.assertEquals(1,data[0]);
        Assert.assertEquals(2,data[1]);
        Assert.assertEquals(3,data[2]);
        Assert.assertEquals(4,data[3]);
        Assert.assertEquals(5,data[4]);
        Assert.assertEquals(11,data[5]);
        Assert.assertEquals(12,data[6]);
        Assert.assertEquals(13,data[7]);
        Assert.assertEquals(14,data[8]);
        Assert.assertEquals(15,data[9]);
    }
 
    @Test
    public void testStreamDataProceedMessage() {
        NodeID high = new NodeID(new byte[]{11,12,13,14,15,16});
        map.insert(0x0FFF, high);
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x19888123);
        // sourceStream, destinationStream, flags(2)
        frame.setData(new byte[]{(byte)0x0F, (byte)0xFF, 4, 6, 0, 0});
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(frame);
        
        Assert.assertEquals("count", 1, list.size());
        Message msg = list.get(0);
        
        Assert.assertTrue(msg instanceof StreamDataProceedMessage);
        Assert.assertEquals("source", source, msg.getSourceNodeID());
        Assert.assertEquals("destination", high, ((StreamDataProceedMessage)msg).getDestNodeID());
        Assert.assertEquals("sourceStreamID ",frame.getElement(2),4);
        Assert.assertEquals("destinationStreamID ",frame.getElement(3),6);
        Assert.assertEquals("flags ",(frame.getElement(4)<<8)+frame.getElement(5),0);
    }
    
    @Test
    public void testStreamDataCompleteMessage() {
        NodeID high = new NodeID(new byte[]{11,12,13,14,15,16});
        map.insert(0x0FFF, high);
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x198A8123);
        // sourceStream, destinationStream, flags(2)
        frame.setData(new byte[]{(byte)0x0F, (byte)0xFF, 4, 6, 0, 0});
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(frame);
        
        Assert.assertEquals("count", 1, list.size());
        Message msg = list.get(0);
        
        Assert.assertTrue(msg instanceof StreamDataCompleteMessage);
        Assert.assertEquals("source", source, msg.getSourceNodeID());
        Assert.assertEquals("destination", high, ((StreamDataCompleteMessage)msg).getDestNodeID());
        Assert.assertEquals("sourceStreamID ",frame.getElement(2),4);
        Assert.assertEquals("destinationStreamID ",frame.getElement(3),6);
        Assert.assertEquals("flags ",(frame.getElement(4)<<8)+frame.getElement(5),0);
    }
    
    String toHexString(int n) {
        return Integer.toHexString(n);
    }
    
    void compareContent(byte[] data, CanFrame f) {
        if (data == null) {
            Assert.assertEquals("no data", 0, f.getNumDataElements());
        } else {
            Assert.assertEquals("data length", data.length, f.getNumDataElements());
            for (int i=0; i<data.length; i++) {
                Assert.assertEquals("data byte "+i, DatagramUtils.byteToInt(data[i]),f.getElement
                        (i));
            }
        }
    }
    
    NodeID source = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID destination = new NodeID(new byte[]{6,5,4,3,2,1});
    EventID event = new EventID(new byte[]{11,12,13,14,15,16,17,18});
    AliasMap map = new AliasMap();
   
    @Before 
    public void setUp() {
        map = new AliasMap();
        map.insert(0x0123, source);
        map.insert(0x321, destination);
    }

    @After
    public void tearDown(){
        map = null;
    }
}
