package org.openlcb;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class MessageTest {

    @Test
    public void testEqualsSame() {
        Message m1 = new Message(new NodeID(new byte[]{1,2,3,4,5,6}) )
            {public int getMTI() {return 0; }};
        Message m2 = new Message(new NodeID(new byte[]{1,2,3,4,5,6}) )
            {public int getMTI() {return 0; }};
    
        Assert.assertTrue(m1.equals(m2));
    }

    @Test
    public void testNotEqualsDifferent() {
        Message m1 = new Message(new NodeID(new byte[]{1,2,3,4,5,6}) )
            {public int getMTI() {return 0; }};
        Message m2 = new Message(new NodeID(new byte[]{1,3,3,4,5,6}) )
            {public int getMTI() {return 0; }};
    
        Assert.assertTrue( ! m1.equals(m2));
    }
}
