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

    public StreamTransmitter(NodeID here, NodeID far, int bufferSize, int[] bytes, Connection c) {
        //System.out.println("StreamTransmitter");
        this.here = here;
        this.far = far;
        this.bufferSize = bufferSize;
        this.bytes = bytes;
        this.connection = c;
        
        // We need to set destStreamID before we set it,  Not setting it is
        // a high priorty error identified by spotbugs.  The value here may 
        // not be right, but we don't have a value passed to this method.
        destStreamID = 0;

        // start negotiation
        StreamInitiateRequestMessage m = new StreamInitiateRequestMessage(here, far, bufferSize, sourceStreamID, destStreamID);
        connection.put(m, this);
    }
    
    NodeID here;
    NodeID far;
    int bufferSize; 
    int[] bytes;
    Connection connection;
    int nextIndex;
    
    byte destStreamID;
    byte sourceStreamID = 4;  // notional value
    
    /**
     * Handle "Stream Init Reply" message
     */
    public void handleStreamInitiateReply(StreamInitiateReplyMessage msg, Connection sender){
        //System.out.println("StreamTransmitter handleStreamInitiateReply");
        // pick up buffer size to use
        this.bufferSize = msg.getBufferSize();
        this.destStreamID = msg.getDestinationStreamID();
        
        // init transfer
        nextIndex = 0;

        // send data
        sendNext();
    }

    void sendNext() {
        //System.out.println("StreamTransmitter sendNext");
        int size = Math.min(bufferSize, bytes.length-nextIndex);
        int[] data = new int[size];
        // copy the needed data
        for (int i = 0; i<size; i++)
            data[i] = bytes[nextIndex+i];
        nextIndex = nextIndex+size;
        
        // send data
        Message m = new StreamDataSendMessage(here, far, data);
        connection.put(m, this);
        
        // are we done?
        if (nextIndex < bytes.length) return; // no, wait for Data Proceed message
        
        // yes, say we're done
        m = new StreamDataCompleteMessage(here, far, sourceStreamID, destStreamID);
        connection.put(m, this);
    }
    
    /**
     * Handle "Stream Data Proceed" message
     */
    public void handleStreamDataProceed(StreamDataProceedMessage msg, Connection sender){
        //System.out.println("StreamTransmitter handleStreamDataProceed");
        sendNext();
    }
    
}
