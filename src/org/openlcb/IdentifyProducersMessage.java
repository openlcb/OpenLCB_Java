package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Identify Producers message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class IdentifyProducersMessage extends EventMessage {
    public IdentifyProducersMessage(NodeID source, EventID eventID) {
        super(source, eventID);
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
        decoder.handleIdentifyProducers(this, sender);
     }

    public String toString() {
        return super.toString()
                +" Identify Producers with "+eventID.toString();     
    }

    public int getMTI() { return MTI_IDENTIFY_PRODUCERS; }
}
