package org.openlcb;

// For annotations
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

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
        
    @SuppressWarnings("JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
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

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (! (o instanceof DatagramRejectedMessage))
            return false;
        if (this.code != ((DatagramRejectedMessage)o).getCode())
            return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
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

    @Override
    public String toString() {
        return super.toString()+" Datagram Rejected";
    }

}
