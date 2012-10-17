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
    
    public StreamDataSendMessage(NodeID source, NodeID dest, int[] data,
                    int destStreamID) {
        super(source, dest);
        this.data = data;
        this.destStreamID = destStreamID;
    }
        
    int[] data;
    int destStreamID;
    
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
        return super.toString()
                +" StreamDataSend "+data;     
    }

    public int getMTI() { return MTI_STREAM_DATA_SEND; }
}
