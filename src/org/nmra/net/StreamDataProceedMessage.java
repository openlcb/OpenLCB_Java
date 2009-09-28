package org.nmra.net;

/**
 * Stream Data Proceed message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class StreamDataProceedMessage extends Message {
    
    public StreamDataProceedMessage(NodeID source, NodeID dest) {
        super(source);
    }
        
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
}
