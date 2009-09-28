package org.nmra.net;

/**
 * Stream Initialization Reply message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class StreamInitReplyMessage extends Message {
    
    public StreamInitReplyMessage(NodeID source, NodeID dest, int bufferSize) {
        super(source);
        this.dest = dest;
        this.bufferSize = bufferSize;
    }
    
    NodeID dest;
    int bufferSize;
    
    public int getBufferSize() { return bufferSize; }
    
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
}
