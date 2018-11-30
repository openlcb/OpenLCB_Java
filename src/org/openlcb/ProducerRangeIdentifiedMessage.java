package org.openlcb;

// For annotations

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

/**
 * Producer Consumer Event Report message implementation
 *
 * @author  Balazs Racz   Copyright 2018
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class ProducerRangeIdentifiedMessage extends EventMessage {

    public ProducerRangeIdentifiedMessage(NodeID source, EventID eventID) {
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
        decoder.handleProducerRangeIdentified(this, sender);
    }

    public String toString() {
        return super.toString()
                +" Producer Range Identified with " + eventID.toString();
    }
    
    public int getMTI() { return MTI_PRODUCER_RANGE_IDENTIFIED; }
}
