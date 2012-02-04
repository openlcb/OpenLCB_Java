package org.openlcb;

/**
 * Request message for the Protocol Identification protocol 
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision: 529 $
 */
public class ProtocolIdentificationRequestMessage extends Message {
    
    public ProtocolIdentificationRequestMessage(NodeID source) {
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
        decoder.handleProtocolIdentificationRequest(this, sender);
     }
    public String toString() {
        return getSourceNodeID().toString()
                +" Protocol Identification Request ";   
    }

    public int getMTI() { return MTI_PROTOCOL_IDENT_REQUEST; }
}
