package org.openlcb.implementations;

import org.junit.*;

/**
 * Created by bracz on 5/2/16.
 */
public class DatagramUtilsTest {

    private void assertArrayEquals(int[] expected, int[] real) {
        for (int i = 0; i < Math.min(expected.length, real.length); ++i) {
            Assert.assertEquals("entry " + i, expected[i], real[i]);
        }
        Assert.assertEquals("length ", expected.length, real.length);
    }

    @Test
    public void testParseLong() throws Exception {
        int[] d = new int[]{5, 1, 2, 3, 0x82, 0x11, 0x22, 0x33, 6, 7};
        long l = DatagramUtils.parseLong(d, 4);
        Assert.assertEquals("parsed long", 0x82112233L, l);
        Assert.assertTrue(l >= 0);
    }

    @Test
    public void testRenderLong() throws Exception {
        int[] d = new int[]{5, 1, 2, 3, 0, 0, 0, 0, 6, 7};
        DatagramUtils.renderLong(d, 4, 0x82112233L);
        assertArrayEquals(new int[]{5, 1, 2, 3, 0x82, 0x11, 0x22, 0x33, 6, 7}, d);
    }

    @Test
    public void testParseErrorCode() throws Exception {
        int[] d = new int[]{5, 1, 2, 3, 0x10, 0x83};
        Assert.assertEquals(0x1083, DatagramUtils.parseErrorCode(d, 4));
        d = new int[]{5, 1, 2, 3, 0x95, 0x34, 55, 32};
        Assert.assertEquals(0x9534, DatagramUtils.parseErrorCode(d, 4));
    }

    @Test
    public void testRenderErrorCode() throws Exception {
        int[] d = new int[]{5, 1, 0, 0, 99, 37};
        DatagramUtils.renderErrorCode(d, 2, 0x1093);
        assertArrayEquals(new int[]{5, 1, 0x10, 0x93, 99, 37}, d);
    }

    @Test
    public void testByteToInt() throws Exception {
        Assert.assertEquals(0x80, DatagramUtils.byteToInt((byte)-128));
        Assert.assertEquals(00, DatagramUtils.byteToInt((byte)0));
        Assert.assertEquals(17, DatagramUtils.byteToInt((byte)17));
        Assert.assertEquals(0xff, DatagramUtils.byteToInt((byte)-1));
    }

    @Test
    public void testByteToIntArray() throws Exception {
        Assert.assertEquals(-128, DatagramUtils.intToByte(0x80));
        Assert.assertEquals(0, DatagramUtils.intToByte(0));
        Assert.assertEquals(17, DatagramUtils.intToByte(17));
        Assert.assertEquals((byte)-1, DatagramUtils.intToByte(0xff));
    }

    @Test
    @Ignore("empty test")
    public void testIntToByte() throws Exception {

    }
}
