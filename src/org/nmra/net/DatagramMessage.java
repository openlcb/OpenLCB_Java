package org.nmra.net;

/**
 * Datagram message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class DatagramMessage extends Message {
    
    public DatagramMessage(NodeID source, NodeID dest, int[] data) {
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
        decoder.handleDatagram(this, sender);
     }
}
