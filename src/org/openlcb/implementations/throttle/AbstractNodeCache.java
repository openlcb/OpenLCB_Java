package org.openlcb.implementations.throttle;

import java.util.ArrayList;
import java.util.List;
import org.openlcb.Connection;
import org.openlcb.EventID;
import org.openlcb.MessageDecoder;
import org.openlcb.ProducerConsumerEventReportMessage;

/**
 *
 * @author Bob Jacobsen      Copyright 2012
 */
public class AbstractNodeCache extends MessageDecoder {

    public AbstractNodeCache(EventID indicator) {
        this.indicator = indicator;
    }
    ArrayList<TrainNode> list = new ArrayList<TrainNode>();
    EventID indicator;

    public List<TrainNode> getList() {
        return list;
    }

    public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg, Connection sender) {
        EventID evt = msg.getEventID();
        if (evt.equals(indicator)) {
            // get source as Node ID for train node
            TrainNode tn = new TrainNode(msg.getSourceNodeID());
            list.add(tn);
            firePropertyChange("TrainNode", null, tn);
        }
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
