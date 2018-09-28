package org.openlcb.protocols;

import org.junit.Test;
import org.openlcb.EventID;

import static org.junit.Assert.*;

/**
 * Created by bracz on 9/26/18.
 */
public class TimeProtocolTest {
    @Test
    public void decodeClock() throws Exception {
        EventID e = TimeProtocol.createClockEvent(TimeProtocol.DEFAULT_CLOCK, TimeProtocol.START_SUFFIX);
        int r = TimeProtocol.decodeClock(e, TimeProtocol.DEFAULT_CLOCK);
        assertEquals(TimeProtocol.START_SUFFIX, r);
        r = TimeProtocol.decodeClock(e, TimeProtocol.ALT_CLOCK_1);
        assertEquals(-1, r);

        e = TimeProtocol.createClockEvent(TimeProtocol.DEFAULT_CLOCK, 0x37A1);
        r = TimeProtocol.decodeClock(e, TimeProtocol.DEFAULT_CLOCK);
        assertEquals(0x37A1, r);

    }
}