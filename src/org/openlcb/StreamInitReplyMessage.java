package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Stream Initialization Reply message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class StreamInitReplyMessage extends AddressedMessage {
    
    public StreamInitReplyMessage(NodeID source, NodeID dest, 
            int bufferSize, int sourceStreamID, int destStreamID) {
        super(source, dest);
        this.bufferSize = bufferSize;
        this.sourceStreamID = sourceStreamID;
        this.destStreamID = destStreamID;
    }
    
    int bufferSize;
    int sourceStreamID;
    int destStreamID;
    
    public int getBufferSize() { return bufferSize; }
    public int getDestStreamID() { return destStreamID; }
    
    /**
     * Implement message-type-specific
     * processing when this message
     * is received by a node.
     *<p>
     * Default is to do nothing.
     */
     @Override
     public void applyTo(MessageDecoder decoder, Connection sender) {
        decoder.handleStreamInitReply(this, sender);
     }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        StreamInitReplyMessage p = (StreamInitReplyMessage) o;
        if (bufferSize != p.bufferSize) return false;
        if (sourceStreamID != p.sourceStreamID) return false;
        if (destStreamID != p.destStreamID) return false;
        return super.equals(o);
    } 

    public String toString() {
        return super.toString()
                +" StreamInitReply"
                +" SSID "+sourceStreamID
                +" DSID "+destStreamID
                +" bsize "+bufferSize;     
    }

    public int getMTI() { return MTI_STREAM_INIT_REPLY; }
}
