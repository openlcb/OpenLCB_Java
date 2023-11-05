package org.openlcb;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class ProducerConsumerEventReportMessageTest {
    boolean result;
    
    EventID eventID1 = new EventID(new byte[]{1,0,0,0,0,0,1,0});
    EventID eventID2 = new EventID(new byte[]{1,0,0,0,0,0,2,0});
    
    NodeID nodeID1 = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID nodeID2 = new NodeID(new byte[]{0,0,0,0,0,0});
 
    @Test   
    public void testEqualsSame() {
        Message m1 = new ProducerConsumerEventReportMessage(
                               nodeID1, eventID1 );
        Message m2 = new ProducerConsumerEventReportMessage(
                               nodeID1, eventID1 );
    
        Assert.assertTrue(m1.equals(m2));
    }

    @Test   
    public void testNotEqualsDifferentNode() {
        Message m1 = new ProducerConsumerEventReportMessage(
                                nodeID1, eventID1 );
        Message m2 = new ProducerConsumerEventReportMessage(
                                nodeID2, eventID1 );
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test   
    public void testNotEqualsDifferentEvent() {
        Message m1 = new ProducerConsumerEventReportMessage(
                                nodeID1, eventID1 );
        Message m2 = new ProducerConsumerEventReportMessage(
                                nodeID1, eventID2 );
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test   
    public void testPayloadList() {
        ProducerConsumerEventReportMessage m1 = new ProducerConsumerEventReportMessage(
                                nodeID1, eventID1 );

        Assert.assertEquals(0, m1.getPayloadSize());
        Assert.assertEquals(0, m1.getPayloadList().size()); // not null        
        
        java.util.List<Byte> payload1 = new java.util.ArrayList<Byte>();
        payload1.add((byte)12);
        ProducerConsumerEventReportMessage m2 = new ProducerConsumerEventReportMessage(
                                nodeID1, eventID1, payload1 );
        
        Assert.assertEquals(1, m2.getPayloadSize());
        Assert.assertEquals((byte)12, m2.getPayloadList().get(0));
        
        ProducerConsumerEventReportMessage m3 = new ProducerConsumerEventReportMessage(
                                nodeID1, eventID1, (java.util.List<Byte>) null ); // properly handle null
        
        Assert.assertEquals(0, m3.getPayloadSize());
        Assert.assertEquals(0, m3.getPayloadList().size()); // not null        

    }

    @Test   
    public void testPayloadArray() {
        ProducerConsumerEventReportMessage m1 = new ProducerConsumerEventReportMessage(
                                nodeID1, eventID1 );

        Assert.assertEquals(0, m1.getPayloadSize());
        Assert.assertEquals(0, m1.getPayloadArray().length); // not null        
        
        byte[] payload1 = new byte[]{12};
        ProducerConsumerEventReportMessage m2 = new ProducerConsumerEventReportMessage(
                                nodeID1, eventID1, payload1 );
        
        Assert.assertEquals(1, m2.getPayloadSize());
        Assert.assertEquals(12, m2.getPayloadArray()[0]);
    }

    @Test   
    public void testPayloadHashAndEquals() {
        ProducerConsumerEventReportMessage mNone = new ProducerConsumerEventReportMessage(
                                nodeID1, eventID1 );
        
        ProducerConsumerEventReportMessage mNull = new ProducerConsumerEventReportMessage(
                                nodeID1, eventID1, (java.util.List<Byte>) null ); // properly handle null
        
        java.util.List<Byte> payload0 = new java.util.ArrayList<Byte>();
        ProducerConsumerEventReportMessage mEmpty = new ProducerConsumerEventReportMessage(
                                nodeID1, eventID1, payload0 );

        Assert.assertEquals(mNone.hashCode(), mNull.hashCode());
        Assert.assertEquals(mNull.hashCode(), mEmpty.hashCode());
        Assert.assertEquals(mEmpty.hashCode(), mNone.hashCode());

        Assert.assertTrue(mNone.equals(mNull));
        Assert.assertTrue(mNone.equals(mEmpty));
        Assert.assertTrue(mNull.equals(mNone));
        Assert.assertTrue(mEmpty.equals(mNull));
        Assert.assertTrue(mNull.equals(mNone));
        Assert.assertTrue(mEmpty.equals(mNull));

        java.util.List<Byte> payload1 = new java.util.ArrayList<Byte>();
        payload1.add((byte)12);
        ProducerConsumerEventReportMessage mOne = new ProducerConsumerEventReportMessage(
                                nodeID1, eventID1, payload1 );

        java.util.List<Byte> payload2 = new java.util.ArrayList<Byte>();
        payload2.add((byte)13);
        ProducerConsumerEventReportMessage mAnother = new ProducerConsumerEventReportMessage(
                                nodeID1, eventID1, payload2 );
                                
        Assert.assertNotEquals(mOne.hashCode(), mNone.hashCode());
        Assert.assertNotEquals(mOne.hashCode(), mAnother.hashCode());
        
        Assert.assertFalse(mOne.equals(mAnother));

        Assert.assertFalse(mOne.equals(mNull));
        Assert.assertFalse(mOne.equals(mEmpty));
        Assert.assertFalse(mOne.equals(mNone));
        Assert.assertFalse(mNull.equals(mOne));
        Assert.assertFalse(mEmpty.equals(mOne));
        Assert.assertFalse(mNone.equals(mOne));

    }

    @Test   
    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new ProducerConsumerEventReportMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,0,0,0,0,0,3,0}) );
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }
    
}
