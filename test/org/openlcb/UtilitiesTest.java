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

    public void testArray2() {
        Assert.assertEquals("0A.0B.12", Utilities.toHexDotsString(new int[]{0x0A, 0x0B, 0x12}));
    }

    public void testSpaceByteArrayZeroLen() {
        Assert.assertEquals("", Utilities.toHexSpaceString(new int[]{}));
    }
    public void testSpaceIntArrayZeroLen() {
        Assert.assertEquals("", Utilities.toHexSpaceString(new byte[]{}));
    }

    public void testDotByteArrayZeroLen() {
        Assert.assertEquals("", Utilities.toHexDotsString(new int[]{}));
    }
    public void testDotIntArrayZeroLen() {
        Assert.assertEquals("", Utilities.toHexDotsString(new byte[]{}));
    }

    public void testToByteArray() {
        Assert.assertTrue(compareArrays(new byte[]{0xA, 0xB, 0x12}, Utilities.bytesFromHexString("0A 0B 12")));
        Assert.assertTrue(compareArrays(new byte[]{0xA, 0xB, 0x12}, Utilities.bytesFromHexString("0A.0B.12")));
    }
    
    boolean compareArrays(byte[] a, byte[]b) {
        if (a == null && b == null) return true;
        if (a.length != b.length) return false;
        for (int i = 0; i <a.length; i++) if (a[i]!=b[i]) return false;
        return true;
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
