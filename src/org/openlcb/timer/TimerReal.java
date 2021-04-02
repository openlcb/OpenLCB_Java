package org.openlcb.timer;

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
    public void schedule(Runnable task, long delayMsec) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, delayMsec);
    }

    @Override
    public void schedule(Runnable task, Date absoluteTime) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, absoluteTime);
    }

    @Override
    public void dispose() {
        timer.cancel();
    }
}
