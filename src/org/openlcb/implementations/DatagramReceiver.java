package org.openlcb.implementations;

import org.openlcb.*;

/**
 * Example of receiving a OpenLCB datagram.
 *
 * @author  Bob Jacobsen   Copyright 2009
 */
public class DatagramReceiver extends MessageDecoder {
    public DatagramReceiver(NodeID here, NodeID far, Connection c) {
        this.here = here;
        this.far = far;
        this.connection = c;
    }
    
    NodeID here;
    NodeID far;
    Connection connection;

    /**
     * Handle "Datagram" message
     */
    public void handleDatagram(DatagramMessage msg, Connection sender){
        // accept
        Message m = new DatagramAcknowledgedMessage(here, far);
        connection.put(m, this);
    }

}
