package org.openlcb.implementations;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    //final static int TIMEOUT = 700;
    final static int TIMEOUT = 3000;
    private final static Logger logger = Logger.getLogger(DatagramMeteringBuffer.class.getName());
    private ThreadPoolExecutor threadPool = null;
    final static int minThreads = 10;
    final static int maxThreads = 10;
    final static long threadTimeout = 10; // allowed idle time for threads, in seconds.

    /**
     * @param toDownstream connection object associated with the new buffer 
     *
     * @deprecated since OlcbLibrary version 0.18.  Use {@link #DatagramMeteringBuffer(Connection,ThreadPoolExecutor)} instead.
     */
    @Deprecated
    public DatagramMeteringBuffer(Connection toDownstream ){
          this(toDownstream,
               new ThreadPoolExecutor(minThreads,maxThreads,
                                      threadTimeout,TimeUnit.SECONDS,
                                 new LinkedBlockingQueue<Runnable>(),
                                 new OlcbThreadFactory()));
    }
    
    /**
     * @param toDownstream Connection object associated with the new buffer 
     * @param tpe Thread pool in which threads associated with the buffer run.
     */
    public DatagramMeteringBuffer(Connection toDownstream,ThreadPoolExecutor tpe) {
        threadPool = tpe;
        if(timer == null){
           timer = new Timer("OpenLCB-datagram-timer");
        }
        this.toDownstream = toDownstream;
        datagramComplete();
        
        fromDownstream = new ReplyHandler();
    }
    
    Connection toDownstream;
    Connection fromDownstream;
    MessageMemo currentMemo;
    private Timer timer = null;
    int timeoutMillis = TIMEOUT;

    /**
     * This is where e.g. replies from the OpenLCB
     * network should be returned to.
     * @return the connection where to forward messages from the bus (typically the datagram service object)
     */
    public Connection connectionForRepliesFromDownstream() {
        return fromDownstream;
    }
    
    BlockingQueue<MessageMemo> queue = new LinkedBlockingQueue<MessageMemo>();
    int pendingEntries = 0;
    int threadPending = 0;

    public void setTimeout(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * Waits until all pending entries are sent or we are blocked on sending multiple requests to
     * the same target node.
     */
    public void waitForSendQueue() {
        while(true) {
            synchronized (this) {
                if (pendingEntries == 0 || threadPending == 0) {
                    break;
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                return;
            }
        }
        waitForTimer();
    }

    /**
     * Waits until all pending entries are sent and their callbacks are executed.
     * @throws java.lang.InterruptedException when interrupted.
     */
    public void waitForSendCallbacks() throws InterruptedException {
        while(true) {
            synchronized (this) {
                if (pendingEntries == 0 && threadPending == 1) {
                    break;
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw e;
            }
        }
        waitForTimer();
    }

    private void waitForTimer() {
        final Semaphore s = new Semaphore(0);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                s.release();
            }
        }, 1);
        try {
            s.acquire();
        } catch (InterruptedException e) {
            return;
        }
    }

    /**
     * Accept a datagram message to be sent
     */
    @Override
    public void put(Message msg, Connection toUpstream) {
        if (msg instanceof DatagramMessage) {
            synchronized (this) {
                ++pendingEntries;
            }
            queue.add(new MessageMemo((DatagramMessage) msg, toUpstream, toDownstream));
        } else {
            toDownstream.put(msg, fromDownstream);
        }
    }

    private void datagramComplete() {
        currentMemo = null;
        synchronized (this) {
            threadPending++;
        }
        threadPool.execute(new Consumer(queue));
    }

    class ReplyHandler extends AbstractConnection {
        /*
         * Find the current handler and have it handle it
         */
        @Override
        public void put(Message msg, Connection sender) {
            if (currentMemo == null) {
                return;
            }
            currentMemo.put(msg, sender);
        }
    }
        
    class MessageMemo extends MessageDecoder {
        DatagramMessage message;
        Connection toDownstream;
        Connection toUpstream;
        TimerTask timerTask;

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
            startTimeout();
            toDownstream.put(message, fromDownstream);
        }
        
        void startTimeout() {
            timerTask = new TimerTask(){
                public void run(){
                    timerExpired();
                }
            };
            try {
                timer.schedule(timerTask, timeoutMillis);
            } catch( java.lang.IllegalStateException ise) {
                logger.log(Level.WARNING, "Timer already canceled when starting timeout for datagram {0}", message != null ? message : " == null");
            }
        }

        void endTimeout() {
            if (timerTask != null) timerTask.cancel();
            else logger.log(Level.INFO, "Found timer null for datagram {0}", message != null ? message : " == null");
        }

        void timerExpired() {
            // should not happen, but if it does, 
            // fabricate a permanent error and forward up
            DatagramRejectedMessage msg = new DatagramRejectedMessage(message.getDestNodeID(), message.getSourceNodeID(), 0x0100);
            logger.log(Level.INFO, "Never received reply for datagram {0}", message);
            handleDatagramRejected(msg, null);
            // Inject message to upstream listener
            toUpstream.put(msg, toUpstream);
        }

        /**
         * Handle "Node Init Complete" message and kill timeout if our destination node has reset.
         */
        @Override
        public void handleInitializationComplete(InitializationCompleteMessage msg, Connection
                sender) {
            if (msg.getSourceNodeID() != null && msg.getSourceNodeID().equals(message
                    .getDestNodeID())) {
                // destination node has reset. Let's stop waiting for replies.
                DatagramRejectedMessage rejectedMessage = new DatagramRejectedMessage(message
                        .getDestNodeID(), message.getSourceNodeID(),
                        DatagramRejectedMessage.DATAGRAM_REJECTED_DST_REBOOT);
                logger.log(Level.INFO, "Destination node has rebooted while waiting for datagram reply {0}", message);
                handleDatagramRejected(rejectedMessage, null);
                // Inject message to upstream listener
                toUpstream.put(rejectedMessage, toUpstream);
            }
        }

        /**
         * Handle "Datagram Acknowledged" message
         */
        @Override
        public void handleDatagramAcknowledged(DatagramAcknowledgedMessage msg, Connection sender){
            // check if this is from right source & to us
            if ( ! (msg.getDestNodeID()!=null && msg.getSourceNodeID()!=null && msg.getDestNodeID().equals(message.getSourceNodeID()) && message.getDestNodeID().equals(msg.getSourceNodeID()) ) ) {
                // not for us
                return;
            }
            endTimeout();
            // allow sending another
            datagramComplete();
        }
        
        /**
         * Handle "Datagram Rejected" message
         */
        @Override
        public void handleDatagramRejected(DatagramRejectedMessage msg, Connection sender) {
            // check if this is from right source & to us
            if ( ! (msg.getDestNodeID()!=null && msg.getSourceNodeID()!=null && msg.getDestNodeID().equals(message.getSourceNodeID()) && message.getDestNodeID().equals(msg.getSourceNodeID()) ) ) {
                // not for us
                return;
            }
            endTimeout();
            // check if resend permitted
            if (msg.canResend()) {
                forwardDownstream();
            } else {
                // allow sending another
                datagramComplete();
            }
        }
    }

    /**
     * cleanup local resources
     */
    public void dispose(){
        // shut down the thread pool
        if(threadPool != null && !(threadPool.isShutdown())) {
           // modified from the javadoc for ExecutorService 
           threadPool.shutdown(); // Disable new tasks from being submitted
           try {
              // Wait a while for existing tasks to terminate
              if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                 threadPool.shutdownNow(); // Cancel currently executing tasks
                 // Wait a while for tasks to respond to being cancelled
                 if (!threadPool.awaitTermination(10, TimeUnit.SECONDS))
                     logger.warning("Pool did not terminate");
              }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                threadPool.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }
        threadPool = null;
        // and cancel the timer
        timer.cancel();
        timer = null;
    }    


    class Consumer implements Runnable {
        private final BlockingQueue<MessageMemo> queue;
        Consumer(BlockingQueue<MessageMemo> q) { queue = q; }
        @Override
        public void run() {
            if(threadPool == null || threadPool.isShutdown()) {
               // the buffer has been disposed of, so just return
               return;
            }
            try {
                consume(queue.take());
                synchronized (DatagramMeteringBuffer.this) {
                    pendingEntries--;
                }
            } catch (InterruptedException ex) {
                // interrupted while processing, but not in a loop.
            } finally {
                synchronized (DatagramMeteringBuffer.this) {
                    threadPending--;
                }
            }
            // and exits. Another has to be started with this item is done.
        }
        void consume(MessageMemo x) { x.sendIt(); }
    }    
}
