package org.openlcb.messages;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import org.openlcb.AddressedPayloadMessage;
import org.openlcb.Connection;
import org.openlcb.MessageDecoder;
import org.openlcb.MessageTypeIdentifier;
import org.openlcb.NodeID;

/**
 * Traction Control Request message implementation.
 * <p/>
 * Created by bracz on 12/29/15.
 */
@Immutable
@ThreadSafe
public class TractionControlRequestMessage extends AddressedPayloadMessage {
    public TractionControlRequestMessage(NodeID source, NodeID dest, byte[] payload) {
        super(source, dest, payload);
        this.payload = payload.clone();
    }

    @Override
    public void applyTo(MessageDecoder decoder, Connection sender) {
        decoder.handleTractionControlRequest(this, sender);
    }

    @Override
    public MessageTypeIdentifier getEMTI() {
        return MessageTypeIdentifier.TractionControlRequest;
    }
}
