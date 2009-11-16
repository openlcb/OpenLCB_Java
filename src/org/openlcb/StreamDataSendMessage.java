package org.openlcb;

/**
 * Stream Data Send message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class StreamDataSendMessage extends Message {
    
    public StreamDataSendMessage(NodeID source, NodeID dest, int[] data,
                    int destStreamID) {
        super(source);
        this.dest = dest;
        this.data = data;
        this.destStreamID = destStreamID;
    }
        
    NodeID dest;
    int[] data;
    int destStreamID;
    
    /**
     * Implement message-type-specific
     * processing when this message
     * is received by a node.
     *<p>
     * Default is to do nothing.
     */
     @Override
     public void applyTo(MessageDecoder decoder, Connection sender) {
        decoder.handleStreamDataSend(this, sender);
     }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        StreamDataSendMessage p = (StreamDataSendMessage) o;
        if (!dest.equals(p.dest)) return false;
        if (data.length != p.data.length) return false;
        // should also check data length
        if (destStreamID != p.destStreamID) return false;
        return true;
    } 

    public String toString() {
        return getSourceNodeID().toString()
                +" StreamDataSend "+dest.toString();     
    }
}
