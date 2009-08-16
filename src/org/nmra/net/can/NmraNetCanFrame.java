package org.nmra.net.can;

import org.nmra.net.*;

/**
 * Carry and work with a CAN frame in NMRAnet format.
 *
 * Immutable once created.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */


public class NmraNetCanFrame implements org.nmra.net.can.CanFrame {

    public static NmraNetCanFrame makeCimFrame(int alias, int num, int val) {
        return new NmraNetCanFrame( (0<<26) | (alias&0xffff));
    }
    
    public static NmraNetCanFrame makeRimFrame(int alias, NodeID n) {
        return new NmraNetCanFrame( (1<<26) | (alias&0xffff),
                                    n.getContents());
    }
    
    static int makeHeader(int alias) {
        return 
            (alias&0xFFFF);
    }
    
    // data is stored in completed form as
    // a header and data content; accessors go
    // back and forth to individual fields.
    long header;
    byte[] bytes;
    
    public NmraNetCanFrame(long header) {
        this.header = header;
    }
            
    public NmraNetCanFrame(long header, byte[] bytes) {
        this(header);
        if (bytes.length > 8) {
            throw new IllegalArgumentException("payload too long: "+bytes);
        }
        this.bytes = bytes;
    }
    
    public enum MessageType {   
        PCIR,
        example;
    }
    
    public boolean isDidPresent() {
        return (header&0x0000001) != 0;
    }
    
    // Frame itself is immutable
    static public long setDidPresent(long header, boolean present) {
        return header | 0x0000001;
    }
    
    public enum TypeField {
        CHECKIDMESSAGE,        
        RESERVEDIDMESSAGE,
        CANMESSAGE,
        NMRANETCOMMONMESSAGE;
    }
    
    public TypeField getTypeField() {
        return TypeField.values()[(int)((getHeader() & 0x0C000000) >> 26)]; 
    }
    
    // Frame itself is immutable
    static public long setTypeField(long header, TypeField v) {
        return (header&~0x0C000000)|(v.ordinal() << 26);
    }
    
    public long getHeader() { return header; }
    
    public int getNodeIDa() { return (int)getHeader()&0xFFFF; }
    
    public boolean isCIM() { return (getTypeField() == TypeField.CHECKIDMESSAGE); }
    public boolean isRIM() { return (getTypeField() == TypeField.RESERVEDIDMESSAGE); }
    
    public boolean equals(Object other) {
        // try to cast, else not equal
        try {
            NmraNetCanFrame c = (NmraNetCanFrame) other;
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
