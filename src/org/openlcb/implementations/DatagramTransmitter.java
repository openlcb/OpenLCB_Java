package org.openlcb.implementations;

import org.openlcb.*;

/**
 * Example of sending a OpenLCB datagram.
 *
 * @author  Bob Jacobsen   Copyright 2009
 */
public class DatagramTransmitter extends MessageDecoder {

    public DatagramTransmitter(NodeID here, NodeID far, int[] data, Connection c) {
        this.here = here;
        this.far = far;
        this.data = data;
        this.connection = c;
        
        // send content
        DatagramMessage m = new DatagramMessage(here, far, data);
        connection.put(m, this);
    }
    
    NodeID here;
    NodeID far;
    int[] data;
    Connection connection;

    /**
     * Handle "Datagram Acknowledged" message
     */
    public void handleDatagramAcknowledged(DatagramAcknowledgedMessage msg, Connection sender){
        // done, can drop buffer and end
    }
    
    /**
     * Handle "Datagram Rejected" message
     */
    public void handleDatagramRejected(DatagramRejectedMessage msg, Connection sender){
        // need to check if this is from right source
        
        // try again
        DatagramMessage m = new DatagramMessage(here, far, data);
        connection.put(m, this);
    }
    
}
