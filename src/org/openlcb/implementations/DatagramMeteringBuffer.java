package org.openlcb.implementations;

import org.openlcb.*;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Accepts Datagrams from one or more sources, and meters them out
 * to a downstream node (e.g. on CAN), one at a time.
 *<p>
 * Does not parallelize Datagrams to separate nodes, but could
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
        queue.add(new MessageMemo(msg, upstream, downstream));
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
