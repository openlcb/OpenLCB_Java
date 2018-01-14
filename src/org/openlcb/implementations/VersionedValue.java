package org.openlcb.implementations;

import java.util.logging.Logger;

/**
 * Created by bracz on 12/30/15.
 */
public class VersionedValue<T> {
    T data;
    int version;
    int nextVersion;
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    static int DEFAULT_VERSION = 1;
    private int defaultVersion = DEFAULT_VERSION;
    private final static Logger log = Logger.getLogger(VersionedValue.class.getCanonicalName());

    public VersionedValue(T t) {
        version = DEFAULT_VERSION;
        nextVersion = DEFAULT_VERSION + 1;
        data = t;
    }

    /**
     * @return a strictly monotonically increasing version number to be used for setting the value.
     */
    public int getNewVersion() {
        int newVersion;
        synchronized (this) {
            newVersion = ++nextVersion;
        }
        return newVersion;
    }

    /**
     * Resets the state of the listener to construction state, without changing the value. The
     * next 'set' will force updates to be called, and isVersionAtDefault will return true.
     */
    public synchronized void setVersionToDefault() {
        defaultVersion = version;
    }

    /**
     * @return true if we are currently at the default version (either just after construction or
     * due to ra eset to default call).
     */
    public synchronized boolean isVersionAtDefault() {
        return version == defaultVersion;
    }

    /**
     * Sets the current value; possibly calling listeners. Handles versioning internally.
     * @param t new value of data stored.
     */
    public void set(T t) {
        int version;
        synchronized(this) {
            version = getNewVersion();
        }
        set(version, t);
    }

    /**
     * Sets the value using a pre-requested version. The set has no effect if the version has
     * already been exceeded.
     * @param atVersion proposed new version number
     * @param t new value of data stored
     * @return true if data was updated. False if the version is already outdated and no change
     * was made.
     */
    public boolean setWithForceNotify(int atVersion, T t) {
        return setInternal(atVersion, t, true);
    }

    /**
     * Sets the value using a pre-requested version. The set has no effect if the version has
     * already been exceeded.
     * @param atVersion proposed new version number
     * @param t new value of data stored
     * @return true if data was updated. False if the version is already outdated and no change
     * was made.
     */
    public boolean set(int atVersion, T t) {
        return setInternal(atVersion, t, false);
    }

    private boolean setInternal(int atVersion, T t, boolean forceNotify) {
        T old;
        boolean updated = false;
        synchronized (this) {
            if (atVersion <= version) return false;
            int oldVersion = version;
            version = atVersion;
            if (nextVersion <= atVersion) {
                nextVersion = atVersion + 1;
            }
            if (data.equals(t) && oldVersion != defaultVersion && !forceNotify) {
                return true;
            }
            old = data;
            if (oldVersion == defaultVersion || forceNotify) {
                old = null;
            }
            data = t;
        }
        firePropertyChange("updated", old, t);
        return true;
    }

    /**
     * @return data stored at the current version.
     */
    public T getLatestData() {
        return data;
    }

    /**
     * @return current version number.
     */
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
