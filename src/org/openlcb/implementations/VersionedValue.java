package org.openlcb.implementations;

/**
 * Created by bracz on 12/30/15.
 */
public class VersionedValue<T> {
    T data;
    int version;
    int nextVersion;
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public static int DEFAULT_VERSION = 1;

    public VersionedValue(T t) {
        version = DEFAULT_VERSION;
        nextVersion = DEFAULT_VERSION + 1;
        data = t;
    }

    public int getNewVersion() {
        int newVersion;
        synchronized (this) {
            newVersion = ++nextVersion;
        }
        return newVersion;
    }

    public void set(T t) {
        synchronized(this) {
            int version = getNewVersion();
            set(version, t);
        }
    }

    public boolean set(int atVersion, T t) {
        T old;
        boolean updated = false;
        synchronized (this) {
            if (atVersion <= version) return false;
            int oldVersion = version;
            version = atVersion;
            if (nextVersion <= atVersion) {
                nextVersion = atVersion + 1;
            }
            if (data.equals(t) && oldVersion != DEFAULT_VERSION) {
                return true;
            }
            old = data;
            if (oldVersion == DEFAULT_VERSION) {
                old = null;
            }
            data = t;
        }
        firePropertyChange("updated", old, t);
        return true;
    }

    public T getLatestData() {
        return data;
    }

    public int getVersion() {
        return version;
    }

    public synchronized T getDataForVersion(int expectedVersion) throws VersionOutOfDateException {
        if (version != expectedVersion) {
            throw new VersionOutOfDateException();
        }
        return data;
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
