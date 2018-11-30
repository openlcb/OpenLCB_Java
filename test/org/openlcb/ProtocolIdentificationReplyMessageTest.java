package org.openlcb;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class ProtocolIdentificationReplyMessageTest  {
    boolean result;
    
    NodeID nodeID1 = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID nodeID2 = new NodeID(new byte[]{0,0,0,0,0,0});

    @Test
    public void testEqualsSame() {
        Message m1 = new ProtocolIdentificationReplyMessage(
                               nodeID1, nodeID2,12);
        Message m2 = new ProtocolIdentificationReplyMessage(
                               nodeID1, nodeID2, 12);
    
        Assert.assertTrue(m1.equals(m2));
    }

    @Test
    public void testNotEqualsDifferentNode() {
        Message m1 = new ProtocolIdentificationReplyMessage(
                                nodeID1, nodeID2, 12);
        Message m2 = new ProtocolIdentificationReplyMessage(
                                nodeID2, nodeID2, 12);
    
        Assert.assertTrue( ! m1.equals(m2));
    }


    @Test
    public void testNotEqualsDifferentValue() {
        Message m1 = new ProtocolIdentificationReplyMessage(
                                nodeID1, nodeID2, 12);
        Message m2 = new ProtocolIdentificationReplyMessage(
                                nodeID1, nodeID2, 13);
    
        Assert.assertTrue( ! m1.equals(m2));
    }


    @Test
    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleProtocolIdentificationReply(ProtocolIdentificationReplyMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new ProtocolIdentificationReplyMessage(nodeID1, nodeID2, 21);
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }
    
}
