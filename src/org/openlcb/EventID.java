package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Common EventID implementation.
 * <p>
 * EventID objects are immutable once created.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class EventID {

    static final int BYTECOUNT = 8;
    
    @CheckReturnValue
    public EventID(@NonNull NodeID node, int b7, int b8) {
        this.contents = new byte[BYTECOUNT];
        System.arraycopy(node.contents, 0, this.contents, 0, BYTECOUNT-2);
            
        this.contents[6] = (byte)b7;
        this.contents[7] = (byte)b8;
    }
    
    @CheckReturnValue
    public EventID(@NonNull byte[] contents) {
        if (contents == null)
            throw new java.lang.IllegalArgumentException("null argument invalid");
        if (contents.length != BYTECOUNT)
            throw new java.lang.IllegalArgumentException("Wrong EventID length: "+contents.length);
        this.contents = new byte[BYTECOUNT];
        System.arraycopy(contents, 0, this.contents, 0, BYTECOUNT);
    }
    
    @CheckReturnValue
    public EventID(@NonNull String value) {
        if (value == null)
            throw new java.lang.IllegalArgumentException("null argument invalid");
        byte[] data = org.openlcb.Utilities.bytesFromHexString(value);
        if (data.length != BYTECOUNT)
            throw new java.lang.IllegalArgumentException("Wrong EventID length: "+data.length);
        this.contents = new byte[BYTECOUNT];
        System.arraycopy(data, 0, this.contents, 0, BYTECOUNT);
    }
    
    byte[] contents;
    
    @CheckReturnValue
    @NonNull
    public byte[] getContents() {
        // copy to ensure immutable
        byte[] retval = new byte[BYTECOUNT];
        System.arraycopy(contents, 0, retval, 0, BYTECOUNT);
        return retval;
    }

    @CheckReturnValue
    @Override
    public boolean equals(@NonNull Object o){
        // try to cast, else not equal
        try {
            EventID other = (EventID) o;
            for (int i = 0; i<BYTECOUNT; i++)
                if (other.contents[i] != this.contents[i]) return false;
            return true;
        } catch (Exception e) {
            return false;
        }
    }  

    @CheckReturnValue
    @Override
    public int hashCode() {
        return contents[0]<<21
            +contents[1]<<18
            +contents[2]<<15
            +contents[3]<<12
            +contents[4]<<9
            +contents[5]<<6
            +contents[6]<<3
            +contents[7];
    } 

    @CheckReturnValue
    @NonNull
    @Override
    public String toString() {
        return "EventID:"
                +Utilities.toHexPair(contents[0])+"."
                +Utilities.toHexPair(contents[1])+"."
                +Utilities.toHexPair(contents[2])+"."
                +Utilities.toHexPair(contents[3])+"."
                +Utilities.toHexPair(contents[4])+"."
                +Utilities.toHexPair(contents[5])+"."
                +Utilities.toHexPair(contents[6])+"."
                +Utilities.toHexPair(contents[7]);
    }
}
