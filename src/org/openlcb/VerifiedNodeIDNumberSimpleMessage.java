package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Verified Node ID Number message implementation
 * with the Simple Protocol bit set
 *
 * @author  Bob Jacobsen   Copyright 2009
 */
@Immutable
@ThreadSafe
public class VerifiedNodeIDNumberSimpleMessage extends VerifiedNodeIDNumberMessage {
    
    public VerifiedNodeIDNumberSimpleMessage(NodeID source) {
        super(source);
    }

    public String toString() {
        return super.toString()
                +" with Simple Protocol";     
    }

    public int getMTI() { return MTI_VERIFIED_NID_SIMPLE; }
}
