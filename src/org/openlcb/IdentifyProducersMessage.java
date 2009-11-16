package org.openlcb;

/**
 * Identify Producers message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class IdentifyProducersMessage extends Message {
    
    public IdentifyProducersMessage(NodeID source, EventID eventID) {
        super(source);
        this.eventID = eventID;
    }
        
    EventID eventID;
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

     /**
      * To be equal, messages have to have the
      * same type and content
      */
     @Override
     public boolean equals(Object o) {
        if (!super.equals(o)) return false; // also checks type
        IdentifyProducersMessage msg = (IdentifyProducersMessage) o;
        if (! this.eventID.equals(msg.eventID))
            return false;
        return true;
     }

    public String toString() {
        return getSourceNodeID().toString()
                +" Identify Producers with "+eventID.toString();     
    }
}
