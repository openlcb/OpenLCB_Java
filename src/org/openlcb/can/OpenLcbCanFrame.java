package org.openlcb.can;

import org.openlcb.*;

/**
 * Carry and work with a CAN frame in OpenLCB format.
 *
 * Immutable once created.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */


public class OpenLcbCanFrame implements org.openlcb.can.CanFrame {

    public static OpenLcbCanFrame makeCimFrame(int alias, int num, int val) {
        return new OpenLcbCanFrame( (0<<26) | (alias&0xffffff));
    }
    
    public static OpenLcbCanFrame makeRimFrame(int alias, NodeID n) {
        return new OpenLcbCanFrame( (1<<26) | (alias&0xffffff),
                                    n.getContents());
    }
    
    static int makeHeader(int alias) {
        return 
            (alias&0xFFFFFF);
    }
    
    // data is stored in completed form as
    // a header and data content; accessors go
    // back and forth to individual fields.
    long header;
    byte[] bytes;
    
    public OpenLcbCanFrame(long header) {
        this.header = header;
    }
            
    public OpenLcbCanFrame(long header, byte[] bytes) {
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
        OPENLCBCOMMONMESSAGE;
    }
    
    public TypeField getTypeField() {
        return TypeField.values()[(int)((getHeader() & 0x0C000000) >> 26)]; 
    }
    
    // Frame itself is immutable
    static public long setTypeField(long header, TypeField v) {
        return (header&~0x0C000000)|(v.ordinal() << 26);
    }
    
    public long getHeader() { return header; }
    
    public int getNodeIDa() { return (int)getHeader()&0xFFFFFF; }
    
    public boolean isCIM() { return (getTypeField() == TypeField.CHECKIDMESSAGE); }
    public boolean isRIM() { return (getTypeField() == TypeField.RESERVEDIDMESSAGE); }
    
    public boolean equals(Object other) {
        // try to cast, else not equal
        try {
            OpenLcbCanFrame c = (OpenLcbCanFrame) other;
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
    
    public String toString() {
        return "Type: "+getTypeField()
            + " NIDa: "+getNodeIDa()
            + " isCIM: "+isCIM()
            + " isRIM: "+isRIM();
            
    }
}
