package org.openlcb.timer;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class ManualClock extends Clock {
    private Instant now;
    private ZoneId zone;

    public ManualClock() {
        this(Clock.systemDefaultZone());
    }
    public ManualClock(Clock from) {
        this(from.instant(), from.getZone());
    }
    public ManualClock(Instant now, ZoneId zone) {
        this.now = now;
        this.zone = zone;
    }

    public synchronized void advance(long millis) {
        now = now.plusMillis(millis);
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zoneId) {
        return new ManualClock(now, zoneId);
    }

    @Override
    public Instant instant() {
        return now;
    }
}
