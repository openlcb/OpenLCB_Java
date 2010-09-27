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

}
