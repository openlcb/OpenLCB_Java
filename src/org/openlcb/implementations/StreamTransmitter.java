package org.openlcb.implementations;

import org.openlcb.*;

/**
 * Example of sending a OpenLCB stream.
 *<p>
 * This implementation is limited to sending 
 * from a fixed-size input array.  The protocol
 * permits continuous transmission, but this class
 * is not intended to implement that.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class StreamTransmitter extends MessageDecoder {

    public StreamTransmitter(NodeID here, NodeID far, int bufferSize, byte[] bytes, Connection c) {
        this.here = here;
        this.far = far;
        this.bufferSize = bufferSize;
        this.bytes = bytes;
        this.connection = c;
        
        // start negotiation
        StreamInitiateRequestMessage m = new StreamInitiateRequestMessage(here, far, bufferSize, sourceStreamID, destStreamID);
        connection.put(m, this);
    }
    
    NodeID here;
    NodeID far;
    int bufferSize; 
    byte[] bytes;
    Connection connection;
    int nextIndex;
    
    byte destStreamID;
    byte sourceStreamID = 4;  // notional value
    
    /**
     * Handle "Stream Init Reply" message
     */
    public void handleStreamInitiateReply(StreamInitiateReplyMessage msg, Connection sender){
        // pick up buffer size to use
        this.bufferSize = msg.getBufferSize();
        this.destStreamID = msg.getDestinationStreamID();
        
        // init transfer
        nextIndex = 0;

        // send data
        sendNext();
    }

    void sendNext() {
        int size = Math.min(bufferSize, bytes.length-nextIndex);
        byte[] data = new byte[size];
        // copy the needed data
        for (int i = 0; i<size; i++)
            data[i] = bytes[nextIndex+i];
        nextIndex = nextIndex+size;
        
        // send data
        Message m = new StreamDataSendMessage(here, far, data, destStreamID);
        connection.put(m, this);
        
        // are we done?
        if (nextIndex < bytes.length) return; // wait for Data Proceed message
        
        // yes, say we're done
        m = new StreamDataCompleteMessage(here, far, sourceStreamID, destStreamID);
        connection.put(m, this);
    }
    
    /**
     * Handle "Stream Data Proceed" message
     */
    public void handleStreamDataProceed(StreamDataProceedMessage msg, Connection sender){
        sendNext();
    }
    
}
