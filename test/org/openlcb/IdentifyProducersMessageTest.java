package org.openlcb;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class IdentifyProducersMessageTest {
    boolean result;
   
    @Test 
    public void testEqualsSame() {
        Message m1 = new IdentifyProducersMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
        Message m2 = new IdentifyProducersMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
    
        Assert.assertTrue(m1.equals(m2));
    }

    @Test 
    public void testNotEqualsDifferentNode() {
        Message m1 = new IdentifyProducersMessage(
                                            new NodeID(new byte[]{99,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
        Message m2 = new IdentifyProducersMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test 
    public void testNotEqualsDifferentEvent() {
        Message m1 = new IdentifyProducersMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
        Message m2 = new IdentifyProducersMessage(
                                            new NodeID(new byte[]{99,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test 
    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleIdentifyProducers(IdentifyProducersMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new IdentifyProducersMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }
    
}
