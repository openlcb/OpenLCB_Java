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
 * @author  Bob Jacobsen   Copyright 2009, 2023
 */
@Immutable
@ThreadSafe
public class ProducerConsumerEventReportMessage extends EventMessage {
    
    public ProducerConsumerEventReportMessage(NodeID source, EventID eventID) {
        super(source, eventID);
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
    
    private List<Integer> payload = null;
    
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
        if (payload == null) payload = new ArrayList<Integer>(); // zero length list by default
        return Collections.unmodifiableList(payload);
    }
    
    public String toString() {
        return super.toString()
                +" Producer/Consumer Event Report  "+eventID.toString()
                +" payload of "+getPayloadSize();     
    }
    
    public int getMTI() { return MTI_PC_EVENT_REPORT; }
}
