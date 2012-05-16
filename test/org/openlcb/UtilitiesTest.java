package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class UtilitiesTest extends TestCase {

    public void testZeroString() {
        Assert.assertEquals("00", Utilities.toHexPair(0));
    }

    public void testHexAE() {
        Assert.assertEquals("AE", Utilities.toHexPair(0xAE));
    }

    public void testHex0A() {
        Assert.assertEquals("0A", Utilities.toHexPair(0xA));
    }

    public void testArray1() {
        Assert.assertEquals("0A 0B 12", Utilities.toHexSpaceString(new int[]{0x0A, 0x0B, 0x12}));
    }

   // from here down is testing infrastructure
    
    public UtilitiesTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {UtilitiesTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(UtilitiesTest.class);
        return suite;
    }
}
