package org.openlcb.implementations.throttle;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import org.openlcb.*;
import org.openlcb.implementations.DatagramService;

/**
 * Function control datagram from throttle to command station.
 *
 * Content:
 *  Datagram type byte     0x30
 *  Subtype: set function  0x11
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class ThrottleFunctionDatagram {

    public ThrottleFunctionDatagram(int number, int state) {
        this.number = number;
        this.state = state;
    }
    
    int number;
    int state;
    
    public int[] getData() {
        int[] data = new int[]{0x30,0x11, number>>8, number&0xFF, state>>8, state&0xFF};
        return data;
    }
}
