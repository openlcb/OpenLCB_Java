package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Producer Identified message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class ProducerIdentifiedMessage extends EventMessage {
    public ProducerIdentifiedMessage(NodeID source, EventID eventID, EventState eventState) {
        super(source, eventID);
        this.eventState = eventState;
    }

    private final EventState eventState;

    public EventState getEventState() { return eventState; }

    /**
     * Implement message-type-specific
     * processing when this message
     * is received by a node.
     *<p>
     * Default is to do nothing.
     */
     @Override
     public void applyTo(MessageDecoder decoder, Connection sender) {
        decoder.handleProducerIdentified(this, sender);
     }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        ProducerIdentifiedMessage p = (ProducerIdentifiedMessage) o;
        return eventState == p.eventState;
    } 

    public String toString() {
        return super.toString() + " Producer Identified " + eventState.toString() + " for " + eventID.toString();
    }

    public int getMTI() { return MTI_PRODUCER_IDENTIFIED; }
}
