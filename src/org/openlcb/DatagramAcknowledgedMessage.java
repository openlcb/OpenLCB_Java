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
public class DatagramAcknowledgedMessage extends AddressedMessage {
    
    public DatagramAcknowledgedMessage(NodeID source, NodeID dest) {
        super(source, dest);
    }
        
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
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public int getMTI() { return MTI_DATAGRAM_RCV_OK; }

    @Override
    public String toString() {
        return super.toString()+" Datagram Acknowledged";
    }
}
