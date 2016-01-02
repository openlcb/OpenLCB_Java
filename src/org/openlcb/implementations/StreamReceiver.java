package org.openlcb.implementations;

import org.openlcb.*;

/**
 * Example of receiving a OpenLCB stream.
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

    byte sourceStreamID;
    byte destStreamID = 3;  //  notional value

    /**
     * Handle "Stream Init Request" message
     */
    public void handleStreamInitiateRequest(StreamInitiateRequestMessage msg, Connection sender){
        // send reply with same length
        int len = msg.getBufferSize();
        sourceStreamID = msg.getSourceStreamID();

        Message m = new StreamInitiateReplyMessage(here, far, len, sourceStreamID, destStreamID);
        connection.put(m, this);
    }

    /**
     * Handle "Stream Data Send" message
     */
    public void handleStreamDataSend(StreamDataSendMessage msg, Connection sender){
        // send proceed reply
        Message m = new StreamDataProceedMessage(here, far, sourceStreamID, destStreamID);
        connection.put(m, this);
    }

}
