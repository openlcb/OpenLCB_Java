package org.openlcb;

/**
 * Stream Data Complete message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class StreamDataCompleteMessage extends Message {
    
    public StreamDataCompleteMessage(NodeID source, NodeID dest,
                int sourceStreamID, int destStreamID) {
        super(source);
        this.dest = dest;
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
        decoder.handleStreamDataComplete(this, sender);
     }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        StreamDataCompleteMessage p = (StreamDataCompleteMessage) o;
        if (!dest.equals(p.dest)) return false;
        if (sourceStreamID != p.sourceStreamID) return false;
        if (destStreamID != p.destStreamID) return false;
        return true;
    } 

    public String toString() {
        return getSourceNodeID().toString()
                +" StreamDataComplete "+dest.toString();     
    }

    public int getMTI() { return MTI_STREAM_DATA_COMPLETE; }
}
