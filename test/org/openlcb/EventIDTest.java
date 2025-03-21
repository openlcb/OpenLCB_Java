package org.openlcb;

import org.junit.Assert;
import org.junit.Test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class EventIDTest {
    @SuppressFBWarnings(value="NP_NONNULL_PARAM_VIOLATION",
            justification="Null passed for non null parameter")
    @Test
    public void testNullArg() {
        try {
            new EventID((byte[]) null);
        } catch (IllegalArgumentException e) {
            return;
        }
        Assert.fail("Should have thrown exception");
    }

    @Test
    public void testTooLongArg() {
        try {
            new EventID(new byte[]{1,2,3,4,5,6,7,8,9});
        } catch (IllegalArgumentException e) {
            return;
        }
        Assert.fail("Should have thrown exception");
    }

    @Test
    public void testTooShortArg() {
        try {
            new EventID(new byte[]{1,2,3,4,5,6,7});
        } catch (IllegalArgumentException e) {
            return;
        }
        Assert.fail("Should have thrown exception");
    }

    @Test
    public void testOKLengthArg() {
        EventID e = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        Assert.assertNotNull(e);
    }

    @Test
    public void testEqualsSame() {
        EventID e1 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        EventID e2 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        Assert.assertTrue(e1.equals(e2));
        Assert.assertEquals("hashcodes equal when equal",e1.hashCode(),e2.hashCode());
    }

    @Test
    public void testStringArgDotted() {
        EventID e1 = new EventID("1.2.3.4.5.6.7.8");
        EventID e2 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        Assert.assertTrue(e1.equals(e2));
    }

    @Test
    public void testStringArgSpaces() {
        EventID e1 = new EventID("1 2 3 4 5 6 7 8");
        EventID e2 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        Assert.assertTrue(e1.equals(e2));
    }

    @Test
    public void testAltCtor() {
        EventID e1 = new EventID(new byte[]{1,2,3,4,5,6,7,8});

        NodeID n = new NodeID(new byte[]{1,2,3,4,5,6});
        EventID e2 = new EventID(n, 7, 8);

        Assert.assertTrue(e1.equals(e2));
    }

    @Test
    public void testEqualsCastSame() {
        Object e1 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        EventID e2 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        Assert.assertTrue(e1.equals(e2));
        Assert.assertEquals("hashcodes equal when equal",e1.hashCode(),e2.hashCode());
    }

    @Test
    public void testEqualsSelf() {
        EventID e1 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        Assert.assertTrue(e1.equals(e1));
        Assert.assertEquals("hashcodes equal when equal",e1.hashCode(),e1.hashCode());
    }

    @Test
    public void testEqualsCastSelf() {
        EventID e1 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        Object e2 = e1;
        Assert.assertTrue(e1.equals(e2));
        Assert.assertEquals("hashcodes equal when equal",e1.hashCode(),e1.hashCode());
    }

    @Test
    public void testNotEquals() {
        EventID e1 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        EventID e2 = new EventID(new byte[]{1,3,3,4,5,6,7,8});
        Assert.assertTrue(!e1.equals(e2));
    }

    @Test
    public void testNotEqualsOtherType() {
        EventID e1 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        Object e2 = new Object();
        Assert.assertTrue(!e1.equals(e2));
    }

    @SuppressFBWarnings(value="EC_UNRELATED_TYPES",
            justification="Call to equals with unrelated types")
    @Test
    public void testNodesAreNotEvents() {
        EventID e1 = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        NodeID e2 = new NodeID(new byte[]{1,2,3,4,5,6});
        Assert.assertTrue(!e1.equals(e2));
    }

    @Test
    public void testOutputFormat() {
        EventID e1 = new EventID(new byte[]{0,0,1,0x10,0x13,0x0D,(byte)0xD0,(byte)0xAB});
        Assert.assertEquals(e1.toString(), "EventID:00.00.01.10.13.0D.D0.AB");
    }

    @Test
    public void testShortOutputFormat() {
        EventID e1 = new EventID(new byte[]{0,0,1,0x10,0x13,0x0D,(byte)0xD0,(byte)0xAB});
        Assert.assertEquals(e1.toShortString(), "00.00.01.10.13.0D.D0.AB");
    }

    @Test
    public void testToLong() {
        EventID e1 = new EventID(new byte[]{0,0,0,0,0,0,0,0});
        Assert.assertEquals(0L, e1.toLong());
        Assert.assertEquals(1L, new EventID(new byte[]{0,0,0,0,0,0,0,1}).toLong());
        Assert.assertEquals(256L, new EventID(new byte[]{0,0,0,0,0,0,1,0}).toLong());
        Assert.assertEquals(1L<<32, new EventID(new byte[]{0,0,0,1,0,0,0,0}).toLong());
        Assert.assertEquals(150L<<32, new EventID(new byte[]{0,0,0,(byte)150,0,0,0,0}).toLong());

        Assert.assertEquals(127L<<56, new EventID(new byte[]{127,0,0,0,0,0,0,0}).toLong());
        Assert.assertEquals(-1L, new EventID(new byte[]{(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,
                (byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff}).toLong());
        Assert.assertEquals(-2L, new EventID(new byte[]{(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,
                (byte)0xff,(byte)0xff,(byte)0xff,(byte)0xfe}).toLong());
    }
    
    @Test
    public void testFromLong() {
        EventID eid;
        eid = new EventID(0x12345678);
        Assert.assertEquals(0x12345678L, eid.toLong());
    }
    
    @Test
    public void testBoring() {
        EventID eid = new EventID("02.02.59.00.00.00.00.00");
        Assert.assertEquals("", eid.parse());
    }

    @Test
    public void testDefault() {
        EventID eid = new EventID("00.00.00.00.00.00.02.00");
        Assert.assertEquals("Reserved 00.00.00.00.00.00.02.00", eid.parse());
    }

    @Test
    public void testWellKnown() {
        EventID eid = new EventID("01.00.00.00.00.00.FF.FE");
        Assert.assertEquals("Clear Emergency Off", eid.parse());
    }

    @Test
    public void testFastClock() {
        EventID eid = new EventID("01.01.00.00.01.01.09.02");
        Assert.assertEquals("Fast Clock 1 time 9:02", eid.parse());
        eid = new EventID("01.01.00.00.01.01.09.32");
        Assert.assertEquals("Fast Clock 1 time 9:50", eid.parse());
        eid = new EventID("01.01.00.00.01.01.89.32");
        Assert.assertEquals("Fast Clock 1 Set time 9:50", eid.parse());
        eid = new EventID("01.01.00.00.01.01.F0.02");
        Assert.assertEquals("Fast Clock 1 Start", eid.parse());
    }

    @Test
    public void testRangeSuffix() {
        EventID eid = new EventID("00.00.00.00.00.00.FF.FF");
        Assert.assertEquals(0xFFFF, eid.rangeSuffix());
        eid = new EventID("00.00.00.00.00.FF.00.00");
        Assert.assertEquals(0xFFFF, eid.rangeSuffix());

        eid = new EventID("00.00.00.00.00.FF.80.00");
        Assert.assertEquals(0x7FFF, eid.rangeSuffix());

        eid = new EventID("00.00.00.00.00.00.00.03");
        Assert.assertEquals(0x03, eid.rangeSuffix());
    }

    @Test
    public void testTurnout() {
        EventID eid = new EventID("01.01.02.00.00.FF.00.09");
        Assert.assertEquals("DCC Acc Decoder 1 N/C/On  active", eid.parse());
        eid = new EventID("01.01.02.00.00.FF.00.08");
        Assert.assertEquals("DCC Acc Decoder 1 R/T/Off active", eid.parse());

        eid = new EventID("01.01.02.00.00.FF.00.0B");
        Assert.assertEquals("DCC Acc Decoder 2 N/C/On  active", eid.parse());
        eid = new EventID("01.01.02.00.00.FF.00.0A");
        Assert.assertEquals("DCC Acc Decoder 2 R/T/Off active", eid.parse());

        eid = new EventID("01.01.02.00.00.FF.00.0F");
        Assert.assertEquals("DCC Acc Decoder 4 N/C/On  active", eid.parse());
        eid = new EventID("01.01.02.00.00.FF.00.0E");
        Assert.assertEquals("DCC Acc Decoder 4 R/T/Off active", eid.parse());

        eid = new EventID("01.01.02.00.00.FF.00.17");
        Assert.assertEquals("DCC Acc Decoder 8 N/C/On  active", eid.parse());
        eid = new EventID("01.01.02.00.00.FF.00.16");
        Assert.assertEquals("DCC Acc Decoder 8 R/T/Off active", eid.parse());

        eid = new EventID("01.01.02.00.00.FF.00.27");
        Assert.assertEquals("DCC Acc Decoder 16 N/C/On  active", eid.parse());
        eid = new EventID("01.01.02.00.00.FF.00.26");
        Assert.assertEquals("DCC Acc Decoder 16 R/T/Off active", eid.parse());

        eid = new EventID("01.01.02.00.00.FF.00.47");
        Assert.assertEquals("DCC Acc Decoder 32 N/C/On  active", eid.parse());
        eid = new EventID("01.01.02.00.00.FF.00.46");
        Assert.assertEquals("DCC Acc Decoder 32 R/T/Off active", eid.parse());

        eid = new EventID("01.01.02.00.00.FF.00.87");
        Assert.assertEquals("DCC Acc Decoder 64 N/C/On  active", eid.parse());
        eid = new EventID("01.01.02.00.00.FF.00.86");
        Assert.assertEquals("DCC Acc Decoder 64 R/T/Off active", eid.parse());

        eid = new EventID("01.01.02.00.00.FF.01.07");
        Assert.assertEquals("DCC Acc Decoder 128 N/C/On  active", eid.parse());
        eid = new EventID("01.01.02.00.00.FF.01.06");
        Assert.assertEquals("DCC Acc Decoder 128 R/T/Off active", eid.parse());

        eid = new EventID("01.01.02.00.00.FF.02.07");
        Assert.assertEquals("DCC Acc Decoder 256 N/C/On  active", eid.parse());
        eid = new EventID("01.01.02.00.00.FF.02.06");
        Assert.assertEquals("DCC Acc Decoder 256 R/T/Off active", eid.parse());

        eid = new EventID("01.01.02.00.00.FF.04.07");
        Assert.assertEquals("DCC Acc Decoder 512 N/C/On  active", eid.parse());
        eid = new EventID("01.01.02.00.00.FF.04.06");
        Assert.assertEquals("DCC Acc Decoder 512 R/T/Off active", eid.parse());

        eid = new EventID("01.01.02.00.00.FF.08.07");
        Assert.assertEquals("DCC Acc Decoder 1024 N/C/On  active", eid.parse());
        eid = new EventID("01.01.02.00.00.FF.08.06");
        Assert.assertEquals("DCC Acc Decoder 1024 R/T/Off active", eid.parse());

        eid = new EventID("01.01.02.00.00.FF.00.05");
        Assert.assertEquals("DCC Acc Decoder 2047 N/C/On  active", eid.parse());
        eid = new EventID("01.01.02.00.00.FF.00.04");
        Assert.assertEquals("DCC Acc Decoder 2047 R/T/Off active", eid.parse());
    }
}
