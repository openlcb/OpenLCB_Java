package org.nmra.net;

/**
 * Producer Identified message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class ProducerIdentifiedMessage extends Message {
    
    public ProducerIdentifiedMessage(NodeID source, EventID eventID) {
        super(source);
        if (eventID == null)
            throw new IllegalArgumentException("EventID cannot be null");
        this.eventID = eventID;
    }
        
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
        decoder.handleProducerIdentified(this, sender);
     }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        ProducerIdentifiedMessage p = (ProducerIdentifiedMessage) o;
        return eventID.equals(p.eventID);
    } 

    public String toString() {
        return getSourceNodeID().toString()
                +" Producer identified for "+eventID.toString();     
    }
}
