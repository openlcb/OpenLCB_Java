package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Verify Node ID Number message implementation.
 * 
 * Addressed form
 *
 * @author  Bob Jacobsen   Copyright 2009, 2010, 2024
 */
@Immutable
@ThreadSafe
public class VerifyNodeIDNumberAddressedMessage extends AddressedMessage {
    
    public VerifyNodeIDNumberAddressedMessage(NodeID source, NodeID destination) {
        super(source, destination);
        this.content = null;
    }

    public VerifyNodeIDNumberAddressedMessage(NodeID source, NodeID destination, NodeID content) {
        this(source, destination);
        this.content = content;
    }
    
    NodeID content;
    
    public NodeID getContent() { return content; }
    
     /**
      * To be equal, messages have to have the
      * same type and content
      */
     public boolean equals(Object o) {
        if (! (o instanceof VerifyNodeIDNumberAddressedMessage))
            return false;
        VerifyNodeIDNumberAddressedMessage msg = (VerifyNodeIDNumberAddressedMessage) o;
        if (this.content != null) {
            if (msg.content == null || (! this.content.equals(msg.content)))
                return false;
        } else {
            if (msg.content != null) return false;
        }
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
        decoder.handleVerifyNodeIDNumberAddressed(this, sender);
     }

    public String toString() {
        return super.toString()
                +" Verify Node ID Number Addressed";    
    }

    public int getMTI() { return MTI_VERIFY_NID; }
}
