package org.openlcb;

/**
 * Stream Data Proceed message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class StreamDataProceedMessage extends Message {
    
    public StreamDataProceedMessage(NodeID source, NodeID dest, 
                        int sourceStreamID, int destStreamID) {
        super(source);
        this.dest = dest;
        this.sourceStreamID = sourceStreamID;
        this.destStreamID = destStreamID;
    }
        
    NodeID dest;
    int sourceStreamID;
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
        decoder.handleStreamDataProceed(this, sender);
     }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        StreamDataProceedMessage p = (StreamDataProceedMessage) o;
        if (!dest.equals(p.dest)) return false;
        if (sourceStreamID != p.sourceStreamID) return false;
        if (destStreamID != p.destStreamID) return false;
        return true;
    } 

    public String toString() {
        return getSourceNodeID().toString()
                +" StreamDataProceed "+dest.toString();     
    }

    public int getMTI() { return MTI_STREAM_DATA_PROCEED; }
}
