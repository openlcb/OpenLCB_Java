package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Verify Node ID Number message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009, 2010
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class VerifyNodeIDNumberMessage extends Message {
    
    public VerifyNodeIDNumberMessage(NodeID source) {
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
        decoder.handleVerifyNodeIDNumber(this, sender);
     }

    public String toString() {
        return getSourceNodeID().toString()
                +" Verify Node ID number";     
    }

    public int getMTI() { return MTI_VERIFY_NID; }
}
