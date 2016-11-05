package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Request message for the Simple Node Ident Info protocol 
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision: 529 $
 */
@Immutable
@ThreadSafe
public class SimpleNodeIdentInfoRequestMessage extends AddressedPayloadMessage {
    
    public SimpleNodeIdentInfoRequestMessage(NodeID source, NodeID dest) {
        super(source, dest, null);
    }
        
     /**
      * To be equal, messages have to have the
      * same type and content
      */
     public boolean equals(Object o) {
        if (o == null) return false;
        if (! (o instanceof SimpleNodeIdentInfoRequestMessage))
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
        decoder.handleSimpleNodeIdentInfoRequest(this, sender);
     }

    @Override
    public MessageTypeIdentifier getEMTI() { return MessageTypeIdentifier.SimpleNodeIdentInfoRequest; }
}
