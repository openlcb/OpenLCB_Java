package org.openlcb;

import java.util.Comparator;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.TimerTask;

public class TimerFake implements TimerInterface {
    private Thread timerThread;
    private boolean isCancelled = false;
    final PriorityQueue<TimerTask> taskQueue = new PriorityQueue<>(128, new TaskComparator());

    TimerFake() {
        timerThread = new Thread(this::threadBody,"OpenLCB Fake Timer Thread");
        timerThread.start();
    }

    private static class TaskComparator implements Comparator<TimerTask> {
        @Override
        public int compare(TimerTask t1, TimerTask t2) {
            return Long.compare(t1.scheduledExecutionTime(), t2.scheduledExecutionTime());
        }
    }

    private void threadBody() {
        while (true) {
            TimerTask head = null;
            synchronized (this) {
                if (isCancelled) {
                    return;
                }
                if (!taskQueue.isEmpty()) {
                    head = taskQueue.peek();
                }
                if (head == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                    continue;
                }
                long diff = head.scheduledExecutionTime() - currentTime();
                if (diff < 0) {
                    taskQueue.poll();
                } else {
                    head = null;
                    try {
                        wait(diff);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    continue;
                }
            }

        }
    }

    private long currentTime() {
        return System.currentTimeMillis();
    }

    @Override
    public void schedule(TimerTask task, long delayMsec) {

    }

    @Override
    public void schedule(TimerTask task, Date absoluteTime) {

    }

    @Override
    public void dispose() {
        synchronized (this) {
            isCancelled = true;
            notifyAll();
        }
    }
}
