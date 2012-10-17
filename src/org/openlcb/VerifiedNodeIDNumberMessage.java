package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Verified Node ID Number message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class VerifiedNodeIDNumberMessage extends Message {
    
    public VerifiedNodeIDNumberMessage(NodeID source) {
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
        decoder.handleVerifiedNodeIDNumber(this, sender);
     }
    public String toString() {
        return super.toString()
                +" Verified Node ID Number";     
    }

    public int getMTI() { return MTI_VERIFIED_NID; }
}
