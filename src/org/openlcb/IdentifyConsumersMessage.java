package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Identify Consumers message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class IdentifyConsumersMessage extends EventMessage {
    
    public IdentifyConsumersMessage(NodeID source, EventID event) {
        super(source, event);
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
        decoder.handleIdentifyConsumers(this, sender);
     }

    public String toString() {
        return super.toString()
                +" Identify Consumers with "+eventID.toString();     
    }

    public int getMTI() { return MTI_IDENTIFY_CONSUMERS; }
}
