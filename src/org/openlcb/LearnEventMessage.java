package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Learn Event message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class LearnEventMessage extends EventMessage {
    public LearnEventMessage(NodeID source, EventID eid) {
        super(source, eid);
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
        decoder.handleLearnEvent(this, sender);
     }

    public String toString() {
        return super.toString()
                +" LearnEvent "+eventID.toString();     
    }
     
    public int getMTI() { return MTI_LEARN_EVENT; }
}
