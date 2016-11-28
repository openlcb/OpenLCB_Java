package org.openlcb.can;


/**
 * Simple interface for a CAN frame.
 *
 * Immutable once created.
 *
 * @author  Bob Jacobsen   Copyright 2009, 2010
 * @version $Revision$
 */


public interface CanFrame {

    public int getHeader();
    
    public boolean isExtended();
    
    public boolean isRtr();
    
    public int getNumDataElements();
    public int getElement(int n);
    
    /**
     * Returns a long from all bytes of the CAN frame body.
     * Does not skip 1st address bytes in an OpenLCB header
     * @return the 8 bytes of CAN payload compacted into a 64-bit number. MSB-first.
     */
    public long bodyAsLong();
    
    /**
     * Returns a long from data bytes from the CAN frame body.
     * Skips 1st address bytes in an OpenLCB header
     * @return the 6 bytes of addressed payload (CAN frame bytes [2..7]) compacted into a 64-bit number, actually up to 48 bits filled. MSB-first.
     */
    public long dataAsLong();
    
    public byte[] getData();

}
