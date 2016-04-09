package org.openlcb;

/**
 * Created by bracz on 4/9/16.
 */
public class DefaultPropertyListenerSupport implements PropertyListenerSupport {
    final java.beans.PropertyChangeSupport pcs;

    public DefaultPropertyListenerSupport() {
        pcs = new java.beans.PropertyChangeSupport(this);
    }

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }
}
