package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class MessageTypeIdentifierTest extends TestCase {

    public void testEqualsSame() {
        Message m1 = new Message(new NodeID(new byte[]{1,2,3,4,5,6}) )
            {public int getMTI() {return 0; }};
        Message m2 = new Message(new NodeID(new byte[]{1,2,3,4,5,6}) )
            {public int getMTI() {return 0; }};
    
        Assert.assertTrue(m1.equals(m2));
    }

    public void testNotEqualsDifferent() {
        Message m1 = new Message(new NodeID(new byte[]{1,2,3,4,5,6}) )
            {public int getMTI() {return 0; }};
        Message m2 = new Message(new NodeID(new byte[]{1,3,3,4,5,6}) )
            {public int getMTI() {return 0; }};
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    // from here down is testing infrastructure
    
    public MessageTypeIdentifierTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MessageTypeIdentifierTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MessageTypeIdentifierTest.class);
        return suite;
    }
}
