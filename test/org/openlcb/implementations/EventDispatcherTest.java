package org.openlcb.implementations;

import junit.framework.TestCase;

import org.openlcb.Connection;
import org.openlcb.ConsumerIdentifiedMessage;
import org.openlcb.EventID;
import org.openlcb.EventState;
import org.openlcb.IdentifyConsumersMessage;
import org.openlcb.IdentifyProducersMessage;
import org.openlcb.LearnEventMessage;
import org.openlcb.NodeID;
import org.openlcb.ProducerConsumerEventReportMessage;
import org.openlcb.ProducerIdentifiedMessage;
import org.openlcb.VerifyNodeIDNumberMessage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created by bracz on 8/10/17.
 */
public class EventDispatcherTest extends org.openlcb.InterfaceTestBase {
    private EventDispatcher dispatcher;
    private Connection c1 = mock(Connection.class);
    private Connection c2 = mock(Connection.class);
    private Connection c3 = mock(Connection.class);

    private static final EventID e1 = new EventID(new byte[]{5, 4, 3, 2, 1, 0, 7, 8});
    private static final EventID e2 = new EventID(new byte[]{5, 4, 3, 2, 1, 0, 7, 9});
    private static final EventID e3 = new EventID(new byte[]{5, 4, 3, 2, 1, 0, 7, 10});
    private static final EventID e4 = new EventID(new byte[]{5, 4, 3, 2, 1, 0, 7, 11});

    private static final NodeID s = new NodeID(new byte[]{5, 2, 1, 1, 0, 42});
    private Connection cs = mock(Connection.class);

    public void setUp() throws Exception {
        super.setUp();
        dispatcher = new EventDispatcher(iface);
    }

    @Override
    protected void tearDown() throws Exception {
        verifyHelper();
        super.tearDown();
    }

    private void verifyHelper() throws Exception {
        verifyNoMoreInteractions(c1, c2, c3);
        reset(c1, c2, c3);
    }

    private void registerAll() {
        dispatcher.registerListener(c1, e1);
        dispatcher.registerListener(c2, e2);
        dispatcher.registerListener(c3, e3);
        dispatcher.registerListener(c1, e4);
        dispatcher.registerListener(c3, e4);
    }

    public void testRegisterListener() throws Exception {
        registerAll();

        dispatcher.put(new ProducerConsumerEventReportMessage(s, e1), cs);
        verify(c1).put(new ProducerConsumerEventReportMessage(s, e1), cs);
        verifyHelper();

        dispatcher.put(new ProducerConsumerEventReportMessage(s, e2), cs);
        verify(c2).put(new ProducerConsumerEventReportMessage(s, e2), cs);
        verifyHelper();

        dispatcher.put(new ProducerConsumerEventReportMessage(s, e3), cs);
        verify(c3).put(new ProducerConsumerEventReportMessage(s, e3), cs);
        verifyHelper();

        dispatcher.put(new ProducerConsumerEventReportMessage(s, e4), cs);
        verify(c1).put(new ProducerConsumerEventReportMessage(s, e4), cs);
        verify(c3).put(new ProducerConsumerEventReportMessage(s, e4), cs);
        verifyHelper();

        dispatcher.unRegisterListener(c1);

        dispatcher.put(new ProducerConsumerEventReportMessage(s, e1), cs);
        verifyHelper();

        dispatcher.put(new ProducerConsumerEventReportMessage(s, e4), cs);
        verify(c3).put(new ProducerConsumerEventReportMessage(s, e4), cs);
        verifyHelper();
    }

    public void testOtherMessage() throws Exception {
        registerAll();
        dispatcher.put(new VerifyNodeIDNumberMessage(s), cs);
        verify(c1).put(new VerifyNodeIDNumberMessage(s), cs);
        verify(c2).put(new VerifyNodeIDNumberMessage(s), cs);
        verify(c3).put(new VerifyNodeIDNumberMessage(s), cs);
        verifyHelper();
    }

    public void testHandleLearnEvent() throws Exception {
        registerAll();
        dispatcher.put(new LearnEventMessage(s, e2), cs);
        verify(c2).put(new LearnEventMessage(s, e2), cs);
    }

    public void testHandleIdentifyConsumers() throws Exception {
        registerAll();
        dispatcher.put(new IdentifyConsumersMessage(s, e2), cs);
        verify(c2).put(new IdentifyConsumersMessage(s, e2), cs);
    }

    public void testHandleConsumerIdentified() throws Exception {
        registerAll();
        dispatcher.put(new ConsumerIdentifiedMessage(s, e2, EventState.Invalid), cs);
        verify(c2).put(new ConsumerIdentifiedMessage(s, e2, EventState.Invalid), cs);
    }

    public void testHandleIdentifyProducers() throws Exception {
        registerAll();
        dispatcher.put(new IdentifyProducersMessage(s, e2), cs);
        verify(c2).put(new IdentifyProducersMessage(s, e2), cs);
    }

    public void testHandleProducerIdentified() throws Exception {
        registerAll();
        dispatcher.put(new ProducerIdentifiedMessage(s, e2, EventState.Invalid), cs);
        verify(c2).put(new ProducerIdentifiedMessage(s, e2, EventState.Invalid), cs);
    }

}