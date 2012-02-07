package org.openlcb.can;

import org.openlcb.*;

import java.util.List;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2010
 * @version $Revision$
 */
public class MessageBuilderTest extends TestCase {
    
    public void testCtor() {
    }
    
    public void testInitializationCompleteMessage() {
        
        Message m = new InitializationCompleteMessage(source);
        MessageBuilder b = new MessageBuilder(map);
        
        List<OpenLcbCanFrame> list = b.processMessage(m);
        
        // looking for [1908f123] 01 02 03 04 05 06
        
        Assert.assertEquals("count", 1, list.size()); 
        CanFrame f0 = list.get(0);
        Assert.assertEquals("header", toHexString(0x1908F123), toHexString(f0.getHeader()));
        compareContent(source.getContents(), f0);
    }
    public void testVerifyNodeIDNumberMessage() {
        
        Message m = new VerifyNodeIDNumberMessage(source);
        MessageBuilder b = new MessageBuilder(map);
        
        List<OpenLcbCanFrame> list = b.processMessage(m);
        
        // looking for [190AF123]
        
        Assert.assertEquals("count", 1, list.size()); 
        CanFrame f0 = list.get(0);
        Assert.assertEquals("header", toHexString(0x190a7123), toHexString(f0.getHeader()));
        compareContent(null, f0);
    }
    public void testVerifiedNodeIDNumberMessage() {
        
        Message m = new VerifiedNodeIDNumberMessage(source);
        MessageBuilder b = new MessageBuilder(map);
        
        List<OpenLcbCanFrame> list = b.processMessage(m);
        
        // looking for [190BF123] 01 02 03 04 05 06
        
        Assert.assertEquals("count", 1, list.size()); 
        CanFrame f0 = list.get(0);
        Assert.assertEquals("header", toHexString(0x190b7123), toHexString(f0.getHeader()));
        compareContent(source.getContents(), f0);
    }

    public void testProducerConsumerEventReportMessage() {
        
        Message m = new ProducerConsumerEventReportMessage(source, event);
        MessageBuilder b = new MessageBuilder(map);
        
        List<OpenLcbCanFrame> list = b.processMessage(m);
        
        // looking for [182df123] 11 12 13 14 15 16 17 18
        
        Assert.assertEquals("count", 1, list.size()); 
        CanFrame f0 = list.get(0);
        Assert.assertEquals("header", toHexString(0x182Df123), toHexString(f0.getHeader()));
        compareContent(event.getContents(), f0);
    }

    public void testDatagramMessageShort() {
        int[] data = new int[]{21,22,23};
        Message m = new DatagramMessage(source, destination, data);
        MessageBuilder b = new MessageBuilder(map);
        
        List<OpenLcbCanFrame> list = b.processMessage(m);
        
        Assert.assertEquals("count", 1, list.size()); 
        CanFrame f0 = list.get(0);

        Assert.assertEquals("header", toHexString(0x1D321123), toHexString(f0.getHeader()));
        compareContent(new byte[]{21,22,23}, f0);
    }

    public void testDatagramMessageEight() {
        int[] data = new int[]{21,22,23,24,25,26,27,28};
        Message m = new DatagramMessage(source, destination, data);
        MessageBuilder b = new MessageBuilder(map);
        
        List<OpenLcbCanFrame> list = b.processMessage(m);
        
        Assert.assertEquals("count", 1, list.size()); 
        CanFrame f0 = list.get(0);

        Assert.assertEquals("header", toHexString(0x1D321123), toHexString(f0.getHeader()));
        compareContent(new byte[]{21,22,23,24,25,26,27,28}, f0);
    }

    public void testDatagramMessageTwoFrame() {
        int[] data = new int[]{21,22,23,24,25,26,27,28, 
                               31,32,33,34,35,36,37,38};
                               
        Message m = new DatagramMessage(source, destination, data);
        MessageBuilder b = new MessageBuilder(map);
        
        List<OpenLcbCanFrame> list = b.processMessage(m);
        
        
        Assert.assertEquals("count", 2, list.size()); 

        CanFrame f0 = list.get(0);
        Assert.assertEquals("f0 header", toHexString(0x1C321123), toHexString(f0.getHeader()));
        compareContent(new byte[]{21,22,23,24,25,26,27,28}, f0);

        CanFrame f1 = list.get(1);
        Assert.assertEquals("f1 header", toHexString(0x1D321123), toHexString(f1.getHeader()));
        compareContent(new byte[]{31,32,33,34,35,36,37,38}, f1);
    }

    public void testDatagramMessageNine() {
        int[] data = new int[]{21,22,23,24,25,26,27,28, 
                               31};
                               
        Message m = new DatagramMessage(source, destination, data);
        MessageBuilder b = new MessageBuilder(map);
        
        List<OpenLcbCanFrame> list = b.processMessage(m);
        
        
        Assert.assertEquals("count", 2, list.size()); 

        CanFrame f0 = list.get(0);
        Assert.assertEquals("f0 header", toHexString(0x1C321123), toHexString(f0.getHeader()));
        compareContent(new byte[]{21,22,23,24,25,26,27,28}, f0);

        CanFrame f1 = list.get(1);
        Assert.assertEquals("f1 header", toHexString(0x1D321123), toHexString(f1.getHeader()));
        compareContent(new byte[]{31}, f1);
    }

    public void testInitializationCompleteFrame() {
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x19087123);
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(frame);
        
        Assert.assertEquals("count", 1, list.size()); 
        Message msg = list.get(0);
        
        Assert.assertTrue(msg instanceof InitializationCompleteMessage);        
    }
    
    public void testSingleFrameDatagram() {
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x1D321123);
        
        MessageBuilder b = new MessageBuilder(map);
        
        List<Message> list = b.processFrame(frame);
        
        Assert.assertEquals("count", 1, list.size()); 
        Message msg = list.get(0);
        
        Assert.assertTrue(msg instanceof DatagramMessage);        
    }
    
    public void testTwoFrameDatagram() {
        OpenLcbCanFrame frame = new OpenLcbCanFrame(0x123);
        frame.setHeader(0x1C321123);
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
    
    // from here down is testing infrastructure
    
    public MessageBuilderTest(String s) {
        super(s);
    }

    String toHexString(int n) {
        return Integer.toHexString(n);
    }
    
    void compareContent(byte[] data, CanFrame f) {
        if (data == null) 
            Assert.assertEquals("no data", 0, f.getNumDataElements());
        else {
            Assert.assertEquals("data length", data.length, f.getNumDataElements());
            for (int i=0; i<data.length; i++) {
                Assert.assertEquals("data byte "+i,data[i],f.getElement(i));
            }
        }
    }
    
    NodeID source = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID destination = new NodeID(new byte[]{6,5,4,3,2,1});
    EventID event = new EventID(new byte[]{11,12,13,14,15,16,17,18});
    AliasMap map = new AliasMap();
    
    public void setUp() {
        map.insert(0x0123, source);
        map.insert(0x321, destination);
    }
    
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MessageBuilderTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MessageBuilderTest.class);
        return suite;
    }
}
