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
}
