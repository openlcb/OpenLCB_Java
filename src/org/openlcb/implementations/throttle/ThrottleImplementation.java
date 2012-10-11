package org.openlcb.implementations.throttle;

import org.openlcb.*;
import org.openlcb.implementations.DatagramService;

/**
 * Basic Throttle implementation, a class
 * that uses the Motive Power protocol(s)
 * to interface with e.g. a train.
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class ThrottleImplementation {

    public ThrottleImplementation(NodeID node, 
                                  MimicNodeStore store, DatagramService service) {
        this.dest = node;
        this.store = store;
        this.service = service;
    }

    public ThrottleImplementation(int dccAddress, boolean dccLongAddress, 
                                  MimicNodeStore store, DatagramService service) {
        this(createNodeIdFromDcc(dccAddress, dccLongAddress), store, service);
    }

    MimicNodeStore store;
    DatagramService service;
    NodeID dest;
    
    static NodeID createNodeIdFromDcc(int dccAddress, boolean dccLongAddress) {
        if (dccLongAddress)
            return new NodeID(new byte[]{6,1,0,0,(byte)(((dccAddress>>8) & 0xFF) | 0xC0), (byte)(dccAddress & 0xFF)});
        else
            return new NodeID(new byte[]{6,1,0,0,0, (byte)(dccAddress & 0xFF)});
    }
    
    public void start() {
        store.findNode(dest);
    }
    
    /**
     * @param speed Desired speed in scale meters/second. By convention, 100 m/sec is full speed
     * for DCC locomotives.
     */
    public void setSpeed(double speed, boolean forward) {
        ThrottleSpeedDatagram tsd = new ThrottleSpeedDatagram(speed, forward);
        service.sendData(dest, tsd.getData());
    }
    
    /**
     * Set emergency stop; unset by any following setSpeed command
     */
    public void doEmergencyStop() {
        ThrottleSpeedDatagram tsd = new ThrottleSpeedDatagram();
        service.sendData(dest, tsd.getData());
    }
    
    /**
     * @param number 0 - ?? number of DCC function (0-28 space)
     * @param value 0 for off, non-zero for on
     */
    public void setFunction(int number, int value) {
        ThrottleFunctionDatagram tsd = new ThrottleFunctionDatagram(number, value);
        service.sendData(dest, tsd.getData());
    }
    
}
