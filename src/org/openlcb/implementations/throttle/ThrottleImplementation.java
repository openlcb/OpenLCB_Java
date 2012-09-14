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

    public ThrottleImplementation(int dccAddress, boolean dccLongAddress, 
                                  MimicNodeStore store, DatagramService service) {
        this.dccAddress = dccAddress;
        this.dccLongAddress = dccLongAddress;
        this.store = store;
        this.service = service;
        
        dest = createNodeIdFromDcc(dccAddress, dccLongAddress);
    }

    int dccAddress;
    boolean dccLongAddress;
    MimicNodeStore store;
    DatagramService service;
    
    NodeID dest;
    
    NodeID createNodeIdFromDcc(int dccAddress, boolean dccLongAddress) {
        if (dccLongAddress)
            return new NodeID(new byte[]{6,0,0,0,(byte)((dccAddress>>8) & 0xFF), (byte)(dccAddress & 0xFF)});
        else
            return new NodeID(new byte[]{6,0,0,0,0, (byte)(dccAddress & 0xFF)});
    }
    
    public void start() {
        store.findNode(dest);
    }
    
    /**
     * @param speed Desired speed in scale meters/second. By convention, 100 m/sec is full speed
     * for DCC locomotives.
     */
    public void setSpeed(double speed) {
        ThrottleSpeedDatagram tsd = new ThrottleSpeedDatagram(speed);
        service.sendData(dest, tsd.getData());
    }
    
}
