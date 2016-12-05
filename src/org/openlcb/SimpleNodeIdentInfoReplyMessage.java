package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Reply message for the Simple Node Ident Info protocol.
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision: 529 $
 */
@Immutable
@ThreadSafe
public class SimpleNodeIdentInfoReplyMessage extends AddressedPayloadMessage {
    
    /**
     * @param source sender Node ID
     * @param dest destination Node ID
     * @param dataIn the data content without extra wire-protocol bytes
     */
    public SimpleNodeIdentInfoReplyMessage(NodeID source, NodeID dest, byte[] dataIn) {
        super(source, dest, dataIn);
    }

    /**
     * To be equal, messages have to have the
     * same type and content
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (! (o instanceof SimpleNodeIdentInfoReplyMessage))
            return false;
        SimpleNodeIdentInfoReplyMessage msg = (SimpleNodeIdentInfoReplyMessage) o;
        if (this.getData().length != msg.getData().length)
            return false;
        int n = this.getData().length;
        byte[] d1 = this.getData();
        byte[] d2 = msg.getData();
        for (int i = 0; i<n; i++) {
            if (d1[i]!= d2[i]) return false;
        }
        return super.equals(o);
    }
    
    public byte[] getData() {
        return payload;
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
        decoder.handleSimpleNodeIdentInfoReply(this, sender);
     }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getSourceNodeID().toString());
        b.append(" - ");
        b.append(getDestNodeID().toString());
        b.append(" Simple Node Ident Info with content '");
        for (byte d : getData()) {
            if (d == 0) {
                b.append(',');
            } else if (d < 32) {
                b.append(Integer.toString(d));
                b.append(',');
            } else {
                b.append((char)d);
            }
        }
        b.append('\'');
        return b.toString();
    }

    @Override
    public MessageTypeIdentifier getEMTI() {
        return MessageTypeIdentifier.SimpleNodeIdentInfoReply;
    }
}
