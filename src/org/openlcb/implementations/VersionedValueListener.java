package org.openlcb.implementations;

import org.openlcb.implementations.VersionedValue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by bracz on 12/30/15.
 */
public abstract class VersionedValueListener<T> implements PropertyChangeListener {
    int ownerVersion;
    protected VersionedValue<T> parent;

    public VersionedValueListener(VersionedValue<T> parent) {
        this.parent = parent;
        parent.addPropertyChangeListener(this);
        ownerVersion = parent.getVersion();
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        int pVersion = parent.getVersion();
        boolean isUpdated = false;
        synchronized (this) {
            if (pVersion > ownerVersion) {
                ownerVersion = pVersion;
                isUpdated = true;
            }
        }
        if (isUpdated) {
            update(parent.getLatestData());
        }
    }

    public void setFromOwner(T t) {
        int newVersion;
        synchronized (this) {
            newVersion = parent.getNewVersion();
            ownerVersion = newVersion;
        }
        parent.set(newVersion, t);
    }

    /// Called when the backend has to update its version.
    public abstract void update(T t);
}
