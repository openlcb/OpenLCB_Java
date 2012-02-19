package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Reply message for the Simple Node Ident Info protocol 
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision: 529 $
 */
@Immutable
@ThreadSafe
public class SimpleNodeIdentInfoReplyMessage extends Message {
    
    public SimpleNodeIdentInfoReplyMessage(NodeID source, byte[] dataIn) {
        super(source);
        this.data = new byte[dataIn.length];
        System.arraycopy(dataIn, 0, this.data, 0, dataIn.length);
    }
        
    byte[] data;
    
    /**
     * To be equal, messages have to have the
     * same type and content
     */
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
        return data;
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
    
    public String toString() {
        return getSourceNodeID().toString()
                +" Simple Node Ident Info with content "+getData();   
    }

    public int getMTI() { return MTI_SIMPLE_NODE_IDENT_REPLY; }
}
