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
        Message m1 = new IdentifyEventsAddressedMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
        Message m2 = new IdentifyEventsAddressedMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
    
        Assert.assertTrue(m1.equals(m2));
    }

    @Test    
    public void testEqualsSelf() {
        Message m1 = new IdentifyEventsAddressedMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
    
        Assert.assertTrue(m1.equals(m1));
    }

    @Test    
    public void testNotEqualsDifferentSrc() {
        Message m1 = new IdentifyEventsAddressedMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
        Message m2 = new IdentifyEventsAddressedMessage(
                                            new NodeID(new byte[]{1,3,3,4,5,6}), 
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test    
    public void testNotEqualsDifferentDest() {
        Message m1 = new IdentifyEventsAddressedMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
        Message m2 = new IdentifyEventsAddressedMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{7,10,9,10,11,12}) );
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test
    public void testGlobalEquals() {
        Message m1 = new IdentifyEventsGlobalMessage(
                new NodeID(new byte[]{1,2,3,4,5,6}));
        Message m2 = new IdentifyEventsGlobalMessage(
                new NodeID(new byte[]{7,8,9,10,11,12}));
        Message m3 = new IdentifyEventsGlobalMessage(
                new NodeID(new byte[]{1,2,3,4,5,6}));

        Assert.assertTrue(m1.equals(m3));
        Assert.assertFalse(m1.equals(m2));
        Assert.assertTrue(m1.equals(m1));
    }

    @Test    
    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleIdentifyEventsAddressed(IdentifyEventsAddressedMessage msg,
                                                      Connection sender){
                result = true;
            }
        };
        Message m = new IdentifyEventsAddressedMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }

    @Test
    public void testHandlingGlobal() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleIdentifyEventsGlobal(IdentifyEventsGlobalMessage msg,
                                                   Connection sender){
                result = true;
            }
        };
        Message m = new IdentifyEventsGlobalMessage(
                new NodeID(new byte[]{1,2,3,4,5,6}));

        n.put(m, null);

        Assert.assertTrue(result);
    }
}
