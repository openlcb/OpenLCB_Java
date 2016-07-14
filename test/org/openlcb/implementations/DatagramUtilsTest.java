package org.openlcb.implementations;

import junit.framework.TestCase;

/**
 * Created by bracz on 5/2/16.
 */
public class DatagramUtilsTest extends TestCase {

    private void assertArrayEquals(int[] expected, int[] real) {
        for (int i = 0; i < Math.min(expected.length, real.length); ++i) {
            assertEquals("entry " + i, expected[i], real[i]);
        }
        assertEquals("length ", expected.length, real.length);
    }
    public void testParseLong() throws Exception {
        int[] d = new int[]{5, 1, 2, 3, 0x82, 0x11, 0x22, 0x33, 6, 7};
        long l = DatagramUtils.parseLong(d, 4);
        assertEquals("parsed long", 0x82112233L, l);
        assertTrue(l >= 0);
    }

    public void testRenderLong() throws Exception {
        int[] d = new int[]{5, 1, 2, 3, 0, 0, 0, 0, 6, 7};
        DatagramUtils.renderLong(d, 4, 0x82112233L);
        assertArrayEquals(new int[]{5, 1, 2, 3, 0x82, 0x11, 0x22, 0x33, 6, 7}, d);
    }

    public void testParseErrorCode() throws Exception {
        int[] d = new int[]{5, 1, 2, 3, 0x10, 0x83};
        assertEquals(0x1083, DatagramUtils.parseErrorCode(d, 4));
        d = new int[]{5, 1, 2, 3, 0x95, 0x34, 55, 32};
        assertEquals(0x9534, DatagramUtils.parseErrorCode(d, 4));
    }

    public void testRenderErrorCode() throws Exception {
        int[] d = new int[]{5, 1, 0, 0, 99, 37};
        DatagramUtils.renderErrorCode(d, 2, 0x1093);
        assertArrayEquals(new int[]{5, 1, 0x10, 0x93, 99, 37}, d);
    }

    public void testByteToInt() throws Exception {
        assertEquals(0x80, DatagramUtils.byteToInt((byte)-128));
        assertEquals(00, DatagramUtils.byteToInt((byte)0));
        assertEquals(17, DatagramUtils.byteToInt((byte)17));
        assertEquals(0xff, DatagramUtils.byteToInt((byte)-1));
    }

    public void testByteToIntArray() throws Exception {
        assertEquals(-128, DatagramUtils.intToByte(0x80));
        assertEquals(0, DatagramUtils.intToByte(0));
        assertEquals(17, DatagramUtils.intToByte(17));
        assertEquals((byte)-1, DatagramUtils.intToByte(0xff));
    }

    public void testIntToByte() throws Exception {

    }
}