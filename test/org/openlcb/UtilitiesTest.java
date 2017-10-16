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

    public void testPackByteArray() {
        byte[] b = new byte[5];
        Utilities.HostToNetworkUint8(b, 2, 168);
        Assert.assertEquals("00 00 A8 00 00", Utilities.toHexSpaceString(b));
        Assert.assertEquals(168, Utilities.NetworkToHostUint8(b, 2));
        Utilities.HostToNetworkUint16(b, 1, 43766);
        Assert.assertEquals("00 AA F6 00 00", Utilities.toHexSpaceString(b));
        Assert.assertEquals(43766, Utilities.NetworkToHostUint16(b, 1));
        Utilities.HostToNetworkUint32(b, 1, 17);
        Assert.assertEquals("00 00 00 00 11", Utilities.toHexSpaceString(b));
        Assert.assertEquals(17, Utilities.NetworkToHostUint32(b, 1));
        Utilities.HostToNetworkUint32(b, 1, 3000000017L);
        Assert.assertEquals("00 B2 D0 5E 11", Utilities.toHexSpaceString(b));
        Assert.assertEquals(3000000017L, Utilities.NetworkToHostUint32(b, 1));

        b = new byte[6];
        Utilities.HostToNetworkUint48(b, 0, 0x0501010118DAL);
        Assert.assertEquals("05 01 01 01 18 DA", Utilities.toHexSpaceString(b));
        Assert.assertEquals(0x0501010118DAL, Utilities.NetworkToHostUint48(b, 0));
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
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(UtilitiesTest.class);
        return suite;
    }
}
