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
        for (int i = 0; i < array.length; i++) {
            buff.append(" ");
            buff.append(Utilities.toHexPair(array[i]));
        }
        String retval = new String(buff);
        return retval.substring(1);
    }

    @CheckReturnValue
    @NonNull
    static public String toHexDotsString(int[] array) {
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            buff.append(".");
            buff.append(Utilities.toHexPair(array[i]));
        }
        String retval = new String(buff);
        return retval.substring(1);
    }

    @CheckReturnValue
    @NonNull
    static public String toHexDotsString(byte[] array) {
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            buff.append(".");
            buff.append(Utilities.toHexPair(array[i]));
        }
        String retval = new String(buff);
        return retval.substring(1);
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

}
