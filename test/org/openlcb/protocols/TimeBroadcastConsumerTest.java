package org.openlcb.protocols;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.internal.matchers.LessOrEqual;
import org.openlcb.InterfaceTestBase;
import org.openlcb.MockPropertyChangeListener;

import java.util.Calendar;
import java.util.TimeZone;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created by bracz on 9/27/18.
 */
public class TimeBroadcastConsumerTest extends InterfaceTestBase {
    @Before
    public void SetUp() {
        tcslave = new TimeBroadcastConsumer(iface, TimeProtocol.ALT_CLOCK_1);
        expectFrame(":X194A4333N010100000102FFFF;");
        expectFrame(":X19524333N0101000001028000;");
        expectNoFrames();
    }

    @After
    public void TearDown() {
        tcslave.dispose();
    }

    @Test
    public void testStartStop() throws Exception {
        sendFrame(":X195B4444N010100000102F001;");
        assertFalse(tcslave.timeKeeper.isRunning);
        sendFrame(":X195B4444N010100000102F002;");
        assertTrue(tcslave.timeKeeper.isRunning);
        // wrong clock
        sendFrame(":X195B4444N010100000101F001;");
        assertTrue(tcslave.timeKeeper.isRunning);
    }

    @Test
    public void testRate() throws Exception {
        TimeKeeperTest.FakeTimeKeeper tk = new TimeKeeperTest.FakeTimeKeeper();
        tcslave.timeKeeper = tk;
        sendFrame(":X195B4444N010100000102F001;"); // stop

        long t1 = tcslave.timeKeeper.getTime();
        sendFrame(":X19544444N01010000010247F1;"); // rate = 508.25
        sendFrame(":X195B4444N010100000102F002;"); // start
        tk.overrideTime += 1000; // wait some time
        long t2 =  tcslave.timeKeeper.getTime();
        assertEquals(t1 + 508250, t2);

        sendFrame(":X195B4444N0101000001024FD8;"); // rate = -10
        tk.overrideTime += 1000; // wait some time
        long t3 = tcslave.timeKeeper.getTime();
        assertEquals(-10000, t3-t2);
    }

    @Test
    public void testSynchronize() throws Exception {
        // stop
        sendFrame(":X195B4444N010100000102F001;");

        sendFrame(":X195B4444N01010000010237A1;"); // 1953
        sendFrame(":X195B4444N010100000102291B;"); // 09/27
        sendFrame(":X195B4444N010100000102111E;"); // 17:30

        long ctime = tcslave.timeKeeper.getTime();
        long expected = -513153000L * 1000L; // this is in GMT
        // long expected = -513156600L * 1000L; // this is in CET
        System.err.println("actual: " + ctime + " diff: " + (ctime-expected));
        System.err.println("expect: " + expected);
        StringBuffer strdiff = new StringBuffer();
        long diff = ctime - expected;
        strdiff.insert(0, (diff % 1000) + "msec");
        diff /= 1000;
        strdiff.insert(0, (diff % 60) + "sec ");
        diff /= 60;
        strdiff.insert(0, (diff % 60) + "min ");
        diff /= 60;
        strdiff.insert(0, (diff % 24) + "hours ");
        diff /= 24;
        strdiff.insert(0, diff + "d ");
        System.err.println("diff: " + strdiff.toString());

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ctime);
        strdiff = new StringBuffer();
        strdiff.append("y:" + c.get(Calendar.YEAR));
        strdiff.append(" m:" + c.get(Calendar.MONTH) + 1);
        strdiff.append(" d:" + c.get(Calendar.DAY_OF_MONTH));
        strdiff.append(" h:" + c.get(Calendar.HOUR_OF_DAY));
        strdiff.append(" m:" + c.get(Calendar.MINUTE));
        System.err.println("actual: " + strdiff.toString());
        assertEquals(17, c.get(Calendar.HOUR_OF_DAY));
        // The real numeric value might be off due to timezone variations.
        assertThat(ctime,greaterThanOrEqualTo(expected - 12*3600*1000));
        assertThat(ctime,lessThanOrEqualTo(expected + 12*3600*1000));
    }

    @Test
    public void testSyncWithProducer() throws Exception {
        tcslave.setTimeZone(TimeZone.getTimeZone("GMT"));
        // stop
        sendFrame(":X195B4444N010100000102F001;");

        sendFrame(":X19544444N01010000010247F1;"); // rate = 508.25
        sendFrame(":X19544444N01010000010237A1;"); // 1953
        sendFrame(":X19544444N010100000102291B;"); // 09/27
        sendFrame(":X19544444N010100000102111E;"); // 17:30

        long ctime = tcslave.timeKeeper.getTime();
        long expected = -513153000L * 1000L; // this is in GMT
        assertThat(ctime,equalTo(expected));
        assertEquals(508.25, tcslave.timeKeeper.rate, 0.001);
        assertFalse(tcslave.timeKeeper.isRunning);
    }

    @Test
    public void testSyncWithProducerStartup() throws Exception {
        TimeKeeperTest.FakeTimeKeeper tk = new TimeKeeperTest.FakeTimeKeeper();
        tcslave.timeKeeper = tk;
        tcslave.setTimeZone(TimeZone.getTimeZone("GMT"));

        sendFrame(":X19544444N010100000102F002;"); // start
        tk.overrideTime += 1; // 1 msec between packets.
        sendFrame(":X19544444N01010000010247F1;"); // rate = 508.25
        tk.overrideTime += 1; // 1 msec between packets.
        sendFrame(":X19544444N01010000010237A1;"); // 1953
        tk.overrideTime += 1; // 1 msec between packets.
        sendFrame(":X19544444N010100000102291B;"); // 09/27
        tk.overrideTime += 1; // 1 msec between packets.
        sendFrame(":X19544444N010100000102111E;"); // 17:30
        tk.overrideTime += 1; // 1 msec between packets.

        long ctime = tcslave.timeKeeper.getTime();
        long expected = -513153000L * 1000L; // this is in GMT
        // We are somewhat close -- really 1 mec * 58.25 away.
        assertThat(ctime - expected,greaterThanOrEqualTo(-600L));
        assertThat(ctime - expected,lessThanOrEqualTo(600L));
        // say we had 30 second beyond 17:30 during startup. Then 30 fast sec later the next minute
        // event will come. 30 * 1000 / 508.25 == 59 (msec), 1 msec already passed.
        tk.overrideTime += 58;
        ctime = tcslave.timeKeeper.getTime();
        expected = -513152940L * 1000; // 17:31
        assertThat(ctime - expected,greaterThanOrEqualTo(-60000L)); // one minute accuracy
        assertThat(ctime - expected,lessThanOrEqualTo(60000L));
        // This is the final synchronization event.
        sendFrame(":X195B4444N010100000102111F;"); // 17:31
        ctime = tcslave.timeKeeper.getTime();
        assertThat(ctime - expected,greaterThanOrEqualTo(0L)); // snap fully
        assertThat(ctime - expected,lessThanOrEqualTo(0L));
        tk.overrideTime += 59;
        ctime = tcslave.timeKeeper.getTime();
        expected += 30 * 1000; // half a minute later
        assertThat(ctime - expected,greaterThanOrEqualTo(-2000L));
        assertThat(ctime - expected,lessThanOrEqualTo(2000L));
    }

    @Test
    public void testMidnightRollover() throws Exception {
        TimeKeeperTest.FakeTimeKeeper tk = new TimeKeeperTest.FakeTimeKeeper();
        tcslave.timeKeeper = tk;
        tcslave.setTimeZone(TimeZone.getTimeZone("GMT"));

        sendFrame(":X19544444N010100000102F002;"); // start
        sendFrame(":X19544444N0101000001024028;"); // rate = 10
        sendFrame(":X19544444N01010000010237A1;"); // 1953
        sendFrame(":X19544444N010100000102291B;"); // 09/27
        sendFrame(":X19544444N010100000102111E;"); // 17:30

        long expected = -513153000L * 1000L; // this is in GMT
        assertEquals((double)expected, (double)tcslave.timeKeeper.getTime(), 0);
        // get close to midnight.
        tk.overrideTime += 6.5 * 3600 * 1000 / 10 - 20;
        long midnight = -513129600L * 1000; // midnight
        assertEquals((double)midnight,  (double)tcslave.timeKeeper.getTime(), 250);
        // We get 23:59, so time will roll back a bit.
        sendFrame(":X195B4444N010100000102173b;"); // 23:59
        expected = -513129660L * 1000;
        assertEquals((double)expected,  (double)tcslave.timeKeeper.getTime(), 1);
        // Try time rollback after midnight has been passed
        tk.overrideTime += 2 * 60 * 1000 / 10;
        assertEquals((double)expected + 120e3,  (double)tcslave.timeKeeper.getTime(), 1);
        sendFrame(":X195B4444N010100000102173b;"); // 23:59
        assertEquals((double)expected,  (double)tcslave.timeKeeper.getTime(), 1);

        sendFrame(":X195B4444N010100000102F003;"); // date rollover
        sendFrame(":X195B4444N0101000001020001;"); // 00:01
        assertEquals((double)midnight + 60e3,  (double)tcslave.timeKeeper.getTime(), 1);
        tk.overrideTime += 3000; // 3 real seconds later

        long t1 = tcslave.timeKeeper.getTime();
        sendFrame(":X195B4444N010100000102291C;"); // 09/28 but now it should be a noop.
        long t2 = tcslave.timeKeeper.getTime();
        assertEquals(t1, t2);
    }

    @Test
    public void testMidnightRollback() throws Exception {
        TimeKeeperTest.FakeTimeKeeper tk = new TimeKeeperTest.FakeTimeKeeper();
        tcslave.timeKeeper = tk;
        tcslave.setTimeZone(TimeZone.getTimeZone("GMT"));

        sendFrame(":X19544444N010100000102F002;"); // start
        sendFrame(":X19544444N0101000001024028;"); // rate = 10
        sendFrame(":X19544444N01010000010237A1;"); // 1953
        sendFrame(":X19544444N010100000102291B;"); // 09/27
        sendFrame(":X19544444N010100000102111E;"); // 17:30

        tk.overrideTime += 6.5 * 3600 * 1000 / 10 - 20;
        long midnight = -513129600L * 1000; // midnight

        assertEquals((double)midnight,  (double)tcslave.timeKeeper.getTime(), 250);
        // Now we get the 00:01 but without the date rollover event.
        sendFrame(":X195B4444N0101000001020001;"); // 00:01
        // and end up one day before
        assertEquals((double)midnight + 60e3 - 86400e3,  (double)tcslave.timeKeeper.getTime(),
                250);

        long t1 = tcslave.timeKeeper.getTime();
        // Now if we get the date ahead:
        sendFrame(":X195B4444N010100000102291C;"); // 09/28 but now it isn't a noop.
        long t2 = tcslave.timeKeeper.getTime();

        assertEquals((double)midnight + 60e3,  (double)t2,
                1);
        // Time jumped forward about a day.
        assertEquals((double)86400e3,  (double)t2-t1,
                60e3);
    }

    @Test
    public void testYearRollover() throws Exception {
        TimeKeeperTest.FakeTimeKeeper tk = new TimeKeeperTest.FakeTimeKeeper();
        tcslave.timeKeeper = tk;
        tcslave.setTimeZone(TimeZone.getTimeZone("GMT"));

        sendFrame(":X19544444N010100000102F002;"); // start
        sendFrame(":X19544444N0101000001024028;"); // rate = 10
        sendFrame(":X19544444N01010000010237A1;"); // 1953
        sendFrame(":X19544444N0101000001022c1F;"); // 12/31
        sendFrame(":X19544444N0101000001021700;"); // 23:00

        long expected = -504925200L * 1000;
        assertEquals((double)expected,  (double)tcslave.timeKeeper.getTime(), 1);
        tk.overrideTime += 3600 * 1000 / 10 - 20;
        expected += 3600e3;

        assertEquals((double)expected,  (double)tcslave.timeKeeper.getTime(), 250);

        sendFrame(":X195B4444N010100000102F003;"); // date rollover
        sendFrame(":X195B4444N0101000001020001;"); // 00:01

        assertEquals((double)expected + 60e3,  (double)tcslave.timeKeeper.getTime(), 1);

        tk.overrideTime += 3000;
        long t1 = tcslave.timeKeeper.getTime();
        sendFrame(":X195B4444N01010000010237A2;"); // 1954
        sendFrame(":X19544444N0101000001022101;"); // 01/01
        long t2 = tcslave.timeKeeper.getTime();
        assertEquals(t1, t2); // was a noop because we knew the right date from the rollover event.
    }

    @Test
    public void testDayRollbackwards() {
        TimeKeeperTest.FakeTimeKeeper tk = new TimeKeeperTest.FakeTimeKeeper();
        tcslave.timeKeeper = tk;
        tcslave.setTimeZone(TimeZone.getTimeZone("GMT"));

        sendFrame(":X19544444N010100000102F002;"); // start
        sendFrame(":X19544444N0101000001024FD8;"); // rate = -10
        sendFrame(":X19544444N01010000010237A1;"); // 1953
        sendFrame(":X19544444N010100000102291B;"); // 09/27
        sendFrame(":X19544444N010100000102111E;"); // 17:30

        tk.overrideTime += 17.5 * 3600L * 1000L / 10 - 20;
        long midnight = -513129600L * 1000 - 86400L*1000; // midnight
        assertEquals((double)midnight,  (double)tcslave.timeKeeper.getTime(), 250);

        sendFrame(":X195B4444N010100000102F003;"); // date rollover
        sendFrame(":X195B4444N010100000102173b;"); // 23:59
        assertEquals((double)midnight - 60e3,  (double)tcslave.timeKeeper.getTime(), 1);
    }

    @Test
    public void testSetEvents() {
        expectNoFrames();

        tcslave.setTimeZone(TimeZone.getTimeZone("GMT"));
        tcslave.requestStart();
        expectFrame(":X195B4333N010100000102F002;");
        expectNoFrames();
        tcslave.requestStop();
        expectFrame(":X195B4333N010100000102F001;");
        expectNoFrames();

        tcslave.requestSetRate(-13.25);
        expectFrame(":X195B4333N010100000102CFCB;");
        expectNoFrames();
        tcslave.requestSetRate(2.5);
        expectFrame(":X195B4333N010100000102C00A;");
        expectNoFrames();

        tcslave.requestSetTime(-513153000L * 1000L);
        expectFrame(":X195B4333N010100000102B7A1;"); // 1953
        expectFrame(":X195B4333N010100000102A91B;"); // 09/27
        expectFrame(":X195B4333N010100000102911E;"); // 17:30
        expectNoFrames();

        tcslave.requestQuery();
        expectFrame(":X195B4333N010100000102F000;");
        expectNoFrames();
    }

    @Test
    public void testListeners() throws Exception {
        MockPropertyChangeListener l = new MockPropertyChangeListener();
        // Sets up known initial state.
        tcslave.setTimeZone(TimeZone.getTimeZone("GMT"));
        sendFrame(":X19544444N010100000102F002;"); // start
        sendFrame(":X19544444N0101000001024028;"); // rate = 10
        sendFrame(":X19544444N01010000010237A1;"); // 1953
        sendFrame(":X19544444N0101000001022c1F;"); // 12/31
        sendFrame(":X19544444N0101000001021700;"); // 23:00


        tcslave.addPropertyChangeListener(l);
        assertTrue(tcslave.isRunning());
        sendFrame(":X19544444N010100000102F002;"); // start
        verifyNoMoreInteractions(l.m);  // duplicate update skipped.
        reset(l.m);

        sendFrame(":X19544444N010100000102F001;"); // stop
        verify(l.m).onChange(TimeProtocol.PROP_RUN_UPDATE, false);
        verifyNoMoreInteractions(l.m);
        reset(l.m);

        tcslave.requestStart();
        consumeMessages();
        expectFrame(":X195B4333N010100000102F002;");
        expectNoFrames();
        verify(l.m).onChange(TimeProtocol.PROP_RUN_UPDATE, true);
        verifyNoMoreInteractions(l.m);
        reset(l.m);
        tcslave.requestStop();
        expectFrame(":X195B4333N010100000102F001;");
        expectNoFrames();
        verify(l.m).onChange(TimeProtocol.PROP_RUN_UPDATE, false);
        verifyNoMoreInteractions(l.m);
        reset(l.m);

        sendFrame(":X195B4444N010100000102170A;"); // 23:10
        verify(l.m).onChange(eq(TimeProtocol.PROP_TIME_UPDATE), any());
        verifyNoMoreInteractions(l.m);
        reset(l.m);

        sendFrame(":X19544444N0101000001024004;"); // rate = 1.0
        verify(l.m).onChange(TimeProtocol.PROP_RATE_UPDATE, 1.0);
        verifyNoMoreInteractions(l.m);
        reset(l.m);
    }

    TimeBroadcastConsumer tcslave;
}