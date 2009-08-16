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


public class CanFrame implements org.nmra.net.can.CanFrame {

    long header;
    int[] bytes;
    
    public CanFrame(long header) {
        this.bytes = null;
        this.header = header;
    }
            
    public CanFrame(long header, int[] bytes) {
        this(header);
        if (bytes.length > 8) {
            throw new IllegalArgumentException("payload too long: "+bytes);
        }
        this.bytes = bytes;
    }
    
    public long getHeader() { return header; }
    
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
