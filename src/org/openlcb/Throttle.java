package org.openlcb;

import org.openlcb.implementations.DatagramService;

/**
 * Provides a Throttle end-point for the Traction Protocol
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class Throttle {
    public Throttle(NodeID trainNode, DatagramService downstream) {
        this.trainNode = trainNode;
        this.downstream = downstream;
    }
    
    private static final int DATAGRAM_MOTIVE            = 0x30;
    private static final int DATAGRAM_MOTIVE_SETSPEED   = 0x01;

    public void setSpeed(float speed) {

        int[] data = new int[]{DATAGRAM_MOTIVE, DATAGRAM_MOTIVE_SETSPEED, 0, 0, 0, 0};        
        downstream.sendData(trainNode, data);
    }
    
    protected NodeID trainNode;
    protected DatagramService downstream;
    
}
