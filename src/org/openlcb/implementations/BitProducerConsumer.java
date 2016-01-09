package org.openlcb.implementations;

import org.openlcb.AbstractConnection;
import org.openlcb.Connection;
import org.openlcb.EventID;
import org.openlcb.Message;
import org.openlcb.MessageDecoder;
import org.openlcb.OlcbInterface;
import org.openlcb.ProducerIdentifiedMessage;

/**
 * Maintains a bit represented by two event IDs: one off, one on.
 *
 * Created by bracz on 1/6/16.
 */
public class BitProducerConsumer extends MessageDecoder {
    private final EventID eventOn;
    private final EventID eventOff;
    private final OlcbInterface iface;

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

    private void sendIdentifiedMessages(boolean queryState) {
        Message msg;
        msg = new ProducerIdentifiedMessage(iface.getNodeId(), eventOn);

    }

    public void release() {
        iface.unRegisterMessageListener(this);
    }


}
