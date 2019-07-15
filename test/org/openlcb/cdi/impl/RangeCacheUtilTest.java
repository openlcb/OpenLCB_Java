package org.openlcb.cdi.impl;

import org.junit.*;

import java.util.List;
import org.openlcb.cdi.impl.RangeCacheUtil.Range;

/**
 * Created by bracz on 4/9/16.
 */
public class RangeCacheUtilTest {
    
    @Test
    public void testToString() throws Exception {
        Range r1 = new Range(1,3, false);
        Assert.assertEquals("Range[1,3)", r1.toString());
        r1 = new Range(1,3, true);
        Assert.assertEquals("NRange[1,3)", r1.toString());
    }

    @Test
    public void testToEquals() throws Exception {
        Range r1 = new Range(1,3, false);
        Range r2 = new Range(2, 3, false);
        Range r2b = new Range(2, 3, true);
        Assert.assertFalse(r1.equals(r2));
        Assert.assertFalse(r2.equals(r1));
        Assert.assertFalse(r2b.equals(r1));
        Range r3 = new Range(1, 4, false);
        Range r4 = new Range(1, 3, false);
        Range r5 = new Range(2, 3, true);
        Assert.assertTrue(r1.equals(r4));
        Assert.assertTrue(r4.equals(r1));
        Assert.assertTrue(r5.equals(r2b));
        Assert.assertEquals("hashcodes equal when equal",r1.hashCode(),r4.hashCode());
        Assert.assertFalse(r3.equals(r4));
        Assert.assertFalse(r4.equals(r3));
    }

    @Test
    public void testGetRanges() throws Exception {
        RangeCacheUtil util = new RangeCacheUtil();
        util.addRange(10, 12);
        util.addRange(23, 24);
        util.addRange(0, 4);
        util.addRange(0, 1);
        util.addRange(6, 7);
        util.addRange(26, 28);
        List<Range> rng = util.getRanges();
        Assert.assertEquals(2, rng.size());
        Assert.assertEquals(new Range(0, 12, false), rng.get(0));
        Assert.assertEquals(new Range(23, 28, false), rng.get(1));
        util.addRange(16, 20);
        rng = util.getRanges();
        Assert.assertEquals(1, rng.size());
        Assert.assertEquals(new Range(0, 28, false), rng.get(0));
    }

    @Test
    public void testSimplifyWithNullTermination() throws Exception {
        RangeCacheUtil util = new RangeCacheUtil();
        util.addRange(3, 4);
        util.addRange(1, 2);
        util.addRange(10, 15);
        util.addRange(15, 20, true);
        util.addRange(20, 25);
        util.addRange(25, 26);
        util.addRange(2, 3);
        List<Range> rng = util.getRanges();
        Assert.assertEquals(3, rng.size());
        // First three get merged because the gap is small enough.
        Assert.assertEquals(new Range(1, 15, false), rng.get(0));
        Assert.assertEquals(new Range(15, 20, true), rng.get(1));
        Assert.assertEquals(new Range(20, 26, false), rng.get(2));
    }
}
