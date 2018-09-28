package org.openlcb;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class IdentifyConsumersMessageTest  {
    boolean result;

    @Test    
    public void testEqualsSame() {
        Message m1 = new IdentifyConsumersMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));

        Message m2 = new IdentifyConsumersMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
    
        Assert.assertTrue(m1.equals(m2));
    }

    @Test
    public void testNotEqualsDifferentNode() {
        Message m1 = new IdentifyConsumersMessage(
                                            new NodeID(new byte[]{99,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));

        Message m2 = new IdentifyConsumersMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test
    public void testNotEqualsDifferentEvent() {
        Message m1 = new IdentifyConsumersMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{99,2,3,4,5,6,7,8}));

        Message m2 = new IdentifyConsumersMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test
    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleIdentifyConsumers(IdentifyConsumersMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new IdentifyConsumersMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }
}
