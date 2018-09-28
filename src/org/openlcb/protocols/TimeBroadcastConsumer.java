package org.openlcb.protocols;

import org.openlcb.Connection;
import org.openlcb.DefaultPropertyListenerSupport;
import org.openlcb.MessageDecoder;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.ProducerConsumerEventReportMessage;
import org.openlcb.ProducerIdentifiedMessage;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Implementation of the Clock Consumer feature for the Time Broadcast Protocol.
 *
 * This protocol sends time notifications using event messages. There are two types of messages arriving: Event Report that means a change in the time values (from last sent), and Producer Identified, which means it's a repeat of the last sent value. The Producer Identified messages are always clustered as a block, with the time report being the last of the block of messages.
 *
 * Created by bracz on 9/25/18.
 */

public class TimeBroadcastConsumer extends MessageDecoder implements TimeProtocol {

    TimeBroadcastConsumer(OlcbInterface iface, NodeID clock) {
        this.clock = clock;
        this.timeKeeper = new TimeKeeper();
        this.iface = iface;
        iface.registerMessageListener(this);
        lastReportedTime.set(Calendar.SECOND, 0);
        lastReportedTime.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Overrides default time zone. If never called, uses computer's local time.
     * @param tz This timezone will be used to convert the date and time values coming on
     *           the wire into a long (milliseconds since epoch).
     */
    public void setTimeZone(TimeZone tz) {
        timeZone = tz;
        lastReportedTime.setTimeZone(tz);
    }

    /// De-registers message listeners to prepare for deallocating this object.
    public void dispose() {
        iface.unRegisterMessageListener(this);
    }

    /**
     * Handles an incoming notification from the clock master.
     * @param d event suffix of the clock event (that belongs to our clock of interest).
     * @param force true for event report, false for identified true.
     */
    private void handleTimeEvent(int d, boolean force) {
        switch (d) {
            case TimeProtocol.STOP_SUFFIX: {
                timeKeeper.stop();
                break;
            }
            case TimeProtocol.START_SUFFIX: {
                timeKeeper.start();
                break;
            }
            case TimeProtocol.DATE_ROLLOVER: {
                // This is an advisory message that we are moving over to the next day.
                lastReportedTime.add(Calendar.DAY_OF_MONTH, 1);
                break;
            }
        }
        switch (d >> 12) {
            case NIB_TIME_REPORT:
            case NIB_TIME_REPORT_ALT: {
                int hrs = d>>8;
                int min = d & 0xff;
                // We force the time because this is an event report.
                setHoursMins(hrs, min, force);
                break;
            }
            case NIB_DATE_REPORT: {
                int month = (d>>8) & 0xf;
                int day = d & 0xff;
                setDate(month, day, force);
                break;
            }
            case NIB_YEAR_REPORT: {
                int year = d & 0xfff;
                setYear(year, force);
                break;
            }
            case NIB_RATE_REPORT: {
                int ir = d & 0xfff;
                // Sign-extends the 12-bit value to 32 bits.
                ir <<= (32-12);
                ir >>= (32-12);
                double r = ir;
                r /= 4;
                timeKeeper.setRate(r);
                break;
            }
            // We ignore all other nibble values.
        }

    }

    @Override
    public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg, Connection sender) {
        int d = TimeProtocol.decodeClock(msg.getEventID(), clock);
        if (d < 0) {
            // not for us
            return;
        }
        handleTimeEvent(d, true);
    }

    @Override
    public void handleProducerIdentified(ProducerIdentifiedMessage msg, Connection sender) {
        int d = TimeProtocol.decodeClock(msg.getEventID(), clock);
        if (d < 0) {
            // not for us
            return;
        }
        handleTimeEvent(d, false);
    }

    /**
     * Sets the internal clock's hours and minutes value.
     * @param hrs 0-23 the current hours
     * @param min 0-59 the current minutes
     * @param force true if the set should happen always (i.e. that minute just ticked), false if this is an advisory (i.e. we may be anywhere in that minute).
     */
    private void setHoursMins(int hrs, int min, boolean force) {
        Calendar c = lastReportedTime;
        c.set(Calendar.HOUR_OF_DAY, hrs);
        c.set(Calendar.MINUTE, min);
        long newTime = c.getTimeInMillis();
        // We only set the fast clock if the actual time is more than one minute apart. Otherwise we would be better off with a bit of skew.
        long ctime = timeKeeper.getTime();
        if ((Math.abs(ctime - newTime) > 120 * 1000) || force) {
            timeKeeper.setTime(newTime);
        } else {
            // @todo apply skew to the time keeper.
        }
    }

    /**
     * Sets the month-day of the clock.
     * @param month month, 1-12.
     * @param day day of month, 1-31
     * @param force true if this is a set, false if it's advisory.
     */
    private void setDate(int month, int day, boolean force) {
        Calendar c = lastReportedTime;
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, day);

        if (force) {
            // Apply also to running clock.
            c = Calendar.getInstance(timeZone);
            long ctime = timeKeeper.getTime();
            c.setTimeInMillis(ctime);
            c.set(Calendar.MONTH, month - 1);
            c.set(Calendar.DAY_OF_MONTH, day);
            long newTime = c.getTimeInMillis();
            if (Math.abs(newTime - ctime) > 60*1000) {
                timeKeeper.setTime(newTime);
            }
        }
    }

    /**
     * Sets the year.
     * @param year year number (absolute).
     * @param force if true, sets time; if false, makes it advisory.
     */
    private void setYear(int year, boolean force) {
        Calendar c = lastReportedTime;
        c.set(Calendar.YEAR, year);

        if (force) {
            // Apply also to running clock.
            long ctime = timeKeeper.getTime();
            c = Calendar.getInstance(timeZone);
            c.setTimeInMillis(ctime);
            c.set(Calendar.YEAR, year);
            long newTime = c.getTimeInMillis();
            // Only apply year change if we are indeed away from the current.
            if (Math.abs(newTime - ctime) > 60*1000) {
                timeKeeper.setTime(newTime);
            }
        }
    }

    /// Stores the individual fields of the last reported time, collecting the varous events coming from the network.
    private Calendar lastReportedTime = Calendar.getInstance();
    /// if not null, the client has set the timezone.
    private TimeZone timeZone = TimeZone.getDefault();
    /// Interface we are registered to.
    private final OlcbInterface iface;
    /// Storesthe prefix of the event ID that represents our clock.
    private final NodeID clock;
    /// Internal implementation for the current (fast) time.
    TimeKeeper timeKeeper;
}
