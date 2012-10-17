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
public class ProtocolIdentificationReplyMessage extends AddressedMessage {
    
    public ProtocolIdentificationReplyMessage(NodeID source, NodeID dest, long value) {
        super(source, dest);
        this.value = value;
    }
        
    long value;
    
    public long getValue() { return value; }
    
     /**
      * To be equal, messages have to have the
      * same type and content
      */
     public boolean equals(Object o) {
        if (o == null) return false;
        if (! (o instanceof ProtocolIdentificationReplyMessage))
            return false;
        ProtocolIdentificationReplyMessage msg = (ProtocolIdentificationReplyMessage) o;
        if (this.value != msg.getValue())
            return false;
        return super.equals(o);
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
    
    @Override
    public String toString() {
        return super.toString()
                +" Protocol Identification Reply with value "+value;   
    }

    public int getMTI() { return MTI_PROTOCOL_IDENT_REPLY; }
}
