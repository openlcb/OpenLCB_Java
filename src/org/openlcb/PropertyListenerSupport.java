package org.openlcb;

/**
 * Created by bracz on 4/9/16.
 */
public interface PropertyListenerSupport {
    void addPropertyChangeListener(java.beans.PropertyChangeListener l);
    void removePropertyChangeListener(java.beans.PropertyChangeListener l);
}
