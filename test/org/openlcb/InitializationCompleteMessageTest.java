package org.openlcb;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class InitializationCompleteMessageTest  {
    boolean result;
 
    @Test   
    public void testEqualsSame() {
        Message m1 = new InitializationCompleteMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}) );
        Message m2 = new InitializationCompleteMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}) );
    
        Assert.assertTrue(m1.equals(m2));
    }

    @Test   
    public void testNotEqualsDifferent() {
        Message m1 = new InitializationCompleteMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}) );
        Message m2 = new InitializationCompleteMessage(
                                            new NodeID(new byte[]{1,3,3,4,5,6}) );
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test   
    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleInitializationComplete(InitializationCompleteMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new InitializationCompleteMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}) );
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }
    
}
