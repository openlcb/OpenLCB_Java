package org.openlcb.implementations;

import org.openlcb.*;

/**
 * Example of a OpenLCB node that produces one Event.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class SingleProducerNode extends SingleLinkNode {

    public SingleProducerNode(NodeID node, Connection connection, EventID eventID) {
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
        connection.put(new ProducerIdentifiedMessage(nodeID, eventID), this);
    }
    
    /**
     * For testing, send the nodes Event
     */
    public void send() {
        ProducerConsumerEventReportMessage p 
                = new ProducerConsumerEventReportMessage(nodeID, eventID);
        connection.put(p, this);
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}
}
