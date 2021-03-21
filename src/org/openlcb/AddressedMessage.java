package org.openlcb;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

/**
 * Base for addressed OpenLCB message types
 *<p>
 * Messages (and therefore all subtypes) are immutable once created.
 *<p>
 * A Message works with a {@link MessageDecoder} object in a double dispatch
 * pattern to do message specific-processing in e.g. a node implementation.
 *
 * @author  Bob Jacobsen   Copyright 2009, 2010
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
     * To be equal, messages have to have the same type and content
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (! (o instanceof AddressedMessage)) {
            return false;
        }
        AddressedMessage msg = (AddressedMessage) o;
        if (!this.getDestNodeID().equals(msg.getDestNodeID())) {
            return false;
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode()+getDestNodeID().hashCode();
    }

    @Override
    public String toString() {
        return getSourceNodeID().toString()+" - "+getDestNodeID();
    }
}
