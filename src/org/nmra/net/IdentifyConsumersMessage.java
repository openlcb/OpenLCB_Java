package org.nmra.net;

/**
 * Identify Consumers message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class IdentifyConsumersMessage extends Message {
    
    public IdentifyConsumersMessage(NodeID source, EventID event) {
        super(source);
        this.eventID = event;
    }
    
    EventID eventID = null;
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

     /**
      * To be equal, messages have to have the
      * same type and content
      */
     @Override
     public boolean equals(Object o) {
        if (!super.equals(o)) return false; // also checks type
        IdentifyConsumersMessage msg = (IdentifyConsumersMessage) o;
        if (! this.eventID.equals(msg.eventID))
            return false;
        return true;
     }

    public String toString() {
        return getSourceNodeID().toString()
                +" Identify Consumers with "+eventID.toString();     
    }
}
