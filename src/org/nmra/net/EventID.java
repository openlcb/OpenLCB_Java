package org.nmra.net;

/**
 * Common EventID implementation.
 * <p>
 * EventID objects are immutable once created.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class EventID {

    static final int BYTECOUNT = 8;
    
    public EventID(NodeID node) {
        this (node.contents);
    }
    
    public EventID(byte[] contents) {
        if (contents == null)
            throw new java.lang.IllegalArgumentException("null argument invalid");
        if (contents.length != BYTECOUNT)
            throw new java.lang.IllegalArgumentException("Wrong EventID length: "+contents.length);
        this.contents = new byte[BYTECOUNT];
        for (int i = 0; i < BYTECOUNT; i++)
            this.contents[i] = contents[i];
    }
    
    byte[] contents;
    
    public boolean equals(Object o){
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

    public String toString() {
        return "Event:"
                +contents[0]+","
                +contents[1]+","
                +contents[2]+","
                +contents[3]+","
                +contents[4]+","
                +contents[5]+","
                +contents[6]+","
                +contents[7];
    }
}
