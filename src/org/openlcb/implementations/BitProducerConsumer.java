package org.openlcb.implementations;

import org.openlcb.Connection;
import org.openlcb.ConsumerIdentifiedMessage;
import org.openlcb.EventID;
import org.openlcb.EventState;
import org.openlcb.IdentifyConsumersMessage;
import org.openlcb.IdentifyEventsMessage;
import org.openlcb.IdentifyProducersMessage;
import org.openlcb.Message;
import org.openlcb.MessageDecoder;
import org.openlcb.OlcbInterface;
import org.openlcb.ProducerConsumerEventReportMessage;
import org.openlcb.ProducerIdentifiedMessage;

/**
 * Maintains a bit represented by two event IDs: one off, one on.
 * <p/>
 * Created by bracz on 1/6/16.
 */
public class BitProducerConsumer extends MessageDecoder {
    private final EventID eventOn;
    private final EventID eventOff;
    private final OlcbInterface iface;
    private VersionedValue<Boolean> value = null;
    private VersionedValueListener<Boolean> valueListener = null;

    public BitProducerConsumer(OlcbInterface iface, EventID eventOn, EventID eventOff) {
        this.iface = iface;
        this.eventOn = eventOn;
        this.eventOff = eventOff;
        iface.registerMessageListener(this);
        iface.getOutputConnection().registerStartNotification(new ConnectionListener() {
            @Override
            public void connectionActive(Connection c) {
                sendIdentifiedMessages(true);
            }
        });
    }

    public synchronized VersionedValue<Boolean> getValue(boolean defaultValue) {
        if(value == null) {
            setValueFromNetwork(defaultValue);
        }
        return value;
    }

    public VersionedValue<Boolean> getValue() {
        return value;
    }

    private EventState getOnEventState() {
        if (value == null) return EventState.Unknown;
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
        Message msg;
        msg = new ProducerIdentifiedMessage(iface.getNodeId(), eventOn, getOnEventState());
        iface.getOutputConnection().put(msg, this);
        msg = new ProducerIdentifiedMessage(iface.getNodeId(), eventOff, getOffEventState());
        iface.getOutputConnection().put(msg, this);
        msg = new ConsumerIdentifiedMessage(iface.getNodeId(), eventOn, getOnEventState());
        iface.getOutputConnection().put(msg, this);
        msg = new ConsumerIdentifiedMessage(iface.getNodeId(), eventOff, getOffEventState());
        iface.getOutputConnection().put(msg, this);
        if (queryState) {
            msg = new IdentifyProducersMessage(iface.getNodeId(), eventOn);
            iface.getOutputConnection().put(msg, this);
            msg = new IdentifyConsumersMessage(iface.getNodeId(), eventOn);
            iface.getOutputConnection().put(msg, this);
        }
    }

    public void release() {
        iface.unRegisterMessageListener(this);
    }

    @Override
    public void handleIdentifyConsumers(IdentifyConsumersMessage msg, Connection sender) {
        EventState st = getEventState(msg.getEventID());
        if (st != null) {
            Message m = new ConsumerIdentifiedMessage(iface.getNodeId(), msg.getEventID(), st);
            iface.getOutputConnection().put(m, this);
        }
    }

    @Override
    public void handleIdentifyProducers(IdentifyProducersMessage msg, Connection sender) {
        EventState st = getEventState(msg.getEventID());
        if (st != null) {
            Message m = new ProducerIdentifiedMessage(iface.getNodeId(), msg.getEventID(), st);
            iface.getOutputConnection().put(m, this);
        }
    }

    @Override
    public void handleProducerIdentified(ProducerIdentifiedMessage msg, Connection sender) {
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
        } else if (msg.getEventState().equals(EventState.Invalid)) {
            setValueFromNetwork(!isOn);
        }
    }

    @Override
    public void handleConsumerIdentified(ConsumerIdentifiedMessage msg, Connection sender) {
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
        } else if (msg.getEventState().equals(EventState.Invalid)) {
            setValueFromNetwork(!isOn);
        }
    }

    @Override
    public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg,
                                                  Connection sender) {
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
        synchronized (this) {
            if (value == null) {
                value = new VersionedValue<>(isOn);
                valueListener = new VersionedValueListener<Boolean>(value) {
                    @Override
                    public void update(Boolean newValue) {
                        Message msg = new ProducerConsumerEventReportMessage(iface.getNodeId(),
                                newValue ? eventOn : eventOff);
                        iface.getOutputConnection().put(msg, BitProducerConsumer.this);
                    }
                };
                return;
            }
        }
        valueListener.setFromOwner(isOn);
    }
}
