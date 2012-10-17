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
public class LearnEventMessage extends Message {
    
    public LearnEventMessage(NodeID source, EventID eid) {
        super(source);
        this.eventID = eid;
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
        decoder.handleLearnEvent(this, sender);
     }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        LearnEventMessage p = (LearnEventMessage) o;
        return eventID.equals(p.eventID);
    } 

    public String toString() {
        return super.toString()
                +" LearnEvent "+eventID.toString();     
    }
     
    public int getMTI() { return MTI_LEARN_EVENT; }
}
