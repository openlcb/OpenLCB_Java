package org.openlcb;

// For annotations
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

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
    
    public DatagramMessage(NodeID source, NodeID dest, byte[] dataIn) {
        super(source, dest);
        this.data = new int[dataIn.length];
        for (int i = 0; i<dataIn.length; i++)
            this.data[i] = dataIn[i]&0xFF;
    }

    /**
     * Intended for use by subclasses only
     * to ensure immutable objects
     */
    protected DatagramMessage(NodeID source, NodeID dest) {
        super(source, dest);
    }
    
    @SuppressWarnings("JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
    protected int[] data;
    
     /**
      * To be equal, messages have to have the
      * same type and content
      */
    @Override
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
     
    @Override
     public int hashCode() { return getSourceNodeID().hashCode()+getDestNodeID().hashCode(); }
     
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
    
    @Override
    public int getMTI() { return MTI_DATAGRAM; }

    @Override
    public String toString() {
        StringBuilder value = new StringBuilder(super.toString());
        value.append(" Datagram: ");  
        
        int n = getData().length;
        boolean first = true;
        for (int i = 0; i<n; i++) {
            if (!first) value.append(".");
            value.append(Integer.toHexString((int)(data[i]&0xFF)).toUpperCase());
            first = false;
        }
        return new String(value);   
    }
}
