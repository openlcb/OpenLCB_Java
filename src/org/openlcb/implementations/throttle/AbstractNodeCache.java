package org.openlcb.implementations.throttle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.openlcb.Connection;
import org.openlcb.EventID;
import org.openlcb.NodeID;
import org.openlcb.MessageDecoder;
import org.openlcb.ProducerConsumerEventReportMessage;
import org.openlcb.ProducerIdentifiedMessage;

/**
 * Maintains a cache of nodes seen to emit a particular EventID.
 *
 * @TODO:  Type of node needs to be made generic T
 *
 * @TODO: make sure name and semantics of contained property are correct for a Bean, e.g.
 * it has the right name and related methods are present.
 *     http://docs.oracle.com/javase/tutorial/javabeans/writing/properties.html
 * How are indexed properties handled in Events?
 *
 * @author Bob Jacobsen      Copyright 2012
 */
public abstract class AbstractNodeCache<T> extends MessageDecoder {

    public static final String UPDATE_PROP_CACHE = "cache";

    public AbstractNodeCache(EventID indicator) {
        this.indicator = indicator;
    }
    // Stores which nodes we've seen so far and at what index they are.
    HashMap<NodeID, Integer> index = new HashMap<>();
    ArrayList<T> list = new ArrayList<T>();
    EventID indicator;

    // for indexed Bean form
    public T[] getCache() {
        return (T[])list.toArray();
    }
    public T getCache(int index) throws ArrayIndexOutOfBoundsException {
        return list.get(index);
    }


    public List<T> getList() {
        return list;
    }

    @Override
    public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg, Connection sender) {
        EventID evt = msg.getEventID();
        processEvent(msg.getSourceNodeID(), evt);
    }

    private synchronized void processEvent(NodeID src, EventID evt) {
        if (!evt.equals(indicator)) {
            return;
        }
        if (index.containsKey(src)) {
            return;
        }
        // get source as Node ID for train node
        T node = newObject(src);
        list.add(node);
        int last = list.size() - 1;
        index.put(src, last);
        firePropertyChange(UPDATE_PROP_CACHE, null, node);
    }

    @Override
    public void handleProducerIdentified(ProducerIdentifiedMessage msg, Connection sender) {
        EventID evt = msg.getEventID();
        processEvent(msg.getSourceNodeID(), evt);
    }

    /**
     * Implement this for specific type to be created in this cache - inelegant!
     */
    protected abstract T newObject(NodeID id);

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}
}
