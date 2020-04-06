package tools.cansim;

import java.util.Arrays;

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
            throw new IllegalArgumentException("payload too long: " + Arrays.toString(bytes));
        }
        this.bytes = bytes;
    }
    
    @Override
    public int getHeader() {
        return header;
    }
    
    @Override
    public byte[] getData() { 
        byte[] t = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            t[i] = (byte)bytes[i];
        }
        return t; 
    }
    
    @Override
    public long bodyAsLong() {
        long retval = 0;
        for (int i = 0 ; i<bytes.length; i++) {
            retval = retval << 8 | (bytes[0]&0xFF);
        }
        return retval;
    }
    
    @Override
    public long dataAsLong() {
        long retval = 0;
        for (int i = 2 ; i<bytes.length; i++) {
            retval = retval << 8 | (bytes[0]&0xFF);
        }
        return retval;
    }
    
    @Override
    public boolean isExtended() {
        return true;
    }
    
    @Override
    public boolean isRtr() {
        return false;
    }
    
    @Override
    public int getNumDataElements() {
        return bytes.length;
    }
    
    @Override
    public int getElement(int n) {
        return bytes[n];
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof CanFrame) {
            return equals((CanFrame) other);
        }
        return false;
    }
        
    public boolean equals(CanFrame other) {
        if (this.header != other.header) {
            return false;
        }
        if ((this.bytes == null) && (other.bytes == null)) {
            return true;
        }
        if ((this.bytes != null) && (other.bytes != null)) {
            if (this.bytes.length != other.bytes.length) {
                return false;
            }
            for (int i = 0; i < this.bytes.length; i++) {
                if (this.bytes[i] != other.bytes[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        long hash = header;
        if (bytes != null) {
            for (int i = 0; i < this.bytes.length; i++) {
                hash = 7 * hash + bytes[i];
            }
        }
        return Long.valueOf(hash).hashCode();
    }
}
