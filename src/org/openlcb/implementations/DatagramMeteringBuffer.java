package org.openlcb.implementations;

import org.openlcb.*;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Accepts Datagrams over a Connection from "upstream", and meters them out
 * to "downstream" nodes (e.g. on a CAN network), one at a time.
 * This is to ensure that e.g. simple CAN nodes that can't accept more
 * than one datagram at a time get a chance to reply before the next one
 * arrives.
 *<p>
 * Datagram negative replies cause a local retransmission. Positive
 * replies are reflected upstream to original source of the datagram.
 *<p>
 *<ul>
 *<li>Does not parallelize Datagrams to separate nodes
 *<li>Does not yet check NAK for transient vs permanent
 *<li>Needs to timeout and resume operation is no reply received
 *</ul>
 *<p>
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class DatagramMeteringBuffer extends MessageDecoder {

    final static int TIMEOUT = 2000;
    
    public DatagramMeteringBuffer(Connection downstream) {
        this.downstream = downstream;
        new Thread(new Consumer(queue)).start();
    }
    
    Connection downstream;
    
    BlockingQueue<MessageMemo> queue = new LinkedBlockingQueue<MessageMemo>();
    
    /**
     * Accept a datagram message to be sent
     */
    
    public void put(Message msg, Connection upstream) {
        if (msg instanceof DatagramMessage)
            queue.add(new MessageMemo(msg, upstream, downstream));
        else 
            downstream.put(msg, this);
    }
    
    class MessageMemo extends MessageDecoder {
        Message message;
        Connection downstream;
        Connection upstream;
        
        MessageMemo(Message msg, Connection u, Connection d) {
            message = msg;
            upstream = u;
            downstream = d;
        }
        
        public void sendIt() {
            downstream.put(message, this);
        }
        /**
         * Handle "Datagram Acknowledged" message
         */
        public void handleDatagramAcknowledged(DatagramAcknowledgedMessage msg, Connection sender){
            // forward message upstream
            upstream.put(msg, upstream);
            
            // and allow sending another
            new Thread(new Consumer(queue)).start();
        }
        
        /**
         * Handle "Datagram Rejected" message
         */
        public void handleDatagramRejected(DatagramRejectedMessage msg, Connection sender){
            // need to check if this is from right source
            
            // resend
            downstream.put(message, this);
        }
    }
    
    class Consumer implements Runnable {
        private final BlockingQueue<MessageMemo> queue;
        Consumer(BlockingQueue<MessageMemo> q) { queue = q; }
        public void run() {
            try {
                consume(queue.take());
            } catch (InterruptedException ex) {}
        }
        void consume(MessageMemo x) { x.sendIt(); }
    }
    
}
