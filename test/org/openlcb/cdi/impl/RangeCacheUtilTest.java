package org.openlcb.cdi.impl;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;
import org.openlcb.cdi.impl.RangeCacheUtil.Range;
import org.openlcb.cdi.jdom.JdomCdiRepTest;

/**
 * Created by bracz on 4/9/16.
 */
public class RangeCacheUtilTest extends TestCase {
    
    public void testToString() throws Exception {
        Range r1 = new Range(1,3);
        Assert.assertEquals("Range[1,3)", r1.toString());
    }

    public void testToEquals() throws Exception {
        Range r1 = new Range(1,3);
        Range r2 = new Range(2, 3);
        Assert.assertFalse(r1.equals(r2));
        Assert.assertFalse(r2.equals(r1));
        Range r3 = new Range(1, 4);
        Range r4 = new Range(1, 3);
        Assert.assertTrue(r1.equals(r4));
        Assert.assertTrue(r4.equals(r1));
        Assert.assertEquals("hashcodes equal when equal",r1.hashCode(),r4.hashCode());
        assertFalse(r3.equals(r4));
        assertFalse(r4.equals(r3));
    }


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
        Assert.assertEquals(new Range(0, 12), rng.get(0));
        Assert.assertEquals(new Range(23, 28), rng.get(1));
        util.addRange(16, 20);
        rng = util.getRanges();
        Assert.assertEquals(1, rng.size());
        Assert.assertEquals(new Range(0, 28), rng.get(0));
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RangeCacheUtilTest.class);

        return suite;
    }
}
