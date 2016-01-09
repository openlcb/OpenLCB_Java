package org.openlcb.implementations;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openlcb.AbstractConnection;
import org.openlcb.Connection;
import org.openlcb.EventID;
import org.openlcb.FakeOlcbInterface;
import org.openlcb.Message;

import java.util.List;

/**
 * Created by bracz on 1/8/16.
 */
public class BitProducerConsumerTest extends TestCase {

    public static final EventID onEvent = new EventID(new byte[]{5, 4, 3, 2, 1, 0, 7, 8});
    public static final EventID offEvent = new EventID(new byte[]{5, 4, 3, 2, 1, 0, 7, 9});
    FakeOlcbInterface iface = new FakeOlcbInterface();

    public BitProducerConsumerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {BitProducerConsumerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(BitProducerConsumerTest.class);
        return suite;
    }

    public void testHandleIdentifyConsumers() throws Exception {

    }

    public void testHandleIdentifyProducers() throws Exception {

    }

    public void testHandleProducerIdentified() throws Exception {

    }

    public void testHandleConsumerIdentified() throws Exception {

    }

    public void testHandleProducerConsumerEventReport() throws Exception {

    }

    public void testHandleIdentifyEvents() throws Exception {

    }

    BitProducerConsumer pc;

    private List<Message> sentMessages() {
        return iface.fakeOutputConnection.history;
    }

    @Override
    public void setUp() {
        iface.fakeOutputConnection.history.clear();
        pc = new BitProducerConsumer(iface, onEvent, offEvent);
        assertEquals("init-size", 8, sentMessages().size());

    }
}