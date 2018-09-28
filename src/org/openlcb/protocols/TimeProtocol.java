package org.openlcb.protocols;

import org.openlcb.EventID;
import org.openlcb.NodeID;

/**
 * Created by bracz on 9/26/18.
 */

public interface TimeProtocol {
    final static NodeID DEFAULT_CLOCK = new NodeID(new byte[]{1,1,0,0,1,0});
    final static NodeID DEFAULT_RT_CLOCK = new NodeID(new byte[]{1,1,0,0,1,1});
    final static NodeID ALT_CLOCK_1 = new NodeID(new byte[]{1,1,0,0,1,2});
    final static NodeID ALT_CLOCK_2 = new NodeID(new byte[]{1,1,0,0,1,3});

    final static int QUERY_SUFFIX = 0xF000;
    final static int STOP_SUFFIX = 0xF001;
    final static int START_SUFFIX = 0xF002;
    final static int DATE_ROLLOVER = 0xF003;

    final static int NIB_TIME_REPORT = 0x0;
    final static int NIB_TIME_REPORT_ALT = 0x1;
    final static int NIB_DATE_REPORT = 0x2;
    final static int NIB_YEAR_REPORT = 0x3;
    final static int NIB_RATE_REPORT = 0x4;
    final static int NIB_SET = 0x8;

    /// Tries to decode a clock event.
    /// @param event is the incoming event from the bus
    /// @param expectedClock is the clock we are trying to decode for.
    /// @return -1 if te event is unrelated to that clock, otherwise the last 16 bits (the clock suffix).
    static int decodeClock(EventID event, NodeID expectedClock) {
        if (!event.startsWith(expectedClock)) return -1;
        byte[] cnt = event.getContents();
        int r = cnt[6];
        r &= 0xff;
        r <<= 8;
        r |= (cnt[7] & 0xff);
        return r;
    }

    /// Assembles a clock control event ID.
    /// @param clock is the clock to control.
    /// @param suffix is the last two bytes (from 0 to 65535).
    static EventID createClockEvent(NodeID clock, int suffix) {
        return new EventID(clock, (suffix >> 8) & 0xff, suffix & 0xff);
    }
}
