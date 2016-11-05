package org.openlcb;

// For annotations
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

/**
 * Datagram Acknowledged message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class DatagramAcknowledgedMessage extends AddressedPayloadMessage {
    
    public DatagramAcknowledgedMessage(NodeID source, NodeID dest) {
        super(source, dest, null);
        flags = 0;
    }

    public DatagramAcknowledgedMessage(NodeID source, NodeID dest, int flags) {
        super(source, dest, flags == 0 ? null : new byte[]{(byte)flags});
        this.flags = flags;
    }

    int flags;

    public int getFlags() { return flags; }

    /**
     * Implement message-type-specific
     * processing when this message
     * is received by a node.
     *<p>
     * Default is to do nothing.
     */
     @Override
     public void applyTo(MessageDecoder decoder, Connection sender) {
        decoder.handleDatagramAcknowledged(this, sender);
     }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (! (o instanceof DatagramAcknowledgedMessage))
            return false;
        if (!super.equals(o)) return false;
        return (flags == ((DatagramAcknowledgedMessage)o).flags);
    }

    @Override
    public MessageTypeIdentifier getEMTI() {
        return MessageTypeIdentifier.DatagramReceivedOK;
    }
}
