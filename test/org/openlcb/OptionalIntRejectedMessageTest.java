package org.openlcb;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2012
 */
public class OptionalIntRejectedMessageTest {
    boolean result;
    
    NodeID nodeID1 = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID nodeID2 = new NodeID(new byte[]{0,0,0,0,0,0});

    @Test
    public void testEqualsSame() {
        Message m1 = new OptionalIntRejectedMessage(
                               nodeID1,nodeID2,0,1);
        Message m2 = new OptionalIntRejectedMessage(
                               nodeID1,nodeID2,0,1);
    
        Assert.assertTrue(m1.equals(m2));
    }

    @Test
    public void testNotEqualsDifferentSrcNode() {
        Message m1 = new OptionalIntRejectedMessage(
                                nodeID1,nodeID2,0,1);
        Message m2 = new OptionalIntRejectedMessage(
                                nodeID2,nodeID2,0,1);
    
        Assert.assertTrue( ! m1.equals(m2));
    }
    
    @Test
    public void testNotEqualsDifferentDstNode() {
        Message m1 = new OptionalIntRejectedMessage(
                                nodeID1,nodeID2,0,1);
        Message m2 = new OptionalIntRejectedMessage(
                                nodeID1,nodeID1,0,1);
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test
    public void testNotEqualsDifferentCode() {
        Message m1 = new OptionalIntRejectedMessage(
                                nodeID1,nodeID2,0,1);
        Message m2 = new OptionalIntRejectedMessage(
                                nodeID1,nodeID2,0,2);
    
        Assert.assertTrue( ! m1.equals(m2));
    }
    
    @Test
    public void testNotEqualsDifferentMti() {
        Message m1 = new OptionalIntRejectedMessage(
                                nodeID1,nodeID2,0,1);
        Message m2 = new OptionalIntRejectedMessage(
                                nodeID1,nodeID2,2,1);
    
        Assert.assertTrue( ! m1.equals(m2));
    }
    
    @Test
    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleOptionalIntRejected(OptionalIntRejectedMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new OptionalIntRejectedMessage(nodeID1, nodeID2, 0, 1);
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }
    
}
