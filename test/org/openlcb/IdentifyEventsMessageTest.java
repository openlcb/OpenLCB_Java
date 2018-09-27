package org.openlcb;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class IdentifyEventsMessageTest {
    boolean result;

    @Test    
    public void testEqualsSame() {
        Message m1 = new IdentifyEventsMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
        Message m2 = new IdentifyEventsMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
    
        Assert.assertTrue(m1.equals(m2));
    }

    @Test    
    public void testEqualsSelf() {
        Message m1 = new IdentifyEventsMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
    
        Assert.assertTrue(m1.equals(m1));
    }

    @Test    
    public void testNotEqualsDifferentSrc() {
        Message m1 = new IdentifyEventsMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
        Message m2 = new IdentifyEventsMessage(
                                            new NodeID(new byte[]{1,3,3,4,5,6}), 
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test    
    public void testNotEqualsDifferentDest() {
        Message m1 = new IdentifyEventsMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
        Message m2 = new IdentifyEventsMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{7,10,9,10,11,12}) );
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test    
    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleIdentifyEvents(IdentifyEventsMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new IdentifyEventsMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }
}
