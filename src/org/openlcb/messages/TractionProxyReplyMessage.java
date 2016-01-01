package org.openlcb.messages;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import org.openlcb.AddressedMessage;
import org.openlcb.AddressedPayloadMessage;
import org.openlcb.Connection;
import org.openlcb.MessageDecoder;
import org.openlcb.MessageTypeIdentifier;
import org.openlcb.NodeID;

/**
 * Traction Proxy Reply message implementation.
 * <p/>
 * Created by bracz on 12/29/15.
 */
@Immutable
@ThreadSafe
public class TractionProxyReplyMessage extends AddressedPayloadMessage {
    public TractionProxyReplyMessage(NodeID source, NodeID dest, byte[] payload) {
        super(source, dest, payload);
    }

    @Override
    public void applyTo(MessageDecoder decoder, Connection sender) {
        decoder.handleTractionProxyReply(this, sender);
    }

    @Override
    public MessageTypeIdentifier getEMTI() {
        return MessageTypeIdentifier.TractionProxyReply;
    }
}
