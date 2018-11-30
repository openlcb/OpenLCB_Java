package org.openlcb.protocols;

import org.openlcb.EventID;
import org.openlcb.NodeID;
import org.openlcb.PropertyListenerSupport;

/**
 * Clock interface. Functions for controlling a fast clock and listening to / getting fast
 * clock events. This interface is implemented both by the clock consumer as well as the clock
 * generator.
 *
 * Also has some static declarations regarding to the time protocol.
 *
 * Created by bracz on 9/26/18.
 */

public interface TimeProtocol extends PropertyListenerSupport {
    final static NodeID DEFAULT_CLOCK = new NodeID(new byte[]{1,1,0,0,1,0});
    final static NodeID DEFAULT_RT_CLOCK = new NodeID(new byte[]{1,1,0,0,1,1});
    final static NodeID ALT_CLOCK_1 = new NodeID(new byte[]{1,1,0,0,1,2});
    final static NodeID ALT_CLOCK_2 = new NodeID(new byte[]{1,1,0,0,1,3});

    /// Property change notification when the time gets changed.
    final static String PROP_TIME_UPDATE = "TimeUpdated";
    /// Property change notification when the rate gets updated.
    final static String PROP_RATE_UPDATE = "RateUpdated";
    /// Property change notification when the clock gets started or stopped.
    final static String PROP_RUN_UPDATE = "RunUpdated";

    final static int NIB_TIME_REPORT = 0x0;
    final static int NIB_TIME_REPORT_ALT = 0x1;
    final static int NIB_DATE_REPORT = 0x2;
    final static int NIB_YEAR_REPORT = 0x3;
    final static int NIB_RATE_REPORT = 0x4;
    final static int NIB_SET = 0x8;

    final static int QUERY_SUFFIX = 0xF000;
    final static int STOP_SUFFIX = 0xF001;
    final static int START_SUFFIX = 0xF002;
    final static int DATE_ROLLOVER = 0xF003;
    /// OR this onto a report suffix to get a set suffix.
    final static int SET_SUFFIX = NIB_SET << 12;

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

    /// Creates a suffix for reporting a year.
    /// @param year is the absolute year value (e.g. 2018).
    /// @return a clock event suffix (16-bit integer).
    static int createYear(int year) {
        return (NIB_YEAR_REPORT << 12) | (year & 0xfff);
    }

    /// Creates a suffix for reporting a month-day pair.
    /// @param month is the month of the year, 1-12.
    /// @param day is the day of the month, 1-31.
    /// @return a clock event suffix (16-bit integer).
    static int createMonthDay(int month, int day) {
        return (NIB_DATE_REPORT << 12) | ((month & 0xf) << 8) | (day & 0xff);
    }

    /// Creates a suffix for reporting an hour-minute pair.
    /// @param hrs is the hour of the day, 0..23
    /// @param min is the minute of the hour, 0..59
    /// @return a clock event suffix (16-bit integer).
    static int createHourMin(int hrs, int min) {
        return (NIB_TIME_REPORT << 12) | ((hrs & 0x1F) << 8) | (min & 0xff);
    }

    /// Creates a suffix for reporting the rate.
    /// @param rate is the fast clock rate, -512..+511.75. The value will be clipped into this
    /// range. Resolution is 0.25.
    /// @return a clock event suffix (16-bit integer).
    static int createRate(double rate) {
        int r4 = (int) (rate * 4);
        if (r4 > 0x7ff) r4 = 0x7ff;
        if (r4 < -2048) r4 = -2048;
        return (NIB_RATE_REPORT << 12) | (r4 & 0xfff);
    }

    /// Decodes a set rate suffix.
    /// @param suffix is a suffix starting with NIB_RATE_REPORT.
    /// @return decoded rate.
    static double decodeRate(int suffix) {
        int ir = suffix & 0xfff;
        // Sign-extends the 12-bit value to 32 bits.
        ir <<= (32 - 12);
        ir >>= (32 - 12);
        double r = ir;
        r /= 4;
        return r;
    }

    /// @return the current rate of the clock.
    double getRate();

    /// @return true if the clock is running.
    boolean isRunning();

    /// @return the current time on the clock (in msec since epoch).
    long getTimeInMsec();

    /// Request changing the rate of the clock.
    /// @param rate is the desired rate.
    void requestSetRate(double rate);

    /// Change the clock to be stopped.
    void requestStop();

    /// Change the clock to be running.
    void requestStart();

    /// Sets the clock to a given date and time.
    /// @param timeMsec is the time (in milliseconds since epoch).
    void requestSetTime(long timeMsec);

    /// Requests the clock master to send updates about the current state of the clock.
    void requestQuery();
}
