package org.openlcb.protocols;

import org.openlcb.Connection;
import org.openlcb.ConsumerRangeIdentifiedMessage;
import org.openlcb.DefaultPropertyListenerSupport;
import org.openlcb.EventID;
import org.openlcb.EventState;
import org.openlcb.Message;
import org.openlcb.MessageDecoder;
import org.openlcb.MessageTypeIdentifier;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.ProducerConsumerEventReportMessage;
import org.openlcb.ProducerIdentifiedMessage;
import org.openlcb.ProducerRangeIdentifiedMessage;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.TimerTask;

import static org.openlcb.MessageTypeIdentifier.ConsumerRangeIdentified;
import static org.openlcb.MessageTypeIdentifier.ProducerConsumerEventReport;
import static org.openlcb.MessageTypeIdentifier.ProducerIdentifiedValid;
import static org.openlcb.MessageTypeIdentifier.ProducerRangeIdentified;

/**
 * Implementation of the Clock Generator feature of the Time Broadcast Protocol.
 *
 * Created by bracz on 9/30/18.
 */

public class TimeBroadcastGenerator extends DefaultPropertyListenerSupport implements TimeProtocol {

    public TimeBroadcastGenerator(OlcbInterface iface, NodeID clock) {
        this.clock = clock;
        this.timeKeeper = new TimeKeeper();
        this.fastDayLastAnnounced = timeKeeper.matchingFastTime;
        this.iface = iface;
        iface.registerMessageListener(messageHandler);
        iface.getOutputConnection().registerStartNotification(new Connection.ConnectionListener() {
            @Override
            public void connectionActive(Connection c) {
                sendStartupAction();
            }
        });
    }

    public void setTimeZone(TimeZone tz) {
        timeZone = tz;
    }

    public synchronized void dispose() {
        if (delayedSyncTask != null) {
            delayedSyncTask.cancel();
        }
        iface.unRegisterMessageListener(messageHandler);
    }

    private void sendStartupAction() {
        // Creating an event range representation depending on the lowest bit of the clock ID.
        int producerSuffix;
        if ((clock.getContents()[5] & 1) == 0) {
            producerSuffix = 0xFFFF;
        } else {
            producerSuffix = 0;
        }
        // We are consuming all events in the range.
        sendClockEvent(producerSuffix, ProducerRangeIdentified);
        // We are producing all events in the upper half ("SET" events).
        int consumerSuffix = 0x8000;
        sendClockEvent(consumerSuffix, ConsumerRangeIdentified);
    }

    /// Internal implementation that listens to incoming messages.
    private class Handler extends MessageDecoder {
        @Override
        public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg, Connection sender) {
            int d = TimeProtocol.decodeClock(msg.getEventID(), clock);
            if ((d < 0) || ((d & TimeProtocol.SET_SUFFIX) == 0)) {
                // not for us or not setting.
                return;
            }
            switch(d) {
                case QUERY_SUFFIX: {
                    triggerClockSyncNow();
                    break;
                }
                case STOP_SUFFIX: {
                    updateRunning(false);
                    triggerClockSyncIn3Sec();
                    break;
                }
                case START_SUFFIX: {
                    updateRunning(true);
                    triggerClockSyncIn3Sec();
                    break;
                }
            }
            boolean retransmitReport = true;
            switch ((d>>12) & (NIB_SET - 1)) {
                case NIB_TIME_REPORT:
                case NIB_TIME_REPORT_ALT: {
                    int hrs = (d >> 8) & 0x1f;
                    int min = d & 0xff;
                    // @todo do we need some synchronization here?
                    Calendar c = prepareTimeUpdate();
                    c.set(Calendar.HOUR_OF_DAY, hrs);
                    c.set(Calendar.MINUTE, min);
                    c.set(Calendar.SECOND, 0);
                    c.set(Calendar.MILLISECOND, 0);
                    updateTime(c.getTimeInMillis());
                    break;
                }
                case NIB_DATE_REPORT: {
                    int month = (d >> 8) & 0xf;
                    int day = d & 0xff;
                    Calendar c = prepareTimeUpdate();
                    c.set(Calendar.MONTH, month - 1);
                    c.set(Calendar.DAY_OF_MONTH, day);
                    updateTime(c.getTimeInMillis());
                    break;
                }
                case NIB_YEAR_REPORT: {
                    int year = d & 0xfff;
                    Calendar c = prepareTimeUpdate();
                    c.set(Calendar.YEAR, year);
                    updateTime(c.getTimeInMillis());
                    break;
                }
                case NIB_RATE_REPORT: {
                    double r = TimeProtocol.decodeRate(d);
                    updateRate(r);
                    break;
                }
                default:
                    retransmitReport = false;
            }
            if (retransmitReport) {
                sendClockEvent(d & ~SET_SUFFIX);
                triggerClockSyncIn3Sec();
            }
        }
    }

    /// Helper function for processing the time changed notifications.
    private Calendar prepareTimeUpdate() {
        Calendar c = Calendar.getInstance(timeZone);
        c.setTimeInMillis(getTimeInMsec());
        return c;
    }

    private synchronized void triggerClockSyncIn3Sec() {
        if (delayedSyncTask != null) {
            delayedSyncTask.cancel();
            delayedSyncTask = null;
        }
        delayedSyncTask = new TimerTask() {
            @Override
            public void run() {
                synchronized(TimeBroadcastGenerator.this) {
                    delayedSyncTask = null;
                }
                triggerClockSyncNow();
            }
        };
        iface.getTimer().schedule(delayedSyncTask, RESYNC_DELAY_MSEC);
        // @todo.
    }

    private void triggerClockSyncNow() {
        Calendar c = Calendar.getInstance(timeZone);
        c.setTimeInMillis(getTimeInMsec());
        if (isRunning()) {
            sendClockEvent(START_SUFFIX, ProducerIdentifiedValid);
        } else {
            sendClockEvent(STOP_SUFFIX, ProducerIdentifiedValid);
        }
        sendClockEvent(TimeProtocol.createRate(timeKeeper.rate), ProducerIdentifiedValid);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        sendClockEvent(TimeProtocol.createYear(year), ProducerIdentifiedValid);
        sendClockEvent(TimeProtocol.createMonthDay(month, day), ProducerIdentifiedValid);
        sendClockEvent(TimeProtocol.createHourMin(hour, min), ProducerIdentifiedValid);

    }

    private final Handler messageHandler = new Handler();
    /// The client-set timezone to use for interpreting the set time (in msec) to translate to the wire time (hh:mm).
    private TimeZone timeZone = TimeZone.getDefault();
    /// Interface we are registered to.
    private final OlcbInterface iface;
    /// Stores the prefix of the event ID that represents our clock.
    private final NodeID clock;
    /// Internal implementation for the current (fast) time.
    TimeKeeper timeKeeper;
    /// Timer task used for delaying a sync.
    TimerTask delayedSyncTask = null;
    /// Timer task used to announce midnight
    private TimerTask midnightTask = null;
    /// Real-time at which the current midnight task is scheduled.
    private long midnightScheduledTime = 0;
    /// Current day (by fast time) for the purpose of midnight announcements. This changes exactly
    /// when the midnight announcement goes out or the time jumps to a different day.
    private long fastDayLastAnnounced;
    /// This is how long we wait before sending out the the re-synchronization messages. This is not final in order to write tests that run faster.
    long RESYNC_DELAY_MSEC = 3000;

    @Override
    public double getRate() {
        return timeKeeper.rate;
    }

    @Override
    public boolean isRunning() {
        return timeKeeper.isRunning;
    }

    @Override
    public long getTimeInMsec() {
        return timeKeeper.getTime();
    }

    /// Updates internal state and property change listeners. Does not talk to the bus.
    private void updateRate(double r) {
        double oldRate;
        synchronized (this) {
            oldRate = timeKeeper.rate;
            timeKeeper.setRate(r);
            if (timeKeeper.isRunning) {
                // @todo use the anchor time the timekeeper has set as day designation? Or use
                // the last announced time's midnight? What happens when there is a race where we
                // get the synchronized lock, but the current set time is already after midnight
                // that got triggered but the timer thread is waiting for the lock?
                updateMidnightTask(0, 0);
            }
        }
        firePropertyChange(TimeProtocol.PROP_RATE_UPDATE, oldRate, r);
    }

    /// Updates, if necessary, the scheduled timer task that ticks on date rollover.
    /// @param daysDelta is +1, 0 or -1 to decide how much to move the fastDayLastAnnounced
    /// timestamp (forward a day, not move, or backwards a day).
    private synchronized void updateMidnightTask(long unused, int daysDelta) {
        // Real-time at which we want to fire the midnight task.
        long desiredTime = 0;
        long chosenFastMidnight = 0;
        if (!timeKeeper.isRunning || timeKeeper.rate == 0) {
            desiredTime = 0;
        } else {
            Calendar c = Calendar.getInstance(timeZone);
            c.setTimeInMillis(fastDayLastAnnounced);
            if (daysDelta != 0) {
                c.add(Calendar.DAY_OF_MONTH, daysDelta);
                fastDayLastAnnounced = c.getTimeInMillis();
            }
            // snaps to beginning of the day
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            // iterates to next day if moving forward
            if (timeKeeper.rate > 0) {
                // we want to round up from the current time if moving forward.
                c.add(Calendar.DAY_OF_MONTH, 1);
            }
            chosenFastMidnight = c.getTimeInMillis();
            desiredTime = timeKeeper.translateFastToRealTime(chosenFastMidnight);
            // makes midnight rollover scheduled earlier on the timer queue than any actual
            // time events.
            desiredTime -= 1;
        }
        if (desiredTime == midnightScheduledTime) return; // no need to change.
        if (midnightTask != null) {
            midnightTask.cancel();
            midnightTask = null;
        }
        midnightScheduledTime = 0;
        if (desiredTime == 0) return;
        midnightTask = new TimerTask() {
            @Override
            public void run() {
                announceMidnight(this);
            }
        };
        midnightScheduledTime = desiredTime;
        iface.getTimer().schedule(midnightTask, new Date(midnightScheduledTime));
    }

    /// Updates internal state and property change listeners. Does not talk to the bus.
    private void updateTime(long newTime) {
        long oldTime;
        synchronized (this) {
            oldTime = timeKeeper.getTime();
            timeKeeper.setTime(newTime);
            if (timeKeeper.isRunning) {
                fastDayLastAnnounced = newTime;
                updateMidnightTask(0, 0);
            }
        }
        firePropertyChange(TimeProtocol.PROP_TIME_UPDATE, oldTime, newTime);
    }

    private synchronized void announceMidnight(TimerTask self) {
        // We only run if the midnight task has not been changed from us. This is the
        // lock-protected synchronization we do to avoid outdated midnight tasks from executing.
        if (!timeKeeper.isRunning || midnightTask != self) {
            return;
        }
        sendClockEvent(TimeProtocol.DATE_ROLLOVER);
        midnightTask = null;
        int deltaDays = 0;
        if (timeKeeper.rate > 0) {
            deltaDays = 1;
        } else {
            deltaDays = -1;
        }
        updateMidnightTask(0, deltaDays);
    }

    /// Updates internal state and property change listeners. Does not talk to the bus.
    private void updateRunning(boolean r) {
        boolean lastRunning;
        synchronized(this) {
            lastRunning = timeKeeper.isRunning;
            if (r) {
                timeKeeper.start();
            } else {
                timeKeeper.stop();
            }
            updateMidnightTask(0, 0);
        }
        firePropertyChange(TimeProtocol.PROP_RUN_UPDATE, lastRunning, r);
    }

    private void sendClockEvent(int suffix) {
        sendClockEvent(suffix, ProducerConsumerEventReport);
    }

    private void sendClockEvent(int suffix, MessageTypeIdentifier mti) {
        Message m = null;
        NodeID nid = iface.getNodeId();
        EventID eid = TimeProtocol.createClockEvent(clock, suffix);
        switch(mti) {
            case ProducerConsumerEventReport: {
                m = new ProducerConsumerEventReportMessage(nid, eid);
                break;
            }
            case ProducerIdentifiedValid: {
                m = new ProducerIdentifiedMessage(nid, eid, EventState.Valid);
                break;
            }
            case ProducerRangeIdentified: {
                m = new ProducerRangeIdentifiedMessage(nid, eid);
                break;
            }
            case ConsumerRangeIdentified: {
                m = new ConsumerRangeIdentifiedMessage(nid, eid);
                break;
            }
            default:
                throw new RuntimeException("Unsupported clock event requested.");
        }
        iface.getOutputConnection().put(m, messageHandler);
    }

    /// Call from the application UI. Talks to the bus and sends property listener callback too.
    @Override
    public void requestSetRate(double rate) {
        // We round the rate first.
        double r = TimeProtocol.decodeRate(TimeProtocol.createRate(rate));
        // If the rounding ended up with zero, but the request was not zero, round to the nearest
        // non-zero.
        if (r == 0 && rate > 0) {
            r = 0.25;
        } else if (r == 0 && rate < 0) {
            r = -0.25;
        }
        updateRate(r);
        // Send out update to the bus
        int d = TimeProtocol.createRate(r);
        sendClockEvent(d);
        triggerClockSyncIn3Sec();
    }

    /// Call from the application UI. Talks to the bus and sends property listener callback too.
    @Override
    public void requestStop() {
        if (isRunning()) {
            sendClockEvent(TimeProtocol.STOP_SUFFIX);
            // will do loopback and apply the change. The loopback message will cause clock sync too.
        }
    }

    /// Call from the application UI. Talks to the bus and sends property listener callback too.
    @Override
    public void requestStart() {
        if (!isRunning()) {
            sendClockEvent(TimeProtocol.START_SUFFIX);
            // will do loopback and apply the change. The loopback message will cause clock sync too.
        }
    }

    /// Call from the application UI. Talks to the bus and sends property listener callback too.
    @Override
    public void requestSetTime(long timeMsec) {
        updateTime(timeMsec);
        Calendar c = Calendar.getInstance(timeZone);
        c.setTimeInMillis(timeMsec);
        sendClockEvent(TimeProtocol.createYear(c.get(Calendar.YEAR)));
        sendClockEvent(TimeProtocol.createMonthDay(c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)));
        sendClockEvent(TimeProtocol.createHourMin(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
        triggerClockSyncIn3Sec();
    }

    @Override
    public void requestQuery() {
        // We are the master, we don't need to go to the network to query. We send all nulls to the old value in order to force sending the update notification to the application level.
        firePropertyChange(TimeProtocol.PROP_TIME_UPDATE, null, getTimeInMsec());
        firePropertyChange(TimeProtocol.PROP_RATE_UPDATE, null, getRate());
        firePropertyChange(TimeProtocol.PROP_RUN_UPDATE, null, isRunning());
    }

    // @todo: for rollover of date: also schedule date stamp events.
    // @todo: implement listening to consumer identified and send out specific clock events.
}
