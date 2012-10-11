package org.openlcb.implementations.throttle;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.openlcb.*;

/**
 * Represents a 16-bit IEEE float.
 *
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class Float16 {

    public Float16(float f) {
        this((double)f, (f>=0.0f));
    }
    
    public Float16(double d) {
        this(d, (d>=0.0f));
    }

    /**
     * This allows the use of the -0.0 value if needed. That's not handled
     * by the other constructors
     */
    Float16(double d, boolean positive) {
        if (d == 0.0) {
            byte1 = 0;
            if (!positive) byte1 = (byte)0x80;
            byte2 = 0;
            return;
        }
        
        if (d<0) {
            d = -1 * d;
        }
        int exp = 15;
        if (d < 1.0) {
            while (d < 1.0) {
                d = 2*d;
                exp--;
            }
        } else {
            while (d >= 2.0) {
                d = d/2;
                exp++;
            }
        }
        
        int ch =  ((int)(d*1024.))&0x3FF;
        if ((((int)(d*1024.))&0x400) != 0x400) System.out.println("normalization failed with d="+d+" exp="+exp);
        int bits = ch | (exp<<10);
        if (!positive) bits = bits | 0x8000;
        
        byte1 = (byte)((bits >> 8)&0xFF);
        byte2 = (byte)(bits&0xFF);
    }
    
    public Float16(int i) {
        byte1 = (byte)((i>>8)&0xFF);
        byte2 = (byte)(i&0xFF);
    }
    
    byte byte1, byte2;
    
    public int getInt() { 
        return ((byte1&0xFF)<<8)|(byte2&0xFF); 
    }
    
    public float getFloat() { 
        if (byte1 == 0 && byte2 == 0) return 0.0f;
        int ch = (byte2&0xFF) | ((byte1&0x3)<<8) | 0x400;
        int exp = (( ((int)byte1)&0x7C)>>2)-15;
        int sign = ( (byte1 & 0x80) !=0 ) ? -1 : +1;
        return (float)(((double)ch)/1024.0*((double)(1<<exp)))*sign;
    }
    
}
