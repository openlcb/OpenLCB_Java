package org.openlcb;

/**
 * Learn Cancel message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class LearnCancelMessage extends Message {
    
    public LearnCancelMessage(NodeID source) {
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
        decoder.handleLearnCancel(this, sender);
     }

    public String toString() {
        return getSourceNodeID().toString()
                +" LearnCancel";     
    }
     
}
