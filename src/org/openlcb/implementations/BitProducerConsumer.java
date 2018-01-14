package org.openlcb.implementations;

import org.openlcb.Connection;
import org.openlcb.ConsumerIdentifiedMessage;
import org.openlcb.EventID;
import org.openlcb.EventMessage;
import org.openlcb.EventState;
import org.openlcb.IdentifyConsumersMessage;
import org.openlcb.IdentifyEventsMessage;
import org.openlcb.IdentifyProducersMessage;
import org.openlcb.MessageDecoder;
import org.openlcb.OlcbInterface;
import org.openlcb.ProducerConsumerEventReportMessage;
import org.openlcb.ProducerIdentifiedMessage;

/**
 * Maintains a bit represented by two event IDs: one off, one on.
 * <p>
 * Created by bracz on 1/6/16.
 */
public class BitProducerConsumer extends MessageDecoder {
    private final EventID eventOn;
    private final EventID eventOff;
    private final OlcbInterface iface;
    private final VersionedValue<Boolean> value;
    private final VersionedValueListener<Boolean> valueListener;
    private final int flags;

    public final static EventID nullEvent = new EventID(new byte[]{0, 0, 0, 0, 0, 0, 0, 0});
    //private final static Logger log = Logger.getLogger(VersionedValue.class.getCanonicalName());

    /// Flag bit to set default value. (set: true; clear: false).
    public final static int DEFAULT_TRUE = 1;
    /// Flag bit to declare on the network as a producer.
    public final static int IS_PRODUCER = 2;
    /// Flag bit to declare on the network as a consumer.
    public final static int IS_CONSUMER = 4;
    /// Flag bit to send out a query upon startup. This will also cause listening to event
    /// identified messages whenever the current state is unknown.
    public final static int QUERY_AT_STARTUP = 8;
    /// Flag bit to always listen to event identified messages.
    public final static int LISTEN_EVENT_IDENTIFIED = 16;
    /// Flag bit to always send UNKNOWN response for event identified messages.
    public final static int SEND_UNKNOWN_EVENT_IDENTIFIED = 32;
    /// Flag bit to interpret INVALID response for event identified messages.
    public final static int LISTEN_INVALID_STATE = 64;

    public final static int DEFAULT_FLAGS = IS_PRODUCER | IS_CONSUMER | QUERY_AT_STARTUP |
            LISTEN_EVENT_IDENTIFIED | LISTEN_INVALID_STATE;

    public BitProducerConsumer(OlcbInterface iface, EventID eventOn, EventID eventOff) {
        this(iface, eventOn, eventOff, DEFAULT_FLAGS);
    }

    public BitProducerConsumer(OlcbInterface iface, EventID eventOn, EventID eventOff, boolean
            defaultValue) {
        this(iface, eventOn, eventOff, DEFAULT_FLAGS | (defaultValue ? DEFAULT_TRUE : 0));
    }

    public BitProducerConsumer(OlcbInterface iface, EventID eventOn, EventID eventOff, int flags) {
        this.iface = iface;
        this.eventOn = eventOn;
        this.eventOff = eventOff;
        this.flags = flags;
        value = new VersionedValue<>((flags & DEFAULT_TRUE) != 0);
        valueListener = new VersionedValueListener<Boolean>(value) {
            @Override
            public void update(Boolean newValue) {
                if ((flags & IS_PRODUCER) == 0) return;
                EventID id = newValue ? BitProducerConsumer.this.eventOn :
                        BitProducerConsumer.this.eventOff;
                sendMessage(new ProducerConsumerEventReportMessage(BitProducerConsumer.this.iface
                        .getNodeId(), id));
            }
        };
        iface.registerMessageListener(this);
        iface.getOutputConnection().registerStartNotification(new ConnectionListener() {
            @Override
            public void connectionActive(Connection c) {
                sendIdentifiedMessages((flags & QUERY_AT_STARTUP) != 0);
            }
        });
    }

    public VersionedValue<Boolean> getValue() {
        return value;
    }

    /**
     * Resets the producer/consumer to its default state. This will not change the actual state (also not trigger listeners), but will start reporting unknown state to the network, and enables sending a new query message to the network using @link sendQuery(), assuming the flags are set up for that.
     */
    public void resetToDefault() {
        value.setVersionToDefault();
    }

    /**
     * Sends out query messages to the bus. Useful to be called after resetToDefault().
     */
    public void sendQuery() {
        sendMessage(new IdentifyProducersMessage(iface.getNodeId(), eventOff));
        sendMessage(new IdentifyConsumersMessage(iface.getNodeId(), eventOff));
        sendMessage(new IdentifyProducersMessage(iface.getNodeId(), eventOn));
        sendMessage(new IdentifyConsumersMessage(iface.getNodeId(), eventOn));
    }

    /**
     * Sends out an event message
     * @param <T>    the message type to send.
     * @param msg    event message to send
     */
    <T extends EventMessage> void sendMessage(T msg) {
        if (msg.getEventID().equals(nullEvent)) return;
        iface.getOutputConnection().put(msg, BitProducerConsumer.this);
    }

    /**
     * @return true if we have not received any network state yet, thus the value is still at the
     * default value passed in.
     */
    public boolean isValueAtDefault() {
        return (value.isVersionAtDefault());
    }

    private EventState getOnEventState() {
        if (isValueAtDefault() || ((flags & SEND_UNKNOWN_EVENT_IDENTIFIED) > 0)) {
            return EventState.Unknown;
        }
        if (value.getLatestData()) return EventState.Valid;
        return EventState.Invalid;
    }

    private EventState getOffEventState() {
        return getOnEventState().inverted();
    }

    private EventState getEventState(EventID event) {
        if (event.equals(eventOn)) return getOnEventState();
        if (event.equals(eventOff)) return getOffEventState();
        return null;
    }

    private void sendIdentifiedMessages(boolean queryState) {
        if ((flags & IS_PRODUCER) != 0) {
            sendMessage(new ProducerIdentifiedMessage(iface.getNodeId(), eventOn, getOnEventState
                    ()));

            sendMessage(new ProducerIdentifiedMessage(iface.getNodeId(), eventOff,
                    getOffEventState()));
        }
        if ((flags & IS_CONSUMER) != 0) {
            sendMessage(new ConsumerIdentifiedMessage(iface.getNodeId(), eventOn, getOnEventState
                    ()));

            sendMessage(new ConsumerIdentifiedMessage(iface.getNodeId(), eventOff,
                    getOffEventState()));
        }
        if (queryState) {
            sendQuery();
        }
    }

    public void release() {
        iface.unRegisterMessageListener(this);
    }

    @Override
    public void handleIdentifyConsumers(IdentifyConsumersMessage msg, Connection sender) {
        if (sender == this) return;
        EventState st = getEventState(msg.getEventID());
        if (st != null && ((flags & IS_CONSUMER) != 0)) {
            sendMessage(new ConsumerIdentifiedMessage(iface.getNodeId(), msg.getEventID(), st));
        }
    }

    @Override
    public void handleIdentifyProducers(IdentifyProducersMessage msg, Connection sender) {
        if (sender == this) return;
        EventState st = getEventState(msg.getEventID());
        if (st != null && ((flags & IS_PRODUCER) != 0)) {
            sendMessage(new ProducerIdentifiedMessage(iface.getNodeId(), msg.getEventID(), st));
        }
    }

    /**
     * @return true if we are interested in a P/C identified message reporting layout state right
     * now.
     */
    private boolean shouldListenToIdentifiedMessages() {
        if ((flags & LISTEN_EVENT_IDENTIFIED) != 0) return true;
        if (((flags & QUERY_AT_STARTUP) != 0) && isValueAtDefault()) return true;
        return false;
    }

    @Override
    public void handleProducerIdentified(ProducerIdentifiedMessage msg, Connection sender) {
        if (!shouldListenToIdentifiedMessages()) return;
        boolean isOn;
        if (msg.getEventID().equals(eventOn)) {
            isOn = true;
        } else if (msg.getEventID().equals(eventOff)) {
            isOn = false;
        } else {
            return;
        }
        if (msg.getEventState().equals(EventState.Valid)) {
            setValueFromNetwork(isOn);
        } else if (msg.getEventState().equals(EventState.Invalid) && ((flags &
                LISTEN_INVALID_STATE) != 0)) {
            setValueFromNetwork(!isOn);
        }
    }

    @Override
    public void handleConsumerIdentified(ConsumerIdentifiedMessage msg, Connection sender) {
        if (!shouldListenToIdentifiedMessages()) return;
        boolean isOn;
        if (msg.getEventID().equals(eventOn)) {
            isOn = true;
        } else if (msg.getEventID().equals(eventOff)) {
            isOn = false;
        } else {
            return;
        }
        if (msg.getEventState().equals(EventState.Valid)) {
            setValueFromNetwork(isOn);
        } else if (msg.getEventState().equals(EventState.Invalid) && ((flags &
                LISTEN_INVALID_STATE) != 0)) {
            setValueFromNetwork(!isOn);
        }
    }

    @Override
    public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg,
                                                  Connection sender) {
        if ((flags & IS_CONSUMER) == 0) return;
        boolean isOn;
        if (msg.getEventID().equals(eventOn)) {
            isOn = true;
        } else if (msg.getEventID().equals(eventOff)) {
            isOn = false;
        } else {
            return;
        }
        setValueFromNetwork(isOn);
    }

    @Override
    public void handleIdentifyEvents(IdentifyEventsMessage msg, Connection sender) {
        if (msg.getDestNodeID().equals(iface.getNodeId())) {
            sendIdentifiedMessages(false);
        }
    }

    private void setValueFromNetwork(boolean isOn) {
        valueListener.setFromOwner(isOn);
    }
}
