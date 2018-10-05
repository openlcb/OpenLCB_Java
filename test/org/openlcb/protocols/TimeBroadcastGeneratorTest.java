package org.openlcb.protocols;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlcb.FakeConnection;
import org.openlcb.InterfaceTestBase;
import org.openlcb.MockPropertyChangeListener;

import java.util.Date;
import java.util.TimeZone;
import java.util.TimerTask;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created by bracz on 10/1/18.
 */
public class TimeBroadcastGeneratorTest extends InterfaceTestBase {
    @Before
    public void privateSetUp() throws Exception {
        tgmaster = new TimeBroadcastGenerator(iface, TimeProtocol.ALT_CLOCK_1);
        tgmaster.setTimeZone(TimeZone.getTimeZone("GMT"));
        expectFrame(":X19524333N010100000102FFFF;");
        expectFrame(":X194A4333N0101000001028000;");
        expectNoFrames();
    }

    @After
    public void privateTearDown() throws Exception {
        tgmaster.dispose();
    }

    @Test
    public void getAndSetRate() throws Exception {
        MockPropertyChangeListener l = new MockPropertyChangeListener();
        tgmaster.addPropertyChangeListener(l);
        assertEquals(tgmaster.getRate(), 1.0, 0.001);
        tgmaster.requestSetRate(3.75);
        verify(l.m).onChange(TimeProtocol.PROP_RATE_UPDATE, 3.75);
        verifyNoMoreInteractions(l.m);
        expectFrame(":X195B4333N010100000102400F;"); // rate = 3.75
        expectNoFrames();
        assertEquals(tgmaster.getRate(), 3.75, 0.001);
        expectStateReportPending();

        tgmaster.requestSetRate(-10);
        verify(l.m).onChange(TimeProtocol.PROP_RATE_UPDATE, -10.0);
        verifyNoMoreInteractions(l.m);
        expectFrame(":X195B4333N0101000001024FD8;"); // rate = -10
        assertEquals(tgmaster.getRate(), -10, 0.001);
        expectStateReportPending();
    }

    private void waitForTimerThread() {
        class Holder {
            boolean b = false;
        };
        final Holder h = new Holder();
        iface.getTimer().schedule(new TimerTask() {
            @Override
            public void run() {
                h.b = true;
            }
        }, new Date(System.currentTimeMillis() + 100));
        while (!h.b) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // do nothing.
            }
        }
        iface.flushSendQueue();
    }

    /// Helper function
    private void expectStateReportPending() {
        assertNotNull(tgmaster.delayedSyncTask);
    }

    @Test
    public void stateReport() throws Exception {
        tgmaster.timeKeeper.setTime(-513153000L * 1000L);
        tgmaster.RESYNC_DELAY_MSEC = 50;
        tgmaster.requestSetRate(3.75);
        expectFrame(":X195B4333N010100000102400F;"); // rate = 3.75
        expectNoFrames();
        Thread.sleep(100);
        waitForTimerThread();
        expectFrame(":X19544333N010100000102F002;"); // is running
        expectFrame(":X19544333N010100000102400F;"); // rate = 3.75
        expectFrame(":X19544333N01010000010237A1;"); // 1953
        expectFrame(":X19544333N010100000102291B;"); // 09/27
        expectFrame(":X19544333N010100000102111E;"); // 17:30
        expectNoFrames();
    }

    @Test
    public void setAndIsRunning() throws Exception {
        MockPropertyChangeListener l = new MockPropertyChangeListener();
        tgmaster.addPropertyChangeListener(l);
        assertTrue(tgmaster.isRunning());
        tgmaster.requestStart();
        // No callback if not changing state.
        verifyNoMoreInteractions(l.m);
        expectNoFrames();

        tgmaster.requestStop();
        iface.flushSendQueue();
        expectStateReportPending();
        verify(l.m).onChange(TimeProtocol.PROP_RUN_UPDATE, false);
        verifyNoMoreInteractions(l.m);
        expectFrame(":X195B4333N010100000102F001;");
        expectNoFrames();
        assertFalse(tgmaster.isRunning());

        tgmaster.requestStart();
        expectStateReportPending();
        iface.flushSendQueue();
        verify(l.m).onChange(TimeProtocol.PROP_RUN_UPDATE, true);
        verifyNoMoreInteractions(l.m);
        expectFrame(":X195B4333N010100000102F002;");
        assertTrue(tgmaster.isRunning());
    }

    @Test
    public void getTimeInMsec() throws Exception {
        tgmaster.timeKeeper.setTime(-513153000L * 1000L);
        assertEquals(-513153000L * 1000L, tgmaster.getTimeInMsec());
    }

    @Test
    public void requestSetTime() throws Exception {
        MockPropertyChangeListener l = new MockPropertyChangeListener();
        tgmaster.addPropertyChangeListener(l);
        tgmaster.requestSetTime(-513153000L * 1000L);
        verify(l.m).onChange(TimeProtocol.PROP_TIME_UPDATE, -513153000L * 1000L);
        expectFrame(":X195B4333N01010000010237A1;"); // 1953
        expectFrame(":X195B4333N010100000102291B;"); // 09/27
        expectFrame(":X195B4333N010100000102111E;"); // 17:30
        assertEquals(-513153000L * 1000.0, tgmaster.getTimeInMsec() * 1.0, 1000);
        expectStateReportPending();
    }

    @Test
    public void requestQuery() throws Exception {
        tgmaster.timeKeeper.stop();
        tgmaster.timeKeeper.setTime(-513153000L * 1000L);
        MockPropertyChangeListener l = new MockPropertyChangeListener();
        tgmaster.addPropertyChangeListener(l);
        tgmaster.requestQuery();
        verify(l.m).onChange(TimeProtocol.PROP_RUN_UPDATE, false);

        verify(l.m).onChange(TimeProtocol.PROP_RATE_UPDATE, 1.0);
        verify(l.m).onChange(TimeProtocol.PROP_TIME_UPDATE, -513153000L * 1000L);
        verifyNoMoreInteractions(l.m);
        expectNoFrames();
    }

    @Test
    public void remoteOperationRunAndRate() throws Exception {
        MockPropertyChangeListener l = new MockPropertyChangeListener();
        tgmaster.addPropertyChangeListener(l);

        sendFrame(":X195B4444N010100000102F001;");
        verify(l.m).onChange(TimeProtocol.PROP_RUN_UPDATE, false);
        verifyNoMoreInteractions(l.m);
        expectNoFrames();
        assertFalse(tgmaster.isRunning());
        expectStateReportPending();

        sendFrame(":X195B4444N010100000102F002;");
        verify(l.m).onChange(TimeProtocol.PROP_RUN_UPDATE, true);
        verifyNoMoreInteractions(l.m);
        expectNoFrames();
        assertTrue(tgmaster.isRunning());
        expectStateReportPending();

        sendFrame(":X195B4444N010100000102C028;"); // rate := 10
        verify(l.m).onChange(TimeProtocol.PROP_RATE_UPDATE, 10.0);
        verifyNoMoreInteractions(l.m);
        expectFrame(":X195B4333N0101000001024028;");
        expectStateReportPending();
        assertEquals(tgmaster.getRate(), 10.0, 0.001);

        sendFrame(":X195B4444N010100000102CFD8;"); // rate := -10
        verify(l.m).onChange(TimeProtocol.PROP_RATE_UPDATE, -10.0);
        verifyNoMoreInteractions(l.m);
        expectFrame(":X195B4333N0101000001024FD8;");
        expectStateReportPending();

        assertEquals(tgmaster.getRate(), -10.0, 0.001);
    }

    @Test
    public void testRemoteOperationTime() {
        tgmaster.timeKeeper.stop();
        MockPropertyChangeListener l = new MockPropertyChangeListener();
        tgmaster.addPropertyChangeListener(l);
        sendFrame(":X195B4444N010100000102B7A1;"); // 1953
        verify(l.m).onChange(eq(TimeProtocol.PROP_TIME_UPDATE), any());
        verifyNoMoreInteractions(l.m);
        reset(l.m);
        expectFrame(":X195B4333N01010000010237A1;"); // 1953
        sendFrame(":X195B4444N010100000102A91B;"); // 09/27
        verify(l.m).onChange(eq(TimeProtocol.PROP_TIME_UPDATE), any());
        verifyNoMoreInteractions(l.m);
        reset(l.m);
        expectFrame(":X195B4333N010100000102291B;"); // 09/27
        sendFrame(":X195B4444N010100000102911E;"); // 17:30
        verify(l.m).onChange(TimeProtocol.PROP_TIME_UPDATE, -513153000L * 1000L);
        verifyNoMoreInteractions(l.m);
        reset(l.m);
        expectFrame(":X195B4333N010100000102111E;"); // 17:30
        expectNoFrames();
        assertEquals(-513153000L * 1000.0, tgmaster.getTimeInMsec() * 1.0, 1000);
        expectStateReportPending();
    }

    @Test
    public void testMidnightRollover() throws Exception {
        tgmaster.timeKeeper.setRate(500.0);
        long midnight = -513129600L * 1000;
        tgmaster.requestSetTime(midnight - 100*100);
        expectFrame(":X195B4333N01010000010237A1;"); // 1953
        expectFrame(":X195B4333N010100000102291B;"); // 09/27
        expectFrame(":X195B4333N010100000102173B;"); // 23:59

        Thread.sleep(50);
        expectFrame(":X195B4333N010100000102F003;"); // date rollover
        expectNoFrames();
        tgmaster.requestSetRate(-512.0);
        expectFrame(":X195B4333N0101000001024800;"); // rate = -512
        expectNoFrames();
        Thread.sleep(150);
        expectFrame(":X195B4333N010100000102F003;"); // date rollover
        expectNoFrames();
    }

    @Test
    public void testMidnightRolloverBackwards() throws Exception {
        tgmaster.timeKeeper.setRate(-500.0);
        long midnight = -513129600L * 1000;
        tgmaster.requestSetTime(midnight + 100*100);
        expectFrame(":X195B4333N01010000010237A1;"); // 1953
        expectFrame(":X195B4333N010100000102291C;"); // 09/28
        expectFrame(":X195B4333N0101000001020000;"); // 00:00

        Thread.sleep(50);
        expectFrame(":X195B4333N010100000102F003;"); // date rollover
        expectNoFrames();
        tgmaster.requestSetRate(+511.75);
        expectFrame(":X195B4333N01010000010247FF;"); // rate = +511.75
        expectNoFrames();
        Thread.sleep(150);
        expectFrame(":X195B4333N010100000102F003;"); // date rollover
        expectNoFrames();
    }

    @Test
    public void testMidnightRolloverAfterStart() throws Exception {
        //printAllSentMessages();
        tgmaster.timeKeeper.setRate(500.0);
        long midnight = -513129600L * 1000;
        tgmaster.requestSetTime(midnight - 100*100);
        expectFrame(":X195B4333N01010000010237A1;"); // 1953
        expectFrame(":X195B4333N010100000102291B;"); // 09/27
        expectFrame(":X195B4333N010100000102173B;"); // 23:59
        tgmaster.requestStop();
        expectFrame(":X195B4333N010100000102F001;"); // stop

        Thread.sleep(50);
        expectNoFrames();
        tgmaster.requestStart();
        expectFrame(":X195B4333N010100000102F002;"); // start
        expectNoFrames();
        Thread.sleep(50);
        expectFrame(":X195B4333N010100000102F003;"); // start
    }

    TimeBroadcastGenerator tgmaster;

}