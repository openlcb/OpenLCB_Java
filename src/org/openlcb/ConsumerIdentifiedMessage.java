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
public class ConsumerIdentifiedMessage extends Message {
    
    public ConsumerIdentifiedMessage(NodeID source, EventID eventID) {
        super(source);
        if (eventID == null)
            throw new IllegalArgumentException("EventID cannot be null");
        this.eventID = eventID;
    }
        
    @SuppressWarnings("JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
    EventID eventID;

    // because EventID is immutable, can directly return object
    public EventID getEventID() {
        return eventID;
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
        decoder.handleConsumerIdentified(this, sender);
     }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (! (o instanceof ConsumerIdentifiedMessage))
            return false;
        if (!super.equals(o)) return false;
        ConsumerIdentifiedMessage p = (ConsumerIdentifiedMessage) o;
        return eventID.equals(p.eventID);
    }

    @Override
    public int hashCode() {
        return super.hashCode()+eventID.hashCode();
    }
    
    @Override
    public String toString() {
        return super.toString()
                +" Consumer identified for "+eventID.toString();     
    }

    public int getMTI() { return MTI_CONSUMER_IDENTIFIED; }
}
