package org.openlcb.implementations;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Timer;
import java.util.TimerTask;

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
 *<li>Needs to timeout and resume operation if no reply received
 *</ul>
 *<p>
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class DatagramMeteringBuffer extends MessageDecoder {

    final static int TIMEOUT = 700;
    
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
            queue.add(new MessageMemo((DatagramMessage)msg, toUpstream, toDownstream));
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
        DatagramMessage message;
        Connection toDownstream;
        Connection toUpstream;
        
        MessageMemo(DatagramMessage msg, Connection toUpstream, Connection toDownstream) {
            message = msg;
            this.toUpstream = toUpstream;
            this.toDownstream = toDownstream;
        }
        
        public void sendIt() {
            currentMemo = this;
            forwardDownstream();
        }

        void forwardDownstream() {
            toDownstream.put(message, fromDownstream);
            startTimeout();
        }
        
        Timer timer;
        void startTimeout() {
            timer = new Timer();
            TimerTask task = new TimerTask(){
                public void run(){
                    timerExpired();
                }
            };
            timer.schedule(task, TIMEOUT);
            
        }
        void endTimeout() {
            if (timer != null) timer.cancel();
            else System.out.println("Found timer null for datagram "+(message != null ? message.toString() : " == null"));
        }
        void timerExpired() {
            // should not happen, but if it does, 
            // fabricate a permanent error and forward up
            DatagramRejectedMessage msg = new DatagramRejectedMessage(message.getDestNodeID(), message.getSourceNodeID(), 0x0100);
            System.out.println("Never received reply for datagram "+(message != null ? message.toString() : " == null"));
            handleDatagramRejected(msg, null);
        }

        /**
         * Handle "Datagram Acknowledged" message
         */
        @Override
        public void handleDatagramAcknowledged(DatagramAcknowledgedMessage msg, Connection sender){
            // check if this is from right source & to us
            if ( ! (msg.getDestNodeID()!=null && msg.getSourceNodeID()!=null && msg.getDestNodeID().equals(message.getSourceNodeID()) && message.getDestNodeID().equals(msg.getSourceNodeID()) ) ) {
                // not for us, just forward
                toUpstream.put(msg, toUpstream);
                return;
            }
            endTimeout();
            // forward message upstream
            toUpstream.put(msg, toUpstream);

            // and allow sending another
            new Thread(new Consumer(queue)).start();
        }
        
        /**
         * Handle "Datagram Rejected" message
         */
        @Override
        public void handleDatagramRejected(DatagramRejectedMessage msg, Connection sender) {
            // check if this is from right source & to us
            if ( ! (msg.getDestNodeID()!=null && msg.getSourceNodeID()!=null && msg.getDestNodeID().equals(message.getSourceNodeID()) && message.getDestNodeID().equals(msg.getSourceNodeID()) ) ) {
                // not for us, just forward
                toUpstream.put(msg, toUpstream);
                return;
            }
            endTimeout();
            // check if resend permitted
            if (msg.canResend()) {
                forwardDownstream();
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
