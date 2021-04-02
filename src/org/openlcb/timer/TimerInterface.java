package org.openlcb.timer;

import java.util.Date;

public interface TimerInterface {
    /// Schedules a timer task with an absolute delay from now.
    public void schedule(Runnable task, long delayMsec);

    /// Schedules a timer task at an absolute time.
    public void schedule(Runnable task, Date absoluteTime);

    /// Teardown of this object and associated resources. Can be called more than once (further
    /// calls are a no-op).
    public void dispose();
}
