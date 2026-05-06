package org.openlcb;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
// For annotations
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe; 

/**
 * Global message with unknown/unrecognized/unprocessed MTI
 *
 * @author  Bob Jacobsen   Copyright 2024, 2026
 */
@Immutable
@ThreadSafe
public class UnknownGlobalMtiMessage extends Message {
    public UnknownGlobalMtiMessage(NodeID source, int originalMTI, byte[] content) {
        super(source);
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
        if (o instanceof UnknownGlobalMtiMessage) {
            return equals((UnknownGlobalMtiMessage) o);
        }
        return false;
    }

    public boolean equals(UnknownGlobalMtiMessage o) {
        if ((o == null) || (this.originalMTI != o.getOriginalMTI()) ) {
            return false;
        }
        return super.equals(o);
    }
    
    @Override
    public int getMTI() {
        return MessageTypeIdentifier.UnknownGlobalMTI.mti();
    }
    /**
     * Implement message-type-specific processing when this message is received by a node.
     * <p>
     * Default is to do nothing.
     */
    @Override
    public void applyTo(MessageDecoder decoder, Connection sender) {
        decoder.handleUnknownGlobalMTI(this, sender);
    }

    @Override
    public String toString() {
        StringBuilder value = new StringBuilder(super.toString());
        value.append(" Unknown global MTI message for MTI 0x");  
        value.append(Integer.toHexString(getOriginalMTI()&0xFFF).toUpperCase());  
        return new String(value);   
    }
}
