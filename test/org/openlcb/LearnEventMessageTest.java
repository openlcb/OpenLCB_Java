package org.openlcb;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class LearnEventMessageTest {
   
    @Test	
    public void testEqualsSame() {
        Message m1 = new LearnEventMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
        Message m2 = new LearnEventMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
    
        Assert.assertTrue(m1.equals(m2));
    }

    @Test	
    public void testNotEqualsDifferentNode() {
        Message m1 = new LearnEventMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
        Message m2 = new LearnEventMessage(
                                            new NodeID(new byte[]{99,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test	
    public void testNotEqualsDifferentEvent() {
        Message m1 = new LearnEventMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
        Message m2 = new LearnEventMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{99,2,3,4,5,6,7,8}));
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    private boolean result;
    @Test	
    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleLearnEvent(LearnEventMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new LearnEventMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{99,2,3,4,5,6,7,8}));
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }
    
}
