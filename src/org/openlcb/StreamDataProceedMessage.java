package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Stream Data Proceed message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class StreamDataProceedMessage extends AddressedMessage {
    
    public StreamDataProceedMessage(NodeID source, NodeID dest, 
                        int sourceStreamID, int destStreamID) {
        super(source, dest);
        this.sourceStreamID = sourceStreamID;
        this.destStreamID = destStreamID;
    }
        
    int sourceStreamID;
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
        decoder.handleStreamDataProceed(this, sender);
     }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        StreamDataProceedMessage p = (StreamDataProceedMessage) o;
        if (sourceStreamID != p.sourceStreamID) return false;
        if (destStreamID != p.destStreamID) return false;
        return super.equals(o);
    } 

    public String toString() {
        return super.toString()
                +" StreamDataProceed";     
    }

    public int getMTI() { return MTI_STREAM_DATA_PROCEED; }
}
