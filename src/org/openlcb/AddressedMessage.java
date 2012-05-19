package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Base for addressed OpenLCB message types
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
abstract public class AddressedMessage extends Message {

    public AddressedMessage(NodeID source, NodeID dest) {
        super(source);
        destNodeID = dest;
    }
    
    // cannot create without sourceID, destID
    protected AddressedMessage() {}
    
    NodeID destNodeID;
    
    public NodeID getDestNodeID() { return destNodeID; }
         
     /**
      * To be equal, messages have to have the
      * same type and content
      */
     public boolean equals(Object o) {
        if (o == null) return false;
        if (! (o instanceof AddressedMessage))
            return false;
        AddressedMessage msg = (AddressedMessage) o;
        if (!this.getDestNodeID().equals(msg.getDestNodeID()))
            return false;
        else return super.equals(o);
     }

     @Override
     public int hashCode() { return super.hashCode()+getDestNodeID().hashCode(); }

}
