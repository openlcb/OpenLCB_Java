package org.openlcb;

/**
 * Common NodeID implementation
 * <p>
 * NodeID objects are immutable once created.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class NodeID {
    static final int BYTECOUNT = 6;
    
    public NodeID(NodeID node) {
        this (node.contents);
    }
    
    public NodeID(byte[] contents) {
        if (contents == null)
            throw new java.lang.IllegalArgumentException("null argument invalid");
        if (contents.length < BYTECOUNT)
            throw new java.lang.IllegalArgumentException("Wrong NodeID length: "+contents.length);
        this.contents = new byte[BYTECOUNT];
        for (int i = 0; i < BYTECOUNT; i++)
            this.contents[i] = contents[i];
    }
    
    byte[] contents;

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
    public int hashCode() {
        return contents[0]
            +contents[1]<<5
            +contents[2]<<10
            +contents[3]<<15
            +contents[4]<<20
            +contents[5]<<25;
    } 
    
    public byte[] getContents() {
        // copy to ensure immutable
        byte[] retval = new byte[BYTECOUNT];
        for (int i =0; i < BYTECOUNT; i++) 
            retval[i] = contents[i];
        return retval;
    }

    public String toString() {
        return "NodeID:"
                +contents[0]+","
                +contents[1]+","
                +contents[2]+","
                +contents[3]+","
                +contents[4]+","
                +contents[5];
    }
}
