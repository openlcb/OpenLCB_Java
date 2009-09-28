package org.nmra.net;

/**
 * Stream Initialization Reply message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class StreamInitReplyMessage extends Message {
    
    public StreamInitReplyMessage(NodeID source, NodeID dest, 
            int bufferSize, int sourceStreamID, int destStreamID) {
        super(source);
        this.dest = dest;
        this.bufferSize = bufferSize;
        this.sourceStreamID = sourceStreamID;
        this.destStreamID = destStreamID;
    }
    
    NodeID dest;
    int bufferSize;
    int sourceStreamID;
    int destStreamID;
    
    public int getBufferSize() { return bufferSize; }
    public int getDestStreamID() { return destStreamID; }
    
    /**
     * Implement message-type-specific
     * processing when this message
     * is received by a node.
     *<p>
     * Default is to do nothing.
     */
     @Override
     public void applyTo(MessageDecoder decoder, Connection sender) {
        decoder.handleStreamInitReply(this, sender);
     }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        StreamInitReplyMessage p = (StreamInitReplyMessage) o;
        if (!dest.equals(p.dest)) return false;
        if (bufferSize != p.bufferSize) return false;
        if (sourceStreamID != p.sourceStreamID) return false;
        if (destStreamID != p.destStreamID) return false;
        return true;
    } 

    public String toString() {
        return getSourceNodeID().toString()
                +" StreamInitReply "+dest.toString()
                +" SSID "+sourceStreamID
                +" DSID "+destStreamID
                +" bsize "+bufferSize;     
    }
}
