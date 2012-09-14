package org.openlcb.implementations.throttle;

import org.openlcb.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class ThrottleSpeedDatagramTest extends TestCase {
    
    public void testZeroSpeed() {
        ThrottleSpeedDatagram t = new ThrottleSpeedDatagram(0.0);
        
        int[] content = t.getData();
        
        Assert.assertEquals(4, content.length);
        Assert.assertEquals(0x30, content[0]);
        Assert.assertEquals(0x01, content[1]);
        Assert.assertEquals(0x00, content[2]);
        Assert.assertEquals(0x00, content[3]);
        
    }
    
    public void test100Speed() {
        ThrottleSpeedDatagram t = new ThrottleSpeedDatagram(100.0);
        
        int[] content = t.getData();
        
        Assert.assertEquals(4, content.length);
        Assert.assertEquals(0x30, content[0]);
        Assert.assertEquals(0x01, content[1]);
        Assert.assertEquals(0x56, content[2]);
        Assert.assertEquals(0x40, content[3]);
        
    }

    public void testNeg100Speed() {
        ThrottleSpeedDatagram t = new ThrottleSpeedDatagram(-100.0);
        
        int[] content = t.getData();
        
        Assert.assertEquals(4, content.length);
        Assert.assertEquals(0x30, content[0]);
        Assert.assertEquals(0x01, content[1]);
        Assert.assertEquals(0xD6, content[2]);
        Assert.assertEquals(0x40, content[3]);
        
    }
    
    // from here down is testing infrastructure
    
    public ThrottleSpeedDatagramTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ThrottleSpeedDatagramTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ThrottleSpeedDatagramTest.class);
        return suite;
    }
}
