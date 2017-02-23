package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Producer Consumer Event Report message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class ProducerConsumerEventReportMessage extends EventMessage {
    
    public ProducerConsumerEventReportMessage(NodeID source, EventID eventID) {
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
        decoder.handleProducerConsumerEventReport(this, sender);
    }

    public String toString() {
        return super.toString()
                +" Producer/Consumer Event Report with "+eventID.toString();     
    }
    
    public int getMTI() { return MTI_PC_EVENT_REPORT; }
}
