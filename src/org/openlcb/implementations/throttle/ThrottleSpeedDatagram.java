package org.openlcb.implementations.throttle;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import org.openlcb.*;
import org.openlcb.implementations.DatagramService;

/**
 * Speed control datagram from throttle to command station.
 * 
 * Meant to shield the using code from all the details of that
 * process via some primitives:
 * <ul>
 * <li>
 * <li>
 * </ul>
 *
 * Content:
 *  Datagram type byte  0x30
 *  Subtype: set speed  0x01
 *  Speed as float16 (2 bytes)
 *      100.0 is full DCC speed (notionally meters per second)
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class ThrottleSpeedDatagram {

    /**
     * @param speed Desired speed in scale meters/second. By convention, 100 m/sec is full speed
     * for DCC locomotives.
     */
    public ThrottleSpeedDatagram(double speed, boolean forward) {
        this.speed = speed;
        this.forward = forward;
    }
    
    /**
     * Create datagram for emergency stop
     */
    public ThrottleSpeedDatagram() {
        this.estop = true;
    }
    
    NodeID dest;
    double speed;
    boolean forward;
    boolean estop = false;
    
    public int[] getData() {
        if (estop) {
            int[] data = new int[]{0x30,0x00};
            return data;
        } else {
            Float16 fs = new Float16(speed, forward);
            int fsi = fs.getInt();
            int[] data = new int[]{0x30,0x01,(fsi>>8)&0xFF, fsi&0xFF};
            return data;
        }
    }
}
