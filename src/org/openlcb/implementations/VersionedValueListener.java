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

    /** Stops listening to changes of the parent. Call this before abandoning the instance. */
    public void release() {
        parent.removePropertyChangeListener(this);
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

    /**
     * Sets the value of the shared state as seen from this listener. Will skip callback to this
     * listener.
     * @param t new value
     */
    public void setFromOwner(T t) {
        int newVersion;
        synchronized (this) {
            newVersion = parent.getNewVersion();
            ownerVersion = newVersion;
        }
        parent.set(newVersion, t);
    }

    /**
     * Sets the value of the shared state as seen from this listener. Will skip callback to this
     * listener. Will force callbacks to all other listeners, even if the state has not changed.
     * @param t new value
     */
    public void setFromOwnerWithForceNotify(T t) {
        int newVersion;
        synchronized (this) {
            newVersion = parent.getNewVersion();
            ownerVersion = newVersion;
        }
        parent.setWithForceNotify(newVersion, t);
    }

    /** Calls the updater with the latest data. */
    public void pingUpdater() {
        update(parent.getLatestData());
    }

    /** Called when the backend has to update its version.
     * @param t value as of the new version.
     * */
    public abstract void update(T t);
}
