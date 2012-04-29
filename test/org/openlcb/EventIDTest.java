package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class EventIDTest extends TestCase {
    public void testNullArg() {
        try {
            new EventID((byte[])null);
        } catch (IllegalArgumentException e) { return; }
        Assert.fail("Should have thrown exception");
    }

    public void testTooLongArg() {
        try {
            new EventID(new byte[]{1,2,3,4,5,6,7,8,9});
        } catch (IllegalArgumentException e) { return; }
        Assert.fail("Should have thrown exception");
    }

    public void testTooShortArg() {
        try {
            new EventID(new byte[]{1,2,3,4,5,6,7});
        } catch (IllegalArgumentException e) { return; }
        Assert.fail("Should have thrown exception");
    }
    
    public void testOKLengthArg() {
        new EventID(new byte[]{1,2,3,4,5,6,7,8});
    }
    
    public void testEqualsSame() {
        EventID e1 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        EventID e2 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        Assert.assertTrue(e1.equals(e2));
    }
    
    public void testAltCtor() {
        EventID e1 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        
        NodeID n = new NodeID(new byte[]{1,2,3,4,5,6});
        EventID e2 = new EventID(n, 7, 8);

        Assert.assertTrue(e1.equals(e2));
    }
    
    public void testEqualsCastSame() {
        Object e1 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        EventID e2 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        Assert.assertTrue(e1.equals(e2));
    }
    
    public void testEqualsSelf() {
        EventID e1 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        Assert.assertTrue(e1.equals(e1));
    }
    
    public void testEqualsCastSelf() {
        EventID e1 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        Object e2 = e1;
        Assert.assertTrue(e1.equals(e2));
    }
    
    public void testNotEquals() {
        EventID e1 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        EventID e2 = new EventID(new byte[]{1,3,3,4,5,6,7,8});
        Assert.assertTrue(!e1.equals(e2));
    }
    
    public void testNotEqualsOtherType() {
        EventID e1 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        Object e2 = new Object();
        Assert.assertTrue(!e1.equals(e2));
    }

    public void testNodesAreNotEvents() {
        EventID e1 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        NodeID e2 = new NodeID(new byte[]{1,2,3,4,5,6});
        Assert.assertTrue(!e1.equals(e2));
    }

    public void testOutputFormat() {
        EventID e1 = new EventID(new byte[]{0,0,1,0x10,0x13,0x0D,(byte)0xD0,(byte)0xAB});
        Assert.assertEquals(e1.toString(), "EventID:00.00.01.10.13.0D.D0.AB");
    }

    // from here down is testing infrastructure
    
    public EventIDTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {EventIDTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EventIDTest.class);
        return suite;
    }
}
