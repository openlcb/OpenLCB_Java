package org.openlcb.timer;

import java.time.Clock;
import java.util.Date;
import java.util.PriorityQueue;

public class TimerFake implements TimerInterface {
    private enum ThreadState {
        STARTED,
        CANCEL,
        STOPPED
    }
    private Thread timerThread;
    private ThreadState threadState = ThreadState.STARTED;
    private boolean isRunningCallback = false;
    private final PriorityQueue<TimerTask> taskQueue = new PriorityQueue<TimerTask>(128);
    private final Clock injectedClock;

    TimerFake() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Constructs the fake timer with an injected clock.
     * @param clk injected clock.
     */
    TimerFake(Clock clk) {
        timerThread = new Thread(this::threadBody,"OpenLCB Fake Timer Thread");
        timerThread.start();
        threadState = ThreadState.STARTED;
        this.injectedClock = clk;
    }

    private static class TimerTask implements Comparable<TimerTask> {
        Runnable r;
        long scheduledExecutionTime;

        TimerTask(Runnable r, long scheduledExecutionTime) {
            this.r = r;
            this.scheduledExecutionTime = scheduledExecutionTime;
        }

        @Override
        public int compareTo(TimerTask otherTask) {
            return Long.compare(scheduledExecutionTime, otherTask.scheduledExecutionTime);
        }
    }

    private void threadBody() {
        while (true) {
            TimerTask head = null;
            synchronized (this) {
                isRunningCallback = false;
                if (threadState == ThreadState.CANCEL) {
                    threadState = ThreadState.STOPPED;
                    notifyAll();
                    return;
                }
                if (taskQueue.isEmpty()) {
                    notifyAll();
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                    continue;
                }
                head = taskQueue.peek();
                long diff = head.scheduledExecutionTime - currentTime();
                if (diff <= 0) {
                    // head of queue is in the past
                    taskQueue.poll();
                } else {
                    // head of queue is in the future; we need to wait for it but not remove it
                    // from the queue.
                    head = null;
                    notifyAll();
                    try {
                        wait(diff);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                    continue;
                }
                isRunningCallback = true;
            }
            head.r.run();
        }
    }

    private long currentTime() {
        return injectedClock.millis();
        //return System.currentTimeMillis();
    }

    /// Call this function when an injected fake clock is updated.
    public void clockUpdated() {
        synchronized(this) {
            // wakes up sleeping thread
            notifyAll();
        }
        flush();
    }

    /// Waits for all past / expired tasks to be completed.
    public void flush() {
        while (true) {
            synchronized (this) {
                if (threadState != ThreadState.STARTED) {
                    return;
                }
                TimerTask t = taskQueue.peek();
                if (!isRunningCallback && (t == null || t.scheduledExecutionTime > currentTime())) {
                    return;
                }
                try {
                    wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

    @Override
    public synchronized void schedule(Runnable task, long delayMsec) {
        long when = currentTime() + delayMsec;
        taskQueue.add(new TimerTask(task, when));
        notifyAll();
    }

    @Override
    public synchronized void schedule(Runnable task, Date absoluteTime) {
        long when = absoluteTime.getTime();
        taskQueue.add(new TimerTask(task, when));
        notifyAll();
    }

    @Override
    public synchronized void dispose() {
        if (threadState == ThreadState.STARTED) {
            threadState = ThreadState.CANCEL;
        }
        notifyAll();
        while (threadState != ThreadState.STOPPED) {
            try {
                wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}
