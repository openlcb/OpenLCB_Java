package org.openlcb;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
// For annotations
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe; 

/**
 * Message with unknown/unrecognized/unprocessed MTI
 * Only relevant for addressed case; unrecognized globals are ignored in proessing.
 *
 * @author  Bob Jacobsen   Copyright 2024
 */
@Immutable
@ThreadSafe
public class UnknownMtiMessage extends AddressedPayloadMessage {
    public UnknownMtiMessage(NodeID source, NodeID dest, int originalMTI, byte[] content) {
        super(source, dest, content);
        this.originalMTI = originalMTI;
    }
        
    int originalMTI;
        
    public int getOriginalMTI() {
        return originalMTI;
    }
        
    /**
     * To be equal, messages have to have the same type and content.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof UnknownMtiMessage) {
            return equals((UnknownMtiMessage) o);
        }
        return false;
    }

    public boolean equals(UnknownMtiMessage o) {
        if ((o == null) || (this.originalMTI != o.getOriginalMTI()) ) {
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
        decoder.handleUnknownMTI(this, sender);
    }

    @Override
    public String toString() {
        StringBuilder value = new StringBuilder(super.toString());
        value.append(" Unknown MTI message for MTI 0x");  
        value.append(Integer.toHexString(getOriginalMTI()&0xFFF).toUpperCase());  
        return new String(value);   
    }

    @Override
    public MessageTypeIdentifier getEMTI() {
        return MessageTypeIdentifier.UnknownMTI;
    }
}
