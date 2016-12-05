package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Stream Initialization Request message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class StreamInitiateRequestMessage extends AddressedPayloadMessage {
    
    public StreamInitiateRequestMessage(NodeID source, NodeID dest,
                    int bufferSize, byte sourceStreamID, byte destinationStreamID) {
        super(source, dest, toPayload(bufferSize, sourceStreamID, destinationStreamID));
        this.bufferSize = bufferSize;
        this.sourceStreamID = sourceStreamID;
        this.destinationStreamID = destinationStreamID;
    }
    
    int bufferSize;
    byte sourceStreamID;
    byte destinationStreamID;
    
    public int getBufferSize() { return bufferSize; }
    public byte getSourceStreamID() { return sourceStreamID; }
    public byte getDestinationStreamID() { return destinationStreamID; }

    static byte[] toPayload(int bufferSize, byte sourceStreamID, byte destStreamID) {
        byte[] b = new byte[]{0, 0, 0, 0, sourceStreamID, destStreamID};
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
        decoder.handleStreamInitiateRequest(this, sender);
     }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        StreamInitiateRequestMessage p = (StreamInitiateRequestMessage) o;
        if (bufferSize != p.bufferSize) return false;
        if (sourceStreamID != p.sourceStreamID) return false;
        if (destinationStreamID != p.destinationStreamID) return false;
        return super.equals(o);
    } 

    public String toString() {
        return super.toString()
                +" SSID "+sourceStreamID
                +" DSID "+destinationStreamID
                +" bsize "+bufferSize;
    }

    @Override
    public MessageTypeIdentifier getEMTI() {
        return MessageTypeIdentifier.StreamInitiateRequest;
    }

    public int getMTI() { return MTI_STREAM_INIT_REQUEST; }
}
