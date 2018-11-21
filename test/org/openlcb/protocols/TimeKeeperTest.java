package org.openlcb.protocols;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by bracz on 9/27/18.
 */
public class TimeKeeperTest {
    public static class FakeTimeKeeper extends TimeKeeper {
        @Override
        protected long currentTimeMillis() {
            return overrideTime;
        }

        long overrideTime = System.currentTimeMillis();
    }

    @Test
    public void testStart() throws Exception {
        FakeTimeKeeper tk = new FakeTimeKeeper();
        tk.start();
        long tm = tk.getTime();
        tk.overrideTime += 3600 * 1000;
        long tm2 = tk.getTime();
        assertEquals(3600 * 1000, tm2-tm);
        tk.stop();
        tk.overrideTime += 3600 * 1000;
        assertEquals(tm2, tk.getTime());
        tk.start();
        assertEquals(tm2, tk.getTime());
        tk.overrideTime += 55 * 1000;
        long tm3 = tk.getTime();
        assertEquals(55*1000, tm3-tm2);
    }

    @Test
    public void testSetRate() throws Exception {
        FakeTimeKeeper tk = new FakeTimeKeeper();
        tk.start();
        long tm = tk.getTime();
        tk.overrideTime += 3600 * 1000;
        long tm2 = tk.getTime();
        assertEquals(3600 * 1000, tm2-tm);
        tk.setRate(2.5);
        tk.overrideTime += 3600 * 1000;
        long tm3 = tk.getTime();
        assertEquals((long) (3600 * 1000 * 2.5), tm3-tm2);
        tk.setRate(-1.0);
        assertEquals(tm3, tk.getTime());
        tk.overrideTime += 3600 * 1000;
        long tm4 = tk.getTime();
        assertEquals(-3600 * 1000, tm4-tm3);
        tk.stop();
        tk.setRate(13.0);
        tk.start();
        long tt1 = tk.getTime();
        tk.overrideTime += 1000;
        long tt2 = tk.getTime();
        assertEquals(13000, tt2-tt1);

        tk.setRate(-10.0);
        long tu1 = tk.getTime();
        tk.overrideTime += 1000;
        long tu2 = tk.getTime();
        assertEquals(-10000, tu2-tu1);
    }

    @Test
    public void testSetTime() throws Exception {
        FakeTimeKeeper tk = new FakeTimeKeeper();
        tk.start();
        long tm = tk.getTime();
        tk.overrideTime += 3600 * 1000;
        long tm2 = tk.getTime();
        assertEquals(3600 * 1000, tm2-tm);
        tk.setTime(tm2 + 5000 * 1000);
        long tm3 = tk.getTime();
        assertEquals(5000*1000, tm3-tm2);
        tk.overrideTime += 3600 * 1000;
        long tm4 = tk.getTime();
        assertEquals(3600*1000, tm4-tm3);
        tk.stop();
        tk.setTime(13000000);
        assertEquals(13000000, tk.getTime());
        tk.overrideTime += 3600*1000; // but not running
        assertEquals(13000000, tk.getTime());
        tk.start();
        assertEquals(13000000, tk.getTime());
        tk.overrideTime += 3600*1000; // now it is running;
        assertEquals(13000000 + 3600 * 1000, tk.getTime());
    }
}