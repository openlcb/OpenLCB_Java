package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Datagram message implementation
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class DatagramMessage extends AddressedMessage {
    
    public DatagramMessage(NodeID source, NodeID dest, int[] dataIn) {
        super(source, dest);
        this.data = new int[dataIn.length];
        System.arraycopy(dataIn, 0, this.data, 0, dataIn.length);
    }
    
    int[] data;
    
     /**
      * To be equal, messages have to have the
      * same type and content
      */
     public boolean equals(Object o) {
        if (o == null) return false;
        if (! (o instanceof DatagramMessage))
            return false;
        DatagramMessage msg = (DatagramMessage) o;
        if (this.getData().length != msg.getData().length)
            return false;
        int n = this.getData().length;
        int[] d1 = this.getData();
        int[] d2 = msg.getData();
        for (int i = 0; i<n; i++) {
            if (d1[i]!= d2[i]) return false;
        }
        return super.equals(o);
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
        decoder.handleDatagram(this, sender);
    }

    public int[] getData() {
        return data;
    }
    
    public int getMTI() { return MTI_DATAGRAM; }
}
