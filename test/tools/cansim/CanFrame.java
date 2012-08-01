package tools.cansim;

import tools.*;

/**
 * Carry and work with a simulated CAN frame.
 *
 * Immutable once created.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */


public class CanFrame implements org.openlcb.can.CanFrame {

    int header;
    int[] bytes;
    
    public CanFrame(int header) {
        this.bytes = null;
        this.header = header;
    }
            
    public CanFrame(int header, int[] bytes) {
        this(header);
        if (bytes.length > 8) {
            throw new IllegalArgumentException("payload too long: "+bytes);
        }
        this.bytes = bytes;
    }
    
    public int getHeader() { return header; }
    public byte[] getData() { 
        byte[] t = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) t[i] = (byte)bytes[i];
        return t; 
    }
    public long bodyAsLong() {
        long retval = 0;
        for (int i = 0 ; i<bytes.length; i++) {
            retval = retval << 8 | (bytes[0]&0xFF);
        }
        return retval;
    }
    
    public long dataAsLong() {
        long retval = 0;
        for (int i = 2 ; i<bytes.length; i++) {
            retval = retval << 8 | (bytes[0]&0xFF);
        }
        return retval;
    }
    
    public boolean isExtended() { return true; }    
    public boolean isRtr() { return false; }
    public int getNumDataElements() { return bytes.length; }
    public int getElement(int n) { return bytes[n]; }
    
    public boolean equals(Object other) {
        // try to cast, else not equal
        try {
            CanFrame c = (CanFrame) other;
            if (this.header != c.header) return false;
            if (this.bytes == null && c.bytes == null) return true;
            if (this.bytes == null && c.bytes != null) return false;
            if (this.bytes != null && c.bytes == null) return false;
            if (this.bytes.length != c.bytes.length) return false;
            for (int i = 0; i < this.bytes.length; i++) {
                if (this.bytes[i] != c.bytes[i]) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
        
}
