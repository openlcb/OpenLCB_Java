package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Identify Events message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class IdentifyEventsAddressedMessage extends AddressedMessage {
    
    public IdentifyEventsAddressedMessage(NodeID source, NodeID dest) {
        super(source, dest);
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
        decoder.handleIdentifyEventsAddressed(this, sender);
     }
    public String toString() {
        return super.toString()
                +" Identify Events ";   
    }

    public int getMTI() { return MTI_IDENTIFY_EVENTS; }
}
