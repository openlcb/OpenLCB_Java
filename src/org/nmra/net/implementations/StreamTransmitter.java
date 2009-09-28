package org.nmra.net.implementations;

import org.nmra.net.*;

/**
 * Example of sending a NMRAnet stream.
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

    public StreamTransmitter(NodeID here, NodeID far, int bufferSize, int[] bytes, Connection c) {
        this.here = here;
        this.far = far;
        this.bufferSize = bufferSize;
        this.bytes = bytes;
        this.connection = c;
        
        // start negotiation
        StreamInitRequestMessage m = new StreamInitRequestMessage(here, far, bufferSize, destStreamID);
        connection.put(m, this);
    }
    
    NodeID here;
    NodeID far;
    int bufferSize; 
    int[] bytes;
    Connection connection;
    int nextIndex;
    
    int destStreamID;
    int sourceStreamID = 4;  // notional value 
    
    /**
     * Handle "Stream Init Reply" message
     */
    public void handleStreamInitReply(StreamInitReplyMessage msg, Connection sender){
        // pick up buffer size to use
        this.bufferSize = msg.getBufferSize();
        this.destStreamID = msg.getDestStreamID();
        
        // init transfer
        nextIndex = 0;

        // send data
        sendNext();
    }

    void sendNext() {
        int size = Math.min(bufferSize, bytes.length-nextIndex);
        int[] data = new int[size];
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
