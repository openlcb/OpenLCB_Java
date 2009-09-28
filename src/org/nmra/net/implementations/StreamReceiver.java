package org.nmra.net.implementations;

import org.nmra.net.*;

/**
 * Example of receiving a NMRAnet stream.
 *<p>
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class StreamReceiver extends MessageDecoder {
    public StreamReceiver(NodeID here, NodeID far, Connection c) {
        this.here = here;
        this.far = far;
        this.connection = c;
    }
    
    NodeID here;
    NodeID far;
    Connection connection;

    /**
     * Handle "Stream Init Request" message
     */
    public void handleStreamInitRequest(StreamInitRequestMessage msg, Connection sender){
        // send reply with same length
        int len = msg.getBufferSize();
        
        Message m = new StreamInitReplyMessage(here, far, len);
        connection.put(m, this);
    }

    /**
     * Handle "Stream Data Send" message
     */
    public void handleStreamDataSend(StreamDataSendMessage msg, Connection sender){
        // send proceed reply
        Message m = new StreamDataProceedMessage(here, far);
        connection.put(m, this);
    }

}
