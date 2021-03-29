package org.openlcb;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TimerReal implements TimerInterface {
    private Timer timer;

    public TimerReal(String name) {
        timer = new Timer(name);
    }

    public Timer getTimer() {
        return timer;
    }

    @Override
    public void schedule(TimerTask task, long delayMsec) {
        timer.schedule(task, delayMsec);
    }

    @Override
    public void schedule(TimerTask task, Date absoluteTime) {
        timer.schedule(task, absoluteTime);
    }

    @Override
    public void dispose() {
        timer.cancel();
    }
}
