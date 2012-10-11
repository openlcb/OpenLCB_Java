package org.openlcb.implementations.throttle;

import org.openlcb.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * See http://en.wikipedia.org/wiki/Half-precision_floating-point_format
 *     http://www.mathworks.com/matlabcentral/fileexchange/23173
 *
 * 0 01111 0000000000 = 1
 * 1 10000 0000000000 = −2
 * 0 11110 1111111111 = 65504
 * 0 00000 0000000000 = 0
 * 1 00000 0000000000 = −0
 * 0 01101 0101010101 ≈ 0.33325... ≈ 1/3 
 *
 * 
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class Float16Test extends TestCase {
    Float16 f;
    
    public void testZeroAsBits() {
        f = new Float16(0.0f);
        Assert.assertEquals("zero", 0, f.getInt());
    }
    
    public void testNegZeroAsBits() {
        f = new Float16(0.0, false);
        Assert.assertEquals("neg zero", 0x8000, f.getInt());
    }
    
    public void testOneAsBits() {
        f = new Float16(1.0f);
        Assert.assertEquals("one", 0x3C00, f.getInt());
    }
    
    public void testTwoAsBits() {
        f = new Float16(2.0f);
        Assert.assertEquals("two", 0x4000, f.getInt());
    }
    
    public void testNegTwoAsBits() {
        f = new Float16(-2.0f);
        Assert.assertEquals("-two", 0xC000, f.getInt());
    }
    
    public void test100AsBits() {
        f = new Float16(100.0f);
        Assert.assertEquals("100", 0x5640, f.getInt());
    }
    
    public void test1p2AsBits() {
        f = new Float16(1.2001953f);
        Assert.assertEquals("1.2", 15565, f.getInt());
    }
    
    public void testMaxAsBits() {
        f = new Float16(65504.0f);
        Assert.assertEquals("65504", 0x7BFF, f.getInt());
    }
    
    public void testZeroAsFloat() {
        f = new Float16(0);
        Assert.assertEquals("zero", 0.0f, f.getFloat());
    }
    
    public void testOneAsFloat() {
        f = new Float16(0x3C00);
        Assert.assertEquals("one", 1.0f, f.getFloat());
    }
    
    public void testTwoAsFloat() {
        f = new Float16(0x4000);
        Assert.assertEquals("two", 2.0f, f.getFloat());
    }
    
    public void testNegTwoAsFloat() {
        f = new Float16(0xC000);
        Assert.assertEquals("-two", -2.0f, f.getFloat());
    }
    
    public void testMaxAsFloat() {
        f = new Float16(0x7BFF);
        Assert.assertEquals("65504", 65504.0f, f.getFloat());
    }

    public void test100AsFloat() {
        f = new Float16(0x5640);
        Assert.assertEquals("100", 100.0f, f.getFloat());
    }
    
    public void test1p2AsFloat() {
        f = new Float16(15565);
        Assert.assertEquals("1.2", 1.2001953f, f.getFloat());
    }
    
    // from here down is testing infrastructure
    
    public Float16Test(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {Float16Test.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(Float16Test.class);
        return suite;
    }
}
