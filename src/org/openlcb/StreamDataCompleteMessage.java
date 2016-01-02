package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Stream Data Complete message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class StreamDataCompleteMessage extends AddressedMessage {
    
    public StreamDataCompleteMessage(NodeID source, NodeID dest,
                byte sourceStreamID, byte destStreamID) {
        super(source, dest);
    }
        
    byte sourceStreamID;
    byte destStreamID;
    
    public byte getSourceStreamID() { return sourceStreamID; }
    public byte getDestinationStreamID() { return destStreamID; }


    /**
     * Implement message-type-specific
     * processing when this message
     * is received by a node.
     *<p>
     * Default is to do nothing.
     */
     @Override
     public void applyTo(MessageDecoder decoder, Connection sender) {
        decoder.handleStreamDataComplete(this, sender);
     }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        StreamDataCompleteMessage p = (StreamDataCompleteMessage) o;
        if (sourceStreamID != p.sourceStreamID) return false;
        if (destStreamID != p.destStreamID) return false;
        return true;
    } 

    public String toString() {
        return super.toString()
                +" StreamDataComplete";     
    }

    public int getMTI() { return MTI_STREAM_DATA_COMPLETE; }
}
