package org.openlcb.timer;

import org.junit.Test;

import java.time.Instant;
import java.time.ZoneId;

import static org.junit.Assert.*;

public class ManualClockTest {
    ManualClock c;
    Instant base = Instant.ofEpochSecond(1617378644);
    ZoneId z = ZoneId.systemDefault();

    @Test
    public void advance() {
        c = new ManualClock(base, z);
        assertEquals(base, c.instant());
        c.advance(500);
        assertEquals(base.plusMillis(500), c.instant());
        c.advance(500);
        assertEquals(Instant.ofEpochSecond(1617378645), c.instant());
    }

    @Test
    public void getZone() {
        c = new ManualClock(base, z);
        assertEquals(ZoneId.systemDefault(), z);
    }

    @Test
    public void instant() throws InterruptedException {
        c = new ManualClock(base, z);
        assertEquals(Instant.ofEpochSecond(1617378644), c.instant());
        Thread.sleep(100);
        assertEquals(Instant.ofEpochSecond(1617378644), c.instant());
    }
}