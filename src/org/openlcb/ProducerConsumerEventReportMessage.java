package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Producer Consumer Event Report message implementation
 *
 * This goes to some trouble to _not_ have an internal list, 
 * unless one is specified, for performance reasons
 *
 * @author  Bob Jacobsen   Copyright 2009, 2023
 */
@Immutable
@ThreadSafe
public class ProducerConsumerEventReportMessage extends EventMessage {
    
    public ProducerConsumerEventReportMessage(NodeID source, EventID eventID) {
        super(source, eventID);
        payload = null;
    }

    public ProducerConsumerEventReportMessage(NodeID source, EventID eventID, List<Integer> payload) {
        super(source, eventID);
        this.payload = payload;
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
        decoder.handleProducerConsumerEventReport(this, sender);
    }
    
    List<Integer> payload;
    
    /**
     * Get the size of the payload, which doesn't include
     * the eight bytes of the event ID itself
     */
    public int getPayloadSize() {
        if (payload == null) return 0;
        return payload.size();
    }

    /**
     * Get the payload
     * @return unmodifiable list
     */
    @NonNull
    public List getPayload() {
        if (payload == null) return Collections.unmodifiableList(new ArrayList<Integer>()); // zero length list by default
        return Collections.unmodifiableList(payload);
    }
    
    @Override
    public String toString() {
        return super.toString()
                +" Producer/Consumer Event Report  "+eventID.toString()
                +" payload of "+getPayloadSize();     
    }
    
    public boolean equals(Object o) {
        if (!super.equals(o)) return false; // checks eventID
        if (!(o instanceof ProducerConsumerEventReportMessage)) return false;
        ProducerConsumerEventReportMessage p = (ProducerConsumerEventReportMessage) o;
        if (payload == null) {
            // check for empty payload other end
            if (p.payload == null || p.payload.size() == 0) return true;
        }
        return (getPayload().equals(p.getPayload()) );
    }

    @Override
    public int hashCode() {
        int payloadHash = 0;
        if (payload != null && payload.size() != 0) payloadHash = payload.hashCode();
        return super.hashCode() | payloadHash;
    }
    
    public int getMTI() { return MTI_PC_EVENT_REPORT; }
}
