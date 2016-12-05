package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Common service methods (a library, not a class)
 * <p>
 * NodeID objects are immutable once created.
 *
 * @author  Bob Jacobsen   Copyright 2009, 2010, 2011 2012
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class Utilities {

    @CheckReturnValue
    @NonNull
    static public String toHexPair(int i) {
        String retval = "00"+Integer.toHexString(i).toUpperCase();
        return retval.substring(retval.length()-2);
    }

    @CheckReturnValue
    @NonNull
    static public String toHexSpaceString(int[] array) {
        StringBuffer buff = new StringBuffer();
        boolean first = true;
        for (int i = 0; i < array.length; i++) {
            if (!first) buff.append(" ");
            first = false;
            buff.append(Utilities.toHexPair(array[i]));
        }
        String retval = new String(buff);
        return retval;
    }

    @CheckReturnValue
    @NonNull
    static public String toHexSpaceString(byte[] array) {
        StringBuffer buff = new StringBuffer();
        boolean first = true;
        for (int i = 0; i < array.length; i++) {
            if (!first) buff.append(" ");
            first = false;
            buff.append(Utilities.toHexPair(array[i]));
        }
        String retval = new String(buff);
        return retval;
    }

    @CheckReturnValue
    @NonNull
    static public String toHexDotsString(int[] array) {
        StringBuffer buff = new StringBuffer();
        boolean first = true;
        for (int i = 0; i < array.length; i++) {
            if (!first) buff.append(".");
            first = false;
            buff.append(Utilities.toHexPair(array[i]));
        }
        String retval = new String(buff);
        return retval;
    }

    @CheckReturnValue
    @NonNull
    static public String toHexDotsString(byte[] array) {
        StringBuffer buff = new StringBuffer();
        boolean first = true;
        for (int i = 0; i < array.length; i++) {
            if (!first) buff.append(".");
            first = false;
            buff.append(Utilities.toHexPair(array[i]));
        }
        String retval = new String(buff);
        return retval;
    }

    /**
     * Create a byte[] from a String containing hexadecimal values.
     *
     * @param s String of hex values, ala "01 02 0A B1 21". Either spaces or dots
     * can be used as separators.
     * @return byte array, with one byte for each pair.  Can be zero length,
     *  but will not be null.
     */
    static public byte[] bytesFromHexString(String s) {
        String ts = s.replace("."," ")+"  "; // ensure blanks on end to make scan easier
        int len = 0;
        // scan for length
        for (int i= 0; i< s.length(); i++) {
            if (ts.charAt(i) != ' ')  {
                // need to process char for number. Is this a single digit?
                if (ts.charAt(i+1) != ' ') {
                    // 2 char value
                    i++;
                    len++;
                } else {
                    // 1 char value
                    len++;
                }
            }
        }
        byte[] b = new byte[len];
        // scan for content
        int saveAt = 0;
        for (int i= 0; i< s.length(); i++) {
            if (ts.charAt(i) != ' ')  {
                // need to process char for number. Is this a single digit?
                if (ts.charAt(i+1) != ' ') {
                    // 2 char value
                    String v = ""+ts.charAt(i)+ts.charAt(i+1);
                    b[saveAt] = (byte)Integer.valueOf(v,16).intValue();
                    i++;
                    saveAt++;
                } else {
                    // 1 char value
                    String v = ""+ts.charAt(i);
                    b[saveAt] = (byte)Integer.valueOf(v,16).intValue();
                    saveAt++;
                }
            }
        }
        return b;
    }

    static public int NetworkToHostUint8(byte[] arr, int offset) {
        if (arr == null || arr.length < offset) {
            return 0;
        }
        int r = arr[offset];
        r &= 0xff;
        return r;
    }

    static public void HostToNetworkUint8(byte[] arr, int offset, int value) {
        arr[offset] = (byte) (value & 0xff);
    }

    static public int NetworkToHostUint16(byte[] arr, int offset) {
        if (arr == null || arr.length < (offset+1)) {
            return 0;
        }
        return ((((int)arr[offset]) & 0xff) << 8) |
                (((int)arr[offset+1]) & 0xff);
    }

    static public void HostToNetworkUint16(byte[] arr, int offset, int value) {
        arr[offset] = (byte) ((value >> 8) & 0xff);
        arr[offset+1] = (byte) (value & 0xff);
    }

    static public long NetworkToHostUint32(byte[] arr, int offset) {
        if (arr == null || arr.length < (offset+3)) {
            return 0;
        }
        long ret = 0;
        ret |= ((int)arr[offset]) & 0xff;
        ret <<= 8;
        ret |= ((int)arr[offset+1]) & 0xff;
        ret <<= 8;
        ret |= ((int)arr[offset+2]) & 0xff;
        ret <<= 8;
        ret |= ((int)arr[offset+3]) & 0xff;
        return ret;
    }

    static public void HostToNetworkUint32(byte[] arr, int offset, long value) {
        arr[offset] = (byte) ((value >> 24) & 0xff);
        arr[offset+1] = (byte) ((value >> 16) & 0xff);
        arr[offset+2] = (byte) ((value >> 8) & 0xff);
        arr[offset+3] = (byte) ((value) & 0xff);
    }

    static public long NetworkToHostUint48(byte[] arr, int offset) {
        if (arr == null || arr.length < (offset+5)) {
            return 0;
        }
        long ret = 0;
        ret |= ((int)arr[offset]) & 0xff;
        ret <<= 8;
        ret |= ((int)arr[offset+1]) & 0xff;
        ret <<= 8;
        ret |= ((int)arr[offset+2]) & 0xff;
        ret <<= 8;
        ret |= ((int)arr[offset+3]) & 0xff;
        ret <<= 8;
        ret |= ((int)arr[offset+4]) & 0xff;
        ret <<= 8;
        ret |= ((int)arr[offset+5]) & 0xff;
        return ret;
    }

    static public void HostToNetworkUint48(byte[] arr, int offset, long value) {
        arr[offset] = (byte) ((value >> 40) & 0xff);
        arr[offset+1] = (byte) ((value >> 32) & 0xff);
        arr[offset+2] = (byte) ((value >> 24) & 0xff);
        arr[offset+3] = (byte) ((value >> 16) & 0xff);
        arr[offset+4] = (byte) ((value >> 8) & 0xff);
        arr[offset+5] = (byte) ((value) & 0xff);
    }

}
