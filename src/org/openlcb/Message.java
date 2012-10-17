package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Base for all OpenLCB message types
 *<p>
 * Messages (and therefore all subtypes) are immutable once created.
 *<p>
 * A Message works with a {@link MessageDecoder} object in a double dispatch
 * pattern to do message specific-processing in e.g. a node implementation.
 *
 * @author  Bob Jacobsen   Copyright 2009, 2010
 * @version $Revision$
 * @see MessageDecoder
 */
@Immutable
@ThreadSafe
abstract public class Message implements OpenLcb {

    public Message(NodeID source) {
        sourceNodeID = source;
    }
    
    // cannot create without sourceID
    protected Message() {}
    
    NodeID sourceNodeID;
    
    public NodeID getSourceNodeID() { return sourceNodeID; }
    
    /**
     * Implement message-type-specific
     * processing when this message
     * is received by a node.
     *<p>
     * Default is to do nothing.
     */
     public void applyTo(MessageDecoder decoder, Connection sender) {
     }
     
     /**
      * To be equal, messages have to have the
      * same type and content
      */
     public boolean equals(Object o) {
        if (! (o instanceof Message))
            return false;
        Message msg = (Message) o;
        if (this.getMTI() != msg.getMTI())
            return false;
        if (! this.sourceNodeID.equals(msg.sourceNodeID))
            return false;
        return true;
     }
     
     
     @Override
     public int hashCode() { return getSourceNodeID().hashCode(); }
    
     @Override
     public String toString() {
        return getSourceNodeID().toString()+"                    ";  
     }

     abstract public int getMTI();
}
