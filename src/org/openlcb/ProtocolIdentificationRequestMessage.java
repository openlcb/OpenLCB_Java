package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Request message for the Protocol Identification protocol 
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision: 529 $
 */
@Immutable
@ThreadSafe
public class ProtocolIdentificationRequestMessage extends AddressedPayloadMessage {
    
    public ProtocolIdentificationRequestMessage(NodeID source, NodeID dest) {
        super(source, dest, null);
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

    @Override
    public MessageTypeIdentifier getEMTI() {
        return MessageTypeIdentifier.ProtocolSupportInquiry;
    }
}
