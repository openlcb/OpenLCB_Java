package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Stream Data Send message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class StreamDataSendMessage extends AddressedMessage {
    
//    public StreamDataSendMessage(NodeID source, NodeID dest, byte[] data,
//                    byte destStreamID) {
    public StreamDataSendMessage(NodeID source, NodeID dest, int[] data) {
        super(source, dest);
        this.data = data;
        //this.destStreamID = destStreamID;
    }
        
    int[] data;
    byte destStreamID;
    byte getDestinationStreamID() { return destStreamID; }
    public int[] getData() { return data; }
    /**
     * Implement message-type-specific
     * processing when this message
     * is received by a node.
     *<p>
     * Default is to do nothing.
     */
     @Override
     public void applyTo(MessageDecoder decoder, Connection sender) {
        decoder.handleStreamDataSend(this, sender);
     }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        StreamDataSendMessage p = (StreamDataSendMessage) o;
        if (data.length != p.data.length) return false;
        // should also check data length
        if (destStreamID != p.destStreamID) return false;
        return super.equals(o);
    } 

    public String toString() {
        StringBuilder value = new StringBuilder(super.toString());
        value.append(" Stream: ");
        
        int n = data.length;
        if(n>16) n=16;
        boolean first = true;
        for (int i = 0; i<n; i++) {
            if (!first) value.append(".");
            value.append(Integer.toHexString((int)(data[i]&0xFF)).toUpperCase());
            first = false;
        }
        if(data.length>16) value.append(" ...");
        return new String(value);
    }

    public int getMTI() { return MTI_STREAM_DATA_SEND; }
}
