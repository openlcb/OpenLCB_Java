package org.openlcb.protocols;

/**
 * This class maintains the local state of a fast clock, and extrapolates the current (fast) time. It is an internal implementation of the clock generator and receiver.
 *
 * This class does not specify which timezone the current time is in. Clients may use it with local time or UTC.
 *
 * Created by bracz on 9/26/18.
 */

class TimeKeeper {
    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /// @return the current fast time.
    public long getTime() {
        return getTime(currentTimeMillis());
    }

    private long getTime(long current) {
        if (!isRunning) {
            return matchingFastTime;
        }
        long diff = current - realTimeAnchor;
        long t = matchingFastTime + (long)(rate * diff);
        return t;
    }

    /// Sets the current fast time. @param millis is time since epoch in milliseconds.
    public synchronized void setTime(long millis) {
        realTimeAnchor = currentTimeMillis();
        matchingFastTime = millis;
    }

    /// Stops the clock. Noop if the clock is already stopped.
    public synchronized void stop() {
        if (!isRunning) return;
        long t = getTime();
        matchingFastTime = t;
        isRunning = false;
    }

    /// Starts the clock. Noop if the clock is already running.
    public synchronized void start() {
        if (isRunning) return;
        realTimeAnchor = currentTimeMillis();
        isRunning = true;
    }

    /// Sets the rate of the clock. The clock may be stopped or running.
    public synchronized void setRate(double r) {
        if (rate == r) return;
        long c = currentTimeMillis();
        // need to re-anchor the clock first.
        long t = getTime(c);
        realTimeAnchor = c;
        matchingFastTime = t;
        // then we can set the rate.
        rate = r;
    }

    /// Translates a fast time timestamp to a real time when we will reach it (or have reached it).
    /// @param fastTime is a millisecond timestamp in the fast time that is in the future
    /// (i.e. ahead of getTime() if rate is positive, or below it if negative).
    /// @return -1 if the clock is stopped or rate is zero. Otherwise it is
    /// a real time millisecond timestamp when the given fast time will be reached.
    public long translateFastToRealTime(long fastTime) {
        if (!isRunning || rate == 0) return -1;
        double delta = fastTime - matchingFastTime;
        long deltaRealTime = (long) (delta / rate);
        return realTimeAnchor + deltaRealTime;
    }

    /// true if the clock is running
    boolean isRunning = true;
    /// the real time for which we are storing the equivalent fast time. Ignored if !isRunning. Defined as msec since epoch.
    long realTimeAnchor = currentTimeMillis();
    /// fast time matching the real time anchor, or the fast time when the clock was stopped. Defined as msec since epoch.
    long matchingFastTime = realTimeAnchor;
    /// Rate of the fast clock, may be negative.
    double rate = 1.0;
}
