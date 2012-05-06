package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Datagram Rejected message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class DatagramRejectedMessage extends AddressedMessage {
    
    public DatagramRejectedMessage(NodeID source, NodeID dest, int code) {
        super(source, dest);
        this.code = code;
    }
        
    int code;
    
    /**
     * Implement message-type-specific
     * processing when this message
     * is received by a node.
     *<p>
     * Default is to do nothing.
     */
     @Override
     public void applyTo(MessageDecoder decoder, Connection sender) {
        decoder.handleDatagramRejected(this, sender);
     }

    public int getMTI() { return MTI_DATAGRAM_REJECTED; }
}
