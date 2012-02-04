package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Reply message for the Protocol Identification protocol 
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision: 529 $
 */
@Immutable
@ThreadSafe
public class ProtocolIdentificationReplyMessage extends Message {
    
    public ProtocolIdentificationReplyMessage(NodeID source) {
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
        decoder.handleProtocolIdentificationReply(this, sender);
     }
    public String toString() {
        return getSourceNodeID().toString()
                +" Protocol Identification Reply ";   
    }

    public int getMTI() { return MTI_PROTOCOL_IDENT_REPLY; }
}
