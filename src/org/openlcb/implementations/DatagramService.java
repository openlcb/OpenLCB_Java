package org.openlcb.implementations;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.openlcb.*;

/**
 * Service for sending and receiving data via datagrams.
 * 
 * Meant to shield the using code from all the details of that
 * process via some primitives:
 * <ul>
 * <li>Send data via a memo that gets notified on success
 * <li>Register to be notified when data of a particular type arrives
 * </ul>
 *
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class DatagramService extends MessageDecoder {

    /**
     * @param downstream Connection in the direction of the layout
     */
    public DatagramService(NodeID here, Connection downstream) {
        this.here = here;
        this.downstream = downstream;
        
    }
    
    static final int DEFAULT_ERROR_CODE = 0x1000;
    NodeID here;
    Connection downstream;

    /**
     * Send data to layout
     */
    public void sendData(DatagramServiceTransmitMemo memo){
        xmtMemo = memo;
        Message m = new DatagramMessage(here, memo.dest, memo.data);
        downstream.put(m, this);
    }

    /**
     * Handle "Datagram" message from layout
     */
    public void handleDatagram(DatagramMessage msg, Connection sender){
        // forward
        int retval = DEFAULT_ERROR_CODE;
        if (rcvMemo != null && rcvMemo.type == msg.getData()[0]) {
            retval = rcvMemo.handleData(msg.getSourceNodeID(), msg.getData());
        }
        if (retval  == 0) {
            // accept
            Message m = new DatagramAcknowledgedMessage(here, msg.getSourceNodeID());
            downstream.put(m, this);
        } else {
            // reject
            Message m = new DatagramRejectedMessage(here, msg.getSourceNodeID(), retval);
            downstream.put(m, this);
        }
    }

    /**
     * Handle positive datagram reply message from layout
     */
    public void handleDatagramAcknowledged(DatagramAcknowledgedMessage msg, Connection sender){
        if (xmtMemo != null) {
            xmtMemo.handleReply(0);
        }
        xmtMemo = null;
    }

    DatagramServiceReceiveMemo rcvMemo;
    DatagramServiceTransmitMemo xmtMemo;
    
    /**
     * Accept request to notify for a particular
     * type of datagram
     */
    public void registerForReceive(DatagramServiceReceiveMemo memo) {
        this.rcvMemo = memo;
    }
    
    @Immutable
    @ThreadSafe    
    static protected class DatagramServiceReceiveMemo {
        public DatagramServiceReceiveMemo(int type) {
            this.type = type;
        }

        final int type;
        
        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (! (o instanceof DatagramServiceReceiveMemo)) return false;
            return this.type == ((DatagramServiceReceiveMemo)o).type;
        } 
    
        @Override
        public String toString() {
            return "DatagramServiceReceiveMemo: "+type;
        }
        
        @Override
        public int hashCode() { return type; }
        
        /**
         * Overload this to for notification of data.
         * @return 0 for OK, non-zero for error reply
         */
        public int handleData(NodeID n, int[] data) { 
            // default is error
            return DEFAULT_ERROR_CODE; 
        }

    }

    /**
     * Memo class to hold information about request while
     * it's being processed.
     * 
     * @ToDo copy in and out the data contents to make truly immutable
     * @ToDo are these really immutable, given that subclass will inherit and change them?
     */
    @Immutable
    @ThreadSafe    
    static protected class DatagramServiceTransmitMemo {
        public DatagramServiceTransmitMemo(NodeID dest, int[] data) {
            this.data = data;
            this.dest = dest;
        }

        protected DatagramServiceTransmitMemo(NodeID dest) {
            this.data = null;  // sends zero-byte datagram
            this.dest = dest; 
        }
        
        protected int[] data;
        final NodeID dest;
        
        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (! (o instanceof DatagramServiceTransmitMemo)) return false;
            DatagramServiceTransmitMemo m = (DatagramServiceTransmitMemo) o;
            if (this.data.length != m.data.length) return false;
            if (this.dest != m.dest) return false;
            for (int i = 0; i < this.data.length; i++)
                if (this.data[i] != m.data[i]) return false;
            return true;
        } 
    
        @Override
        public String toString() {
            return "DatagramServiceTransmitMemo: "+Utilities.toHexDotsString(data);
        }
        
        @Override
        public int hashCode() { return this.data.length+this.data[0]+dest.hashCode(); }
        
        /**
         * Overload this to for notification of response
         * @param code 0 for OK, non-zero for error reply
         */
        public void handleReply(int code) { 
        }

    }
    
}
