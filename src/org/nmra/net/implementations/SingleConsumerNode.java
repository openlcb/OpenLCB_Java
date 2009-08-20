package org.nmra.net.implementations;

import org.nmra.net.*;

/**
 * Example of a NMRAnet node that consumes one Event.
 *<p>
 * The event doesn't cause much to happen, but e.g.
 * a {@link org.nmra.net.swing.ConsumerPane} can display
 * it.
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
    public EventID getEventID() { return eventID; }
    public void setEventID(EventID eid) { 
        eventID = eid; 
        firePropertyChange("EventID", null, eventID);
    }

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
        if (msg.getEventID().equals(eventID)) {
            received = true;
            firePropertyChange("Event", null, msg);
        }
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
    
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    boolean received = false;

}
