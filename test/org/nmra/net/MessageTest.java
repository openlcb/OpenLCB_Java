package org.nmra.net;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class MessageTest extends TestCase {

    public void testEqualsSame() {
        Message m1 = new Message(new NodeID(new byte[]{1,2,3,4,5,6}) );
        Message m2 = new Message(new NodeID(new byte[]{1,2,3,4,5,6}) );
    
        Assert.assertTrue(m1.equals(m2));
    }

    public void testNotEqualsDifferent() {
        Message m1 = new Message(new NodeID(new byte[]{1,2,3,4,5,6}) );
        Message m2 = new Message(new NodeID(new byte[]{1,3,3,4,5,6}) );
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    // from here down is testing infrastructure
    
    public MessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MessageTest.class);
        return suite;
    }
}
