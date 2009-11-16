package org.openlcb;

/**
 * Learn Pending message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class LearnPendingMessage extends Message {
    
    public LearnPendingMessage(NodeID source) {
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
        decoder.handleLearnPending(this, sender);
     }

    public String toString() {
        return getSourceNodeID().toString()
                +" LearnPending";     
    }
     
}
