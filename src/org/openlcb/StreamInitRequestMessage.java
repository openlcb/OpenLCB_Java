package org.openlcb;

/**
 * Stream Initialization Request message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class StreamInitRequestMessage extends Message {
    
    public StreamInitRequestMessage(NodeID source, NodeID dest, 
                    int bufferSize, int sourceStreamID) {
        super(source);
        this.dest = dest;
        this.bufferSize = bufferSize;
        this.sourceStreamID = sourceStreamID;
    }
    
    NodeID dest;
    int bufferSize;
    int sourceStreamID;
    
    public int getBufferSize() { return bufferSize; }
    public int getSourceStreamID() { return sourceStreamID; }
    
    /**
     * Implement message-type-specific
     * processing when this message
     * is received by a node.
     *<p>
     * Default is to do nothing.
     */
     @Override
     public void applyTo(MessageDecoder decoder, Connection sender) {
        decoder.handleStreamInitRequest(this, sender);
     }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        StreamInitRequestMessage p = (StreamInitRequestMessage) o;
        if (!dest.equals(p.dest)) return false;
        if (bufferSize != p.bufferSize) return false;
        if (sourceStreamID != p.sourceStreamID) return false;
        return true;
    } 

    public String toString() {
        return getSourceNodeID().toString()
                +" StreamInitRequest "+dest.toString()    
                +" SSID "+sourceStreamID
                +" bsize "+bufferSize;     
    }
}
