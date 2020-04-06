package org.openlcb;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
// For annotations
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe; 

/**
 * Optional Interaction Rejected message
 *
 * @author  Bob Jacobsen   Copyright 2012
 */
@Immutable
@ThreadSafe
public class OptionalIntRejectedMessage extends AddressedPayloadMessage {
    public OptionalIntRejectedMessage(NodeID source, NodeID dest, int mti, int code) {
        super(source, dest, toPayload(mti, code));
        this.mti = mti;
        this.code = code;
    }
        
    int mti;
    int code;
    
    @SuppressFBWarnings
    @Deprecated
    public int getMti() {
        return mti;
    }
    
    public int getRejectMTI() {
        return mti;
    }
    
    public int getCode() {
        return code;
    }

    private static byte[] toPayload(int mti, int code) {
        byte[] b = new byte[4];
        Utilities.HostToNetworkUint16(b, 0, mti);
        Utilities.HostToNetworkUint16(b, 2, code);
        return b;
    }
    
    /**
     * To be equal, messages have to have the same type and content.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof OptionalIntRejectedMessage) {
            return equals((OptionalIntRejectedMessage) o);
        }
        return false;
    }

    public boolean equals(OptionalIntRejectedMessage o) {
        if ((o == null) || (this.mti != o.getRejectMTI()) || (this.code != o.getCode())) {
            return false;
        }
        return super.equals(o);
    }
    
    /**
     * Implement message-type-specific processing when this message is received by a node.
     * <p>
     * Default is to do nothing.
     */
    @Override
    public void applyTo(MessageDecoder decoder, Connection sender) {
        decoder.handleOptionalIntRejected(this, sender);
    }

    @Override
    public String toString() {
        StringBuilder value = new StringBuilder(super.toString());
        value.append(" Optional Interaction Rejected for MTI 0x");  
        value.append(Integer.toHexString(getRejectMTI()&0xFFF).toUpperCase());  
        value.append(" code 0x");  
        value.append(Integer.toHexString(getCode()&0xFFFF).toUpperCase());  
        return new String(value);   
    }

    @Override
    public MessageTypeIdentifier getEMTI() {
        return MessageTypeIdentifier.OptionalInteractionRejected;
    }
}
