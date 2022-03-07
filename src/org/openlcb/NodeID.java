package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Common NodeID implementation
 * <p>
 * NodeID objects are immutable once created.
 *
 * @author  Bob Jacobsen   Copyright 2009, 2010, 2011
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class NodeID {
    static final int BYTECOUNT = 6;
    
    @CheckReturnValue
    public NodeID(@NonNull NodeID node) {
        this (node.contents);
    }
    
    @CheckReturnValue
    public NodeID() {
        this (new byte[]{0,0,0,0,0,0});
    }
    
    @CheckReturnValue
    public NodeID(@NonNull byte[] contents) {
        if (contents == null)
            throw new java.lang.IllegalArgumentException("null argument invalid");
        if (contents.length < BYTECOUNT)
            throw new java.lang.IllegalArgumentException("Wrong NodeID length: "+contents.length);
        this.contents = new byte[BYTECOUNT];
        for (int i = 0; i < BYTECOUNT; i++)
            this.contents[i] = contents[i];
    }
    
    @CheckReturnValue
    public NodeID(@NonNull String value) {
        if (value == null)
            throw new java.lang.IllegalArgumentException("null argument invalid");
        byte[] contents = org.openlcb.Utilities.bytesFromHexString(value);
        if (contents.length < BYTECOUNT)
            throw new java.lang.IllegalArgumentException("Wrong NodeID length: "+contents.length);
        this.contents = new byte[BYTECOUNT];
        for (int i = 0; i < BYTECOUNT; i++)
            this.contents[i] = contents[i];
    }

    @CheckReturnValue
    public NodeID(long value) {
        byte[] c = new byte[BYTECOUNT];
        Utilities.HostToNetworkUint48(c, 0, value);
        contents = c;
    }

    byte[] contents;

    @CheckReturnValue
    public boolean equals(Object o){
        // try to cast, else not equal
        try {
            NodeID other = (NodeID) o;
            for (int i = 0; i<BYTECOUNT; i++)
                if (other.contents[i] != this.contents[i]) return false;
            return true;
        } catch (Exception e) {
            return false;
        }
    }  
    
    @CheckReturnValue
    public int hashCode() {
        return contents[0]
            +contents[1]<<5
            +contents[2]<<10
            +contents[3]<<15
            +contents[4]<<20
            +contents[5]<<25;
    } 

    public long toLong() {
        long retval = 0;
        retval |= contents[0] & 0xff; retval <<= 8;
        retval |= contents[1] & 0xff; retval <<= 8;
        retval |= contents[2] & 0xff; retval <<= 8;
        retval |= contents[3] & 0xff; retval <<= 8;
        retval |= contents[4] & 0xff; retval <<= 8;
        retval |= contents[5] & 0xff;
        return retval;
    }

    @CheckReturnValue
    @NonNull
    public byte[] getContents() {
        // copy to ensure immutable
        byte[] retval = new byte[BYTECOUNT];
        for (int i =0; i < BYTECOUNT; i++) 
            retval[i] = contents[i];
        return retval;
    }

    @CheckReturnValue
    @NonNull
    public String toString() {
        return Utilities.toHexDotsString(contents);
    }
    
}
