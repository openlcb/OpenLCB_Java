package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Optional Interaction Rejected message
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision: 529 $
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
    
    public int getMti() { return mti; }
    
    public int getCode() { return code; }

    private static byte[] toPayload(int mti, int code) {
        byte[] b = new byte[4];
        Utilities.HostToNetworkUint16(b, 0, mti);
        Utilities.HostToNetworkUint16(b, 2, code);
        return b;
    }
     /**
      * To be equal, messages have to have the
      * same type and content
      */
     public boolean equals(Object o) {
        if (o == null) return false;
        if (! (o instanceof OptionalIntRejectedMessage))
            return false;
        if (this.mti != ((OptionalIntRejectedMessage)o).getMti() ) return false;
        if (this.code != ((OptionalIntRejectedMessage)o).getCode() ) return false;
        return super.equals(o);
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
        decoder.handleOptionalIntRejected(this, sender);
     }

    @Override
    public String toString() {
        StringBuilder value = new StringBuilder(super.toString());
        value.append(" Optional Interaction Rejected for MTI 0x");  
        value.append(Integer.toHexString((int)(getMti()&0xFFF)).toUpperCase());  
        value.append(" code 0x");  
        value.append(Integer.toHexString((int)(getCode()&0xFFFF)).toUpperCase());  
        return new String(value);   
    }

    @Override
    public MessageTypeIdentifier getEMTI() {
        return MessageTypeIdentifier.OptionalInteractionRejected;
    }
}
