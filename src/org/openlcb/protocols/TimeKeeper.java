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

    /// true if the clock is running
    boolean isRunning = true;
    /// the real time for which we are storing the equivalent fast time. Ignored if !isRunning. Defined as msec since epoch.
    long realTimeAnchor = currentTimeMillis();
    /// fast time matching the real time anchor, or the fast time when the clock was stopped. Defined as msec since epoch.
    long matchingFastTime = realTimeAnchor;
    /// Rate of the fast clock, may be negative.
    double rate = 1.0;
}
