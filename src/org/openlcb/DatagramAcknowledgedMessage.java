package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

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

    public int getMTI() { return MTI_DATAGRAM_RCV_OK; }
}
