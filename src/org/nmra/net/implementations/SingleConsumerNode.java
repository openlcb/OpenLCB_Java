package org.nmra.net.implementations;

import org.nmra.net.*;

/**
 * Example of a NMRAnet node that consumes one Event.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class SingleConsumerNode extends SingleLinkNode {

    public SingleConsumerNode(NodeID node, Connection connection, EventID eventID) {
        super(node, connection);
        if (eventID == null)
            throw new IllegalArgumentException("EventID cannot be null");
        this.eventID = eventID;
    }
        
    EventID eventID;
    
    /**
     * Initialize this node and put it in operation
     */
    public void initialize() {
        super.initialize();
        // announce which event is being sent
        connection.put(new ConsumerIdentifiedMessage(nodeID, eventID), this);
    }
    
    /**
     * Receive ProducerConsumerEventReport messages, 
     * recording when it's proper event
     */
    @Override
    public void handleProducerConsumerEventReport(
                    ProducerConsumerEventReportMessage msg, Connection sender) {
        if (msg.getEventID().equals(eventID)) 
            received = true;
    }
    
    /**
     * Has the message been received?
     *<p>
     * Resets the value each time called
     */
    public boolean getReceived() {
        boolean retval = received;
        received = false;
        return retval;
    }
    
    boolean received = false;
}
