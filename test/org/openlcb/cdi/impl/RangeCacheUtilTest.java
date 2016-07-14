package org.openlcb.cdi.impl;

import junit.framework.TestCase;

import java.util.List;
import org.openlcb.cdi.impl.RangeCacheUtil.Range;
/**
 * Created by bracz on 4/9/16.
 */
public class RangeCacheUtilTest extends TestCase {

    public void testToStringEquals() throws Exception {
        Range r1 = new Range(1,3);
        assertEquals("Range[1,3)", r1.toString());
        Range r2 = new Range(2, 3);
        assertFalse(r1.equals(r2));
        assertFalse(r2.equals(r1));
        Range r3 = new Range(1, 4);
        Range r4 = new Range(1, 3);
        assertTrue(r1.equals(r4));
        assertTrue(r4.equals(r1));
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
        assertEquals(2, rng.size());
        assertEquals(new Range(0, 12), rng.get(0));
        assertEquals(new Range(23, 28), rng.get(1));
        util.addRange(16, 20);
        rng = util.getRanges();
        assertEquals(1, rng.size());
        assertEquals(new Range(0, 28), rng.get(0));
    }
}