package org.openlcb.timer;

import org.junit.After;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.Timer;

import static org.junit.Assert.*;

public class TimerFakeTest {
    ManualClock c = new ManualClock(Instant.ofEpochMilli(123456789000L), ZoneId.systemDefault());
    TimerFake timer = new TimerFake(c);
    TestTask task1 = new TestTask();
    TestTask task2 = new TestTask();

    private static class TestTask implements Runnable {
        boolean hasRun = false;
        @Override
        public void run() {
            assertFalse(hasRun);
            hasRun = true;
        }
    }

    @After
    public void tearDown() {
        timer.dispose();
    }

    @Test
    public void scheduleRelative() {
        timer.schedule(task1, 100);
        timer.flush();
        assertFalse(task1.hasRun);
        c.advance(99);
        timer.clockUpdated();
        assertFalse(task1.hasRun);
        c.advance(1);
        timer.clockUpdated();
        assertTrue(task1.hasRun);
    }

    @Test
    public void schedulePast() {
        timer.schedule(task1, -100);
        timer.flush();
        assertTrue(task1.hasRun);

        timer.schedule(task2, 0);
        timer.flush();
        assertTrue(task2.hasRun);
    }


    @Test
    public void scheduleAbsolutePast() {
        timer.schedule(task1, Date.from(Instant.ofEpochMilli(123456789000L)));
        timer.flush();
        assertTrue(task1.hasRun);
    }

    @Test
    public void scheduleAbsoluteFuture() {
        timer.schedule(task1, new Date(123456789100L));
        timer.flush();
        assertFalse(task1.hasRun);

        c.advance(99);
        timer.clockUpdated();
        assertFalse(task1.hasRun);

        c.advance(1);
        timer.clockUpdated();
        assertTrue(task1.hasRun);
    }

    @Test
    public void withRealClock() throws InterruptedException {
        timer.dispose();
        timer = new TimerFake();
        timer.schedule(task1, 100);
        Thread.sleep(101);
        timer.flush();
        assertTrue(task1.hasRun);
    }
}