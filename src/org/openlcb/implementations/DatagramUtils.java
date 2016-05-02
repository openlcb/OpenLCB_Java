package org.openlcb.implementations;

/**
 * Created by bracz on 4/24/16.
 */
public class DatagramUtils {

    static Long parseLong(int[] payload, int offset) {
        long retval = 0;
        retval |= payload[offset++] & 0xff;
        retval <<= 8;
        retval |= payload[offset++] & 0xff;
        retval <<= 8;
        retval |= payload[offset++] & 0xff;
        retval <<= 8;
        retval |= payload[offset++] & 0xff;
        return retval;
    }

    static void renderLong(int[] payload, int offset, long value) {
        payload[offset++] = (int) ((value >> 24) & 0xff);
        payload[offset++] = (int) ((value >> 16) & 0xff);
        payload[offset++] = (int) ((value >> 8) & 0xff);
        payload[offset++] = (int) (value & 0xff);
    }

    static int parseErrorCode(int[] payload, int offset) {
        int retval = payload[offset++];
        retval <<= 8;
        retval |= payload[offset] & 0xff;
        return retval;
    }

    static void renderErrorCode(int[] payload, int offset, int errorCode) {
        payload[offset++] = (errorCode >> 8) & 0xff;
        payload[offset++] = errorCode & 0xff;
    }

    public static int byteToInt(byte b) {
        return b < 0 ? ((int)b) + 256 : b;
    }

    static void byteToIntArray(int[] dst, int iDst, byte[] src, int iSrc, int len) {
        for (int i = 0; i < len; ++i) {
            dst[i+iDst] = byteToInt(src[i+iSrc]);
        }
    }

    static byte intToByte(int b) {
        return (byte) b;
    }

    static void intToByteArray(byte[] dst, int iDst, int[] src, int iSrc, int len) {
        for (int i = 0; i < len; ++i) {
            dst[i+iDst] = intToByte(src[i + iSrc]);
        }
    }
}
