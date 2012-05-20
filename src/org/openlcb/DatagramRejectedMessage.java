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
    
    public int getCode() { return code; }
    
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
    
    static final int DATAGRAM_REJECTED                           = 0x000;
    static final int DATAGRAM_REJECTED_PERMANENT_ERROR           = 0x100;
    static final int DATAGRAM_REJECTED_INFORMATION_LOGGED        = 0x101;
    static final int DATAGRAM_REJECTED_SOURCE_NOT_PERMITTED      = 0x102;
    static final int DATAGRAM_REJECTED_DATAGRAMS_NOT_ACCEPTED    = 0x104;
    static final int DATAGRAM_REJECTED_BUFFER_FULL               = 0x200;
    static final int DATAGRAM_REJECTED_OUT_OF_ORDER              = 0x600;
    
    static final int DATAGRAM_REJECTED_NO_RESEND_MASK            = 0x100;
    static final int DATAGRAM_REJECTED_RESEND_MASK               = 0x200;
    static final int DATAGRAM_REJECTED_TRANSPORT_ERROR_MASK      = 0x400;
    
    
    public boolean canResend() { 
        return (code & DATAGRAM_REJECTED_RESEND_MASK) == DATAGRAM_REJECTED_RESEND_MASK;
    }
}
