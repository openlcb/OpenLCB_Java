package org.openlcb.implementations;

import org.openlcb.Connection;
import org.openlcb.ConsumerIdentifiedMessage;
import org.openlcb.EventID;
import org.openlcb.EventState;
import org.openlcb.IdentifyConsumersMessage;
import org.openlcb.IdentifyProducersMessage;
import org.openlcb.Message;
import org.openlcb.MessageDecoder;
import org.openlcb.OlcbInterface;
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

    private EventState getOnEventState() {
        if (value == null) return EventState.Unknown;
        if (value.getLatestData()) return EventState.Valid;
        return EventState.Invalid;
    }

    private EventState getOffEventState() {
        return getOnEventState().inverted();
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


}
