package org.nmra.net.can;


/**
 * Very, very simple interface for a CAN frame.
 *
 * This is mostly here to make it possible to (later) 
 * move to an implementation class that's hardware specific.
 *
 * Immutable once created.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */


public interface CanFrame {

    public long getHeader();
    
}
