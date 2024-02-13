package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Verify Node ID Number message implementation.
 * 
 * Two forms:  Global, with and without
 *             target NodeID in data
 *
 * @author  Bob Jacobsen   Copyright 2009, 2010
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class VerifyNodeIDNumberGlobalMessage extends Message {
    
    public VerifyNodeIDNumberGlobalMessage(NodeID source) {
        super(source);
        this.content = null;
    }

    public VerifyNodeIDNumberGlobalMessage(NodeID source, NodeID content) {
        this(source);
        this.content = content;
    }
    
    NodeID content;
    
    public NodeID getContent() { return content; }
    
     /**
      * To be equal, messages have to have the
      * same type and content
      */
     public boolean equals(Object o) {
        if (! (o instanceof VerifyNodeIDNumberGlobalMessage))
            return false;
        VerifyNodeIDNumberGlobalMessage msg = (VerifyNodeIDNumberGlobalMessage) o;
        if (this.content != null) {
            if (msg.content == null || (! this.content.equals(msg.content)))
                return false;
        } else {
            if (msg.content != null) return false;
        }
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
        decoder.handleVerifyNodeIDNumberGlobal(this, sender);
     }

    public String toString() {
        return super.toString()
                +" Verify Node ID Number Global: "
                + ((content != null) ? (content+" only") : ("all nodes"));    
    }

    public int getMTI() { return MTI_VERIFY_NID; }
}
