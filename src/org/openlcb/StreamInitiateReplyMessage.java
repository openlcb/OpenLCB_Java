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
public class StreamInitiateReplyMessage extends AddressedPayloadMessage {
    
    public StreamInitiateReplyMessage(NodeID source, NodeID dest,
            int bufferSize, byte sourceStreamID, byte destStreamID) {
        super(source, dest, toPayload(bufferSize, sourceStreamID, destStreamID));
        this.bufferSize = bufferSize;
        this.sourceStreamID = sourceStreamID;
        this.destStreamID = destStreamID;
    }
    
    int bufferSize;
    byte sourceStreamID;
    byte destStreamID;

    public int getBufferSize() { return bufferSize; }
    public byte getDestinationStreamID() { return destStreamID; }
    public byte getSourceStreamID() { return sourceStreamID; } //dph 20151229

    static byte[] toPayload(int bufferSize, byte sourceStreamID, byte destStreamID) {
        byte[] b = new byte[]{0, 0, sourceStreamID, destStreamID};
        Utilities.HostToNetworkUint16(b, 0, bufferSize);
        return b;
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
        decoder.handleStreamInitiateReply(this, sender);
     }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        StreamInitiateReplyMessage p = (StreamInitiateReplyMessage) o;
        if (bufferSize != p.bufferSize) return false;
        if (sourceStreamID != p.sourceStreamID) return false;
        if (destStreamID != p.destStreamID) return false;
        return super.equals(o);
    } 

    public String toString() {
        return super.toString()
                +" SSID "+sourceStreamID
                +" DSID "+destStreamID
                +" bsize "+bufferSize;     
    }

    @Override
    public MessageTypeIdentifier getEMTI() {
        return MessageTypeIdentifier.StreamInitiateReply;
    }
}
