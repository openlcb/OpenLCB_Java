package org.openlcb;

// For annotations
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

/**
 * Consumer Identified message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class ConsumerIdentifiedMessage extends EventMessage {
    
    public ConsumerIdentifiedMessage(NodeID source, EventID eventID, EventState eventState) {
        super(source, eventID);
        this.eventState = eventState;
    }

    private final EventState eventState;

    // because EventID is immutable, can directly return object
    public EventID getEventID() {
        return eventID;
    }

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
        decoder.handleConsumerIdentified(this, sender);
     }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        ConsumerIdentifiedMessage p = (ConsumerIdentifiedMessage) o;
        return eventState == p.eventState;
    }

    @Override
    public String toString() {
        return super.toString() + " Consumer Identified " + eventState.toString() + " for "+eventID.toString();
    }

    public int getMTI() { return MTI_CONSUMER_IDENTIFIED; }
}
