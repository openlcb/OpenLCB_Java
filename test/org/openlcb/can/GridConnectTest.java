package org.openlcb.can;

import org.junit.*;

import org.openlcb.Utilities;
import org.openlcb.implementations.DatagramUtils;

import java.util.List;

/**
 * Created by bracz on 1/8/16.
 */
public class GridConnectTest  {

    @Test
    public void testFormat() throws Exception {
        Assert.assertEquals(":X195B4123N01020304;", getCanonical(":X195B4123N01020304;"));
        Assert.assertEquals(":X195B4123N;", getCanonical(":X195B4123N;"));
        Assert.assertEquals(":X00000123N;", getCanonical(":X123N;"));
        Assert.assertEquals(":S123NFFFEFD80;", getCanonical(":S123NFFFEFD80;"));
        Assert.assertEquals(":X195B4123N0102030405060708;", getCanonical(":X195b4123N0102030405060708;"));
    }

    private void assertFrame(CanFrame f, int header, boolean isExtended, boolean isRtr, byte[]
            data) {
        Assert.assertEquals("isExtended", isExtended, f.isExtended());
        Assert.assertEquals("isRtr", isRtr, f.isRtr());
        Assert.assertEquals("header", header, f.getHeader());
        Assert.assertEquals(data.length, f.getNumDataElements());
        Assert.assertEquals(Utilities.toHexSpaceString(data), Utilities.toHexSpaceString(f.getData()));
        for (int i = 0; i < data.length; ++i) {
            Assert.assertEquals(DatagramUtils.byteToInt(data[i]), f.getElement(i));
        }
    }

    private void assertParse(String fmt, int header, boolean isExtended, boolean isRtr, byte[]
            data) {
        List<CanFrame> l = GridConnect.parse(fmt);
        Assert.assertEquals(1, l.size());
        CanFrame f = l.get(0);
        assertFrame(f, header, isExtended, isRtr, data);
    }

    private String getCanonical(String orig) {
        List<CanFrame> l = GridConnect.parse(orig);
        Assert.assertEquals(1, l.size());
        CanFrame f = l.get(0);
        return GridConnect.format(f);
    }

    @Test
    public void testParse() throws Exception {
        assertParse(":X195B4123N01020304;", 0x195b4123, true, false, new byte[]{1, 2, 3, 4});
        assertParse(":X195B4123N;", 0x195b4123, true, false, new byte[0]);
        assertParse(":S121Nffffff;", 0x121, false, false, new byte[]{(byte) 255, (byte) 255,
                (byte) 255});
        assertParse(":S121Nffffff;", 0x121, false, false, new byte[]{-1, -1, -1});
    }
}
