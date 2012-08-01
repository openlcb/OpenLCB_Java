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
     */
    public long bodyAsLong();
    
    /**
     * Returns a long from data bytes from the CAN frame body.
     * Skips 1st address bytes in an OpenLCB header
     */
    public long dataAsLong();
    
    public byte[] getData();

}
