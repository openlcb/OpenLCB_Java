package org.openlcb.implementations.throttle;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import org.openlcb.*;
import org.openlcb.implementations.DatagramService;

/**
 * Function control datagram from throttle to command station.
 *
 * Direct use of memory config protocol.
 *
 * DCC functions are defined as 
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
        int[] data = new int[]{0x20,  // mem config
                                0x00, // mem write
                                0x00,0x00,0x00, number,  // address
                                0xF9, // space
                                state&0xFF};   // value
        return data;
    }
}
