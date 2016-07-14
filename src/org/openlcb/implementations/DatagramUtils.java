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
}
