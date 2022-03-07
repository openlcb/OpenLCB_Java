package org.openlcb;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class UtilitiesTest  {
    @Test
    public void testZeroString() {
        Assert.assertEquals("00", Utilities.toHexPair(0));
    }

    @Test
    public void testHexAE() {
        Assert.assertEquals("AE", Utilities.toHexPair(0xAE));
    }

    @Test
    public void testHex0A() {
        Assert.assertEquals("0A", Utilities.toHexPair(0xA));
    }

    @Test
    public void testArray1() {
        Assert.assertEquals("0A 0B 12", Utilities.toHexSpaceString(new int[]{0x0A, 0x0B, 0x12}));
    }

    @Test
    public void testArray2() {
        Assert.assertEquals("0A.0B.12", Utilities.toHexDotsString(new int[]{0x0A, 0x0B, 0x12}));
    }

    @Test
    public void testSpaceByteArrayZeroLen() {
        Assert.assertEquals("", Utilities.toHexSpaceString(new int[]{}));
    }

    @Test
    public void testSpaceIntArrayZeroLen() {
        Assert.assertEquals("", Utilities.toHexSpaceString(new byte[]{}));
    }

    @Test
    public void testDotByteArrayZeroLen() {
        Assert.assertEquals("", Utilities.toHexDotsString(new int[]{}));
    }

    @Test
    public void testDotIntArrayZeroLen() {
        Assert.assertEquals("", Utilities.toHexDotsString(new byte[]{}));
    }

    @Test
    public void testToByteArray() {
        Assert.assertTrue(compareArrays(new byte[]{0xA, 0xB, 0x12}, Utilities.bytesFromHexString("0A 0B 12")));
        Assert.assertTrue(compareArrays(new byte[]{0xA, 0xB, 0x12}, Utilities.bytesFromHexString("0A.0B.12")));
    }

    @Test
    public void testPackByteArray() {
        byte[] b = new byte[5];
        Utilities.HostToNetworkUint8(b, 2, 168);
        Assert.assertEquals("00 00 A8 00 00", Utilities.toHexSpaceString(b));
        Assert.assertEquals(168, Utilities.NetworkToHostUint8(b, 2));

        Utilities.HostToNetworkUint16(b, 1, 43766);
        Assert.assertEquals("00 AA F6 00 00", Utilities.toHexSpaceString(b));
        Assert.assertEquals(43766, Utilities.NetworkToHostUint16(b, 1));

        Utilities.HostToNetworkUint24(b, 1, 12298956);
        Assert.assertEquals("00 BB AA CC 00", Utilities.toHexSpaceString(b));
        Assert.assertEquals(12298956, Utilities.NetworkToHostUint24(b, 1));

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

        Utilities.HostToNetworkUint48(b, 0, 0xDDEEFFAABBCCL);
        Assert.assertEquals("DD EE FF AA BB CC", Utilities.toHexSpaceString(b));
        Assert.assertEquals(0xDDEEFFAABBCCL, Utilities.NetworkToHostUint48(b, 0));

        // These have their offfset out of bounds.
        Assert.assertEquals(0, Utilities.NetworkToHostUint48(b, 1));
        Assert.assertEquals(0, Utilities.NetworkToHostUint32(b, 3));
        Assert.assertEquals(0, Utilities.NetworkToHostUint24(b, 4));
        Assert.assertEquals(0, Utilities.NetworkToHostUint16(b, 5));
        Assert.assertEquals(0, Utilities.NetworkToHostUint8(b, 6));
    }

    boolean compareArrays(byte[] a, byte[]b) {
        if ((a == null) && (b == null)) {
            return true;
        }
        if ((a != null) && (b != null)) {
            if (a.length != b.length) {
                return false;
            }
            for (int i = 0; i <a.length; i++) {
                if (a[i]!=b[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
