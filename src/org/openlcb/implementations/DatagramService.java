package org.openlcb.implementations;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * Does not handle retry of negative replies. For that, see {@link DatagramMeteringBuffer}.
 * <p>
 * Multiple copies of this can exist.  
 * <ul>
 * <li>Replies to sent datagrams are processed one-by-one here. This object
 *      can send to multiple destination nodes, but it cannot overlap sent messages:
 *      each send&amp;reply must be complete before the next is started.
 * <li>Incoming datagrams for this node are forwarded to the receiving
 *      code, regardless of whether the sending node was ever a destination.
 * </ul>
 *
 * @author  Bob Jacobsen   Copyright 2012, 2015
 * @version $Revision$
 */
public class DatagramService extends MessageDecoder {

    private final static Logger logger = Logger.getLogger(DatagramService.class.getName());

    /**
     * @param here       our node ID
     * @param downstream Connection in the direction of the layout
     */
    public DatagramService(NodeID here, Connection downstream) {
        this.here = here;
        this.downstream = downstream;

    }

    public static final int FLAG_REPLY_PENDING = 0x80;
    /// Default error code (permanent error).
    public static final int DEFAULT_ERROR_CODE = 0x1000;
    /// Error code for "Invalid arguments, payload too short".
    public static final int ERROR_TOO_SHORT = 0x1081;
    /// Error code for "Unimplemented, unsupported datagram type".
    public static final int ERROR_UNSUPPORTED_DATAGRAM_TYPE = 0x1042;

    /// Use this value to datagram reply memory to accept the datagram with indicating that a reply datagram will come.
    public static final int ACCEPT_REPLY_PENDING = (FLAG_REPLY_PENDING << 16);
    NodeID here;
    Connection downstream;
    final Map<Integer, DatagramServiceReceiveMemo> receivers = new HashMap<>();

    /**
     * Send data to layout
     *
     * @param memo datagram to send
     */
    public void sendData(DatagramServiceTransmitMemo memo) {
        if (xmtMemo != null) {
            logger.log(Level.SEVERE, "Overriding datagram transmit memo. old {0} new {1}", new Object[]{xmtMemo, memo});
        }
        xmtMemo = memo;
        Message m = new DatagramMessage(here, memo.dest, memo.data);
        downstream.put(m, this);
    }

    /**
     * Send data to layout
     *
     * @param dest target node ID
     * @param data datagram payload
     */
    public void sendData(NodeID dest, int[] data) {
        DatagramServiceTransmitMemo memo = new DatagramServiceTransmitMemo(dest, data) {
            @Override
            public void handleSuccess(int flags) {
            }

            @Override
            public void handleFailure(int errorCode) {
            }
        };
        xmtMemo = memo;
        Message m = new DatagramMessage(here, memo.dest, memo.data);
        downstream.put(m, this);
    }

    /**
     * Handle "Datagram" message from layout
     */
    @Override
    public void handleDatagram(DatagramMessage msg, Connection sender) {
        // ignore if not for here
        if (!msg.getDestNodeID().equals(here)) return;

        // forward
        ReplyMemo replyMemo = new ReplyMemo(msg, downstream, here, this);
        if (msg.getData() == null) {
            new Exception("Unexpected null content of datagram").printStackTrace();
            replyMemo.acceptData(DEFAULT_ERROR_CODE);
            return;
        }
        if (msg.getData().length == 0) {
            new Exception("Unexpected zero length content of datagram").printStackTrace();
            replyMemo.acceptData(ERROR_TOO_SHORT);
            return;
        }
        int t = msg.getData()[0] & 0xff;
        DatagramServiceReceiveMemo receiveMemo = receivers.get(t);
        if (receiveMemo == null) {
            replyMemo.acceptData(ERROR_UNSUPPORTED_DATAGRAM_TYPE);
            return;
        }
        receiveMemo.handleData(msg.getSourceNodeID(), msg.getData(), replyMemo);
        // check that client replied
        if (!replyMemo.hasReplied()) {
            logger.log(Level.SEVERE, "No internal reply received to datagram with contents {0}", Utilities.toHexDotsString(msg.getData())); //log
        }
    }


    /**
     * Handle negative datagram reply message from layout
     */
    @Override
    public void handleDatagramRejected(DatagramRejectedMessage msg, Connection sender){
        if (xmtMemo != null && msg.getDestNodeID().equals(here) && xmtMemo.dest.equals(msg.getSourceNodeID()) ) {
            if (msg.canResend()) return;
            DatagramServiceTransmitMemo temp = xmtMemo;
            xmtMemo = null;
            temp.handleFailure(msg.getCode());
        }
    }

    /**
     * Handle positive datagram reply message from layout
     */
    @Override
    public void handleDatagramAcknowledged(DatagramAcknowledgedMessage msg, Connection sender){
        if (xmtMemo != null && msg.getDestNodeID().equals(here) && xmtMemo.dest.equals(msg.getSourceNodeID()) ) {
            DatagramServiceTransmitMemo temp = xmtMemo;
            xmtMemo = null;
            temp.handleSuccess(msg.getFlags());
        }
    }

    DatagramServiceTransmitMemo xmtMemo;
    
    /**
     * Accept request to notify for a particular
     * type of datagram
     * @param memo    datagram listener
     */
    public synchronized void registerForReceive(DatagramServiceReceiveMemo memo) {
        receivers.put(memo.type, memo);
    }

    /**
     * Stops accepting request to notify for a particular
     * type of datagram
     * @param memo    datagram listener that was previously registered
     */
    public synchronized void unRegisterForReceive(DatagramServiceReceiveMemo memo) {
        DatagramServiceReceiveMemo old = receivers.get(memo.type);
        if (old == memo) {
            receivers.remove(memo.type);
        } else {
            logger.log(Level.SEVERE, "Unregistering a datagram listener that is not registered for type {0}", Integer.toString(memo.type)); //log
            new Exception("Unexpected unregister for datagram listener").printStackTrace();
        }
    }

    @Immutable
    @ThreadSafe
    static public class DatagramServiceReceiveMemo {
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
        
        /*
         * Client tells the datagram service that
         * it's accepted the data and the buffer 
         * can be reused.
         * @param resultCode  zero for OK, or as documented in Datagram spec for reply
         */
        public void acceptData(int resultCode) {

        }
        /**
         * Overload this for notification of data.
         * 
         * @param n       sender node ID (somewhere on the bus)
         * @param data    payload that came
         * @param service Implementations must reply to the datagram by invoking
         *                  reply.acceptData(int replycode)
         *              before returning.  (This is done, instead of using the 
         *              return value, to allow the receiving code to reply immediately
         *              and queue other activity afterwards)
         * 
         */
        public void handleData(NodeID n, int[] data, ReplyMemo service) { 
            // default is error
            service.acceptData(DEFAULT_ERROR_CODE);
        }

    }
    
    @Immutable
    static public class ReplyMemo {
        DatagramMessage msg;
        Connection downstream;
        NodeID here;
        DatagramService service;
        boolean replied = false;

        protected ReplyMemo(DatagramMessage msg, Connection downstream, NodeID here, DatagramService service) {
            this.msg = msg;
            this.downstream = downstream;
            this.here = here;
            this.service = service;
        }

        /**
         * called to indicate whether the datagram was accepted or not
         *
         * @param resultCode 0 for OK, non-zero 16-bit LSB value for error reply;
         *                   ACCEPT_REPLY_PENDING for indicating that a response datagram will be sent.
         */
        public void acceptData(int resultCode) {
            replied = true;
            if ((resultCode & 0xFFFF) == 0) {
                // accept
                int flags = (resultCode >> 16) & 0xFF;
                Message m = new DatagramAcknowledgedMessage(here, msg.getSourceNodeID(), flags);
                downstream.put(m, service);
            } else {
                // reject
                Message m = new DatagramRejectedMessage(here, msg.getSourceNodeID(), resultCode & 0xFFFF);
                downstream.put(m, service);
            }

        }
        
        boolean hasReplied() { return replied; }

    }

    /**
     * Memo class to hold information about request while
     * it's being processed.
     */
     // TODO copy in and out the data contents to make truly immutable
     // TODO are these really immutable, given that subclass will inherit and change them?
    @Immutable
    @ThreadSafe    
    static public abstract class DatagramServiceTransmitMemo {
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
            return "DatagramServiceTransmitMemo to " + dest.toString() + ": "+Utilities.toHexDotsString(data);
        }
        
        @Override
        public int hashCode() { return this.data.length+this.data[0]+dest.hashCode(); }
        
        /**
         * Notifies that the datagram was accepted by the destination.
         * @param flags are the 8-bit flags returned by the destination; bit 0x80 for reply
         *              pending.
         */
        public abstract void handleSuccess(int flags);

        /**
         * Notifies that the datagram send has failed. Retryable errors are internally retried
         * and not reported here.
         * @param errorCode the 16-bit OpenLCB error code.
         */
        public abstract void handleFailure(int errorCode);

    }
    
}
