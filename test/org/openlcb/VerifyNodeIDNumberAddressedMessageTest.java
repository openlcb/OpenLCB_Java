package org.openlcb;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009, 2024
 */
public class VerifyNodeIDNumberAddressedMessageTest  {
    boolean result;
    
    @Test
    public void testEqualsSame() {
        Message m1 = new VerifyNodeIDNumberAddressedMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new NodeID(new byte[]{9,8,7,6,5,4}) );
        Message m2 = new VerifyNodeIDNumberAddressedMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new NodeID(new byte[]{9,8,7,6,5,4}) );
    
        Assert.assertTrue(m1.equals(m2));
    }

    @Test
    public void testEqualsSameWithContent() {
        Message m1 = new VerifyNodeIDNumberAddressedMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{9,8,7,6,5,4}),
                                            new NodeID(new byte[]{1,2,3,4,5,6}));
        Message m2 = new VerifyNodeIDNumberAddressedMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{9,8,7,6,5,4}),
                                            new NodeID(new byte[]{1,2,3,4,5,6}) );
    
        Assert.assertTrue(m1.equals(m2));
    }

    @Test
    public void testNotEqualsDifferent1() {
        Message m1 = new VerifyNodeIDNumberAddressedMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new NodeID(new byte[]{9,8,7,6,5,4}) );
        Message m2 = new VerifyNodeIDNumberAddressedMessage(
                                            new NodeID(new byte[]{10,2,3,4,5,6}),
                                            new NodeID(new byte[]{9,8,7,6,5,4}) );
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test
    public void testNotEqualsDifferent2() {
        Message m1 = new VerifyNodeIDNumberAddressedMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new NodeID(new byte[]{9,8,7,6,5,4}) );
        Message m2 = new VerifyNodeIDNumberAddressedMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new NodeID(new byte[]{19,8,7,6,5,4}) );
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test
    public void testEqualsContentMatters() {
        Message m1 = new VerifyNodeIDNumberAddressedMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{9,8,7,6,5,4}),
                                            new NodeID(new byte[]{1,2,3,4,5,6}) );
        Message m2 = new VerifyNodeIDNumberAddressedMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new NodeID(new byte[]{9,8,7,6,5,4}),
                                            new NodeID(new byte[]{1,2,3,4,5,0}) );
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test
    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleVerifyNodeIDNumberAddressed(VerifyNodeIDNumberAddressedMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new VerifyNodeIDNumberAddressedMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new NodeID(new byte[]{9,8,7,6,5,4}) );
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }
    
}
