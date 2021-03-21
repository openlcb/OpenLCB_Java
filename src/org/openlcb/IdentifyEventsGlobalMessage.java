package org.openlcb;

// For annotations

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

/**
 * Identify Events message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class IdentifyEventsGlobalMessage extends Message {

    public IdentifyEventsGlobalMessage(NodeID source) {
        super(source);
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
        decoder.handleIdentifyEventsGlobal(this, sender);
     }
    public String toString() {
        return super.toString()
                +" Identify Events ";   
    }

    public int getMTI() { return MTI_IDENTIFY_EVENTS; }
}
