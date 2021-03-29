package org.openlcb;

import java.util.Date;
import java.util.TimerTask;

public interface TimerInterface {
    /// Schedules a timer task with an absolute delay from now.
    public void schedule(TimerTask task, long delayMsec);

    /// Schedules a timer task at an absolute time.
    public void schedule(TimerTask task, Date absoluteTime);

    /// Teardown of this object and associated resources.
    public void dispose();
}
