package org.openlcb.implementations;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.openlcb.*;

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
 *<li>Needs to timeout and resume operation if no reply received
 *</ul>
 *<p>
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class DatagramMeteringBuffer extends MessageDecoder {

    final static int TIMEOUT = 2000;
    
    public DatagramMeteringBuffer(Connection toDownstream) {
        this.toDownstream = toDownstream;
        new Thread(new Consumer(queue)).start();
        
        fromDownstream = new ReplyHandler();
    }
    
    Connection toDownstream;
    Connection fromDownstream;
    MessageMemo currentMemo;
    
    /**
     * This is where e.g. replies from the OpenLCB
     * network should be returned to.
     */
    public Connection connectionForRepliesFromDownstream() {
        return fromDownstream;
    }
    
    BlockingQueue<MessageMemo> queue = new LinkedBlockingQueue<MessageMemo>();
    
    /**
     * Accept a datagram message to be sent
     */
    @Override
    public void put(Message msg, Connection toUpstream) {
        if (msg instanceof DatagramMessage)
            queue.add(new MessageMemo(msg, toUpstream, toDownstream));
        else 
            toDownstream.put(msg, fromDownstream);
    }
    
    class ReplyHandler extends AbstractConnection {
        /*
         * Find the current handler and have it handle it
         */
        @Override
        public void put(Message msg, Connection sender) {
            if (currentMemo == null) return;
            currentMemo.put(msg, sender);
        }
    }
    
    class MessageMemo extends MessageDecoder {
        Message message;
        Connection toDownstream;
        Connection toUpstream;
        
        MessageMemo(Message msg, Connection toUpstream, Connection toDownstream) {
            message = msg;
            this.toUpstream = toUpstream;
            this.toDownstream = toDownstream;
        }
        
        public void sendIt() {
            currentMemo = this;
            toDownstream.put(message, fromDownstream);
        }
        /**
         * Handle "Datagram Acknowledged" message
         */
        @Override
        public void handleDatagramAcknowledged(DatagramAcknowledgedMessage msg, Connection sender){
            // forward message upstream
            toUpstream.put(msg, toUpstream);
            
            // and allow sending another
            new Thread(new Consumer(queue)).start();
        }
        
        /**
         * Handle "Datagram Rejected" message
         */
        @Override
        public void handleDatagramRejected(DatagramRejectedMessage msg, Connection sender){
            // need to check if this is from right source
            
            // check if resend permitted
            if (msg.canResend()) {
                toDownstream.put(message, fromDownstream);
            } else {
                // forward upstream to originator and let them sort it out
                toUpstream.put(msg, toUpstream);
                // and allow sending another
                new Thread(new Consumer(queue)).start();
            }
        }
    }
    
    static class Consumer implements Runnable {
        private final BlockingQueue<MessageMemo> queue;
        Consumer(BlockingQueue<MessageMemo> q) { queue = q; }
        @Override
        public void run() {
            try {
                consume(queue.take());
            } catch (InterruptedException ex) {}
            // and exits. Another has to be started with this item is done.
        }
        void consume(MessageMemo x) { x.sendIt(); }
    }
    
}
