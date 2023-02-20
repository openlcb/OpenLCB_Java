package org.openlcb.hub;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple multi-threaded OpenLCB hub implementation.
 * <P>
 * Multiple connections send and receive lines terminated by newline,
 * each of which is echoed to all other connections.
 * <P>
 * Line end behaviours can be changed to not require the newline between
 * incoming CAN Frames or adding them at the end of a Frame whilst sending.
 * <P>
 * Code for finding start / end of CAN frames without line feeds adapted from JMRI.
 * <p>
 * Hub without line endings currently supports the GridConnect Serial spec for
 * their CANUSB Interface, section 2.7.1 Message String Syntax in the pdf from
 * gridconnect.com/collections/can-pc-interfaces/products/canusb-com-fd-converter-usb-can-fd-interface#documents-and-drivers
 * <p>
 * main() directly invokes an object of the class.
 * <p>
 * Current threading model does all the sending from a
 * a single thread.  If this is observed to back up &amp;
 * halt all flow, individual transmit queues and threads
 * may be needed.
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision: 17977 $
 */

public class Hub {
    private final static Logger logger = Logger.getLogger(Hub.class.getName());
    public final static int DEFAULT_PORT = 12021;
    final static int CAPACITY = 20;  // not too long, to reduce delay
    private final boolean sendLineEndings;
    private final boolean requireIncomingLineEndings;
    private boolean disposed = false;

    /**
     * Constructs a new instance using default values.
     * Port - 12021
     * Send line endings - true
     * Require incoming line endings - true
     */
    public Hub() {
        this(Hub.DEFAULT_PORT);
    }

    /**
     * Constructs a new instance using a specified port.
     * Send line endings - true
     * Require incoming line endings - true
     * @param port the port number to use for incoming connections.
     */
    public Hub(int port) {
        this(port, true, true);
    }

    /**
     * Constructs a new instance with the specified port and line end behaviour.
     * @param port the port number to use for incoming connections.
     * @param sendLineEndings true if line endings should be added to sent messages, false otherwise.
     * @param requireIncomingLineEndings true if line endings should be expected in received messages,
     *                      false to detect messages using GridConnect Serial format.
     */
    public Hub(int port, boolean sendLineEndings, boolean requireIncomingLineEndings ) {
        this.port = port;
        this.sendLineEndings = sendLineEndings;
        this.requireIncomingLineEndings = requireIncomingLineEndings;
        createServerThread();
    }

    private void createServerThread() {
        // create array server thread
        Thread t = new Thread("openlcb-hub-output") {
            @Override
            public void run() {
                while (!disposed) {
                    try {
                        // as items arrive in queue, forward to every available connection
                        Memo m = queue.take();
                        for ( Forwarding e : threads) {
                            e.forward(m);
                        }
                    } catch (InterruptedException e) {
                        logger.severe("Hub: Interrupted in queue handling loop");
                        logger.log(Level.SEVERE, "", e);
                        dispose();
                        return; // we have been asked to exit.
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }
    
    BlockingQueue<Memo> queue = new LinkedBlockingQueue<>();
    ArrayList<Forwarding> threads = new ArrayList<>();
    final int port;

    /**
     * Starts the server and listens to incoming connections.
     */
    public void start() {
        try (ServerSocket service = new ServerSocket(port)) {
            while (!disposed) {
                Socket clientSocket = service.accept();
                ReaderThread r = new ReaderThread(clientSocket);
                addForwarder(r);
                r.start();
                // not setting Daemon, so program will wait for thread to end before terminating
                notifyOwner("Connection started with "+getRemoteSocketAddress(clientSocket));
            }
        } catch (IOException e) {
            logger.severe("Hub: Exception in main loop");
            logger.log(Level.SEVERE, "", e);
            notifyOwner(e.getLocalizedMessage());
            dispose();
        }
    }
    
    public int getPort() { return port; }
    public void addForwarder(Forwarding f) {
        threads.add(f);
    }
    
    public void notifyOwner(String line) {
        logger.info(line);
    }
    
    // from jmri.util.SocketUtil
    String getRemoteSocketAddress(Socket socket) {
        try {
            return  socket.getRemoteSocketAddress().toString();
        } catch (Throwable e) {
        } finally {
//            return "<unknown>";
        }
        return "<unknown>";
    }
    
    public void putLine(String line) {
        try {
            queue.put(new Memo(line, null));
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "", e);
        }
    }

    public void dispose() {
        notifyOwner("Hub Shutting Down");
        disposed = true;
    }

    public interface Forwarding {
        public void forward(Memo m);
    }
    
    class ReaderThread extends Thread implements Forwarding {
    
        ReaderThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }
        
        Socket clientSocket;
        PrintStream output;
        
        @Override
        public void run() {
            try ( DataInputStream input = new DataInputStream(clientSocket.getInputStream());
                    BufferedReader bfr = new BufferedReader(new InputStreamReader(input));
            ) {
                output = new PrintStream(clientSocket.getOutputStream(),true,"ISO-8859-1");
                while (!disposed) {
                    String line;
                    if (requireIncomingLineEndings) {
                        line = bfr.readLine();
                    } else {
                        line = loadChars( input);
                    }
                    if (line == null) break;  // socket ended
                    queue.put(new Memo(line, this));
                }
     
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Hub: Error while handling input from {0}", getRemoteSocketAddress(clientSocket));
                logger.log(Level.SEVERE, "", e);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Hub: Interrupted while handling input from {0}", getRemoteSocketAddress(clientSocket));
                logger.log(Level.SEVERE, "", e);
            }
            threads.remove(this);
            notifyOwner("Connection ended with "+getRemoteSocketAddress(clientSocket));
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.severe("Hub: Error while closing socket at end of connection");
                logger.log(Level.SEVERE, "", e);
            }
        }
        
        // increase to 140 for FD CAN Frame Support, should not be > 30 for Classic Frame without timestamp extensions.
        final static int MAX_STREAM_FRAME_BYTE_LENGTH = 30;

        // Defined this way to reduce new object creation
        private byte char1;

        // adapted from jmri.jmrix.can.adapters.gridconnect.GcTrafficController
        private String loadChars(DataInputStream istream) throws IOException {
            StringBuilder sb = new StringBuilder(MAX_STREAM_FRAME_BYTE_LENGTH);
            for (int i = 0; i < MAX_STREAM_FRAME_BYTE_LENGTH; i++) {
                char1 = readByteProtected(istream);
                if (i == 0) {
                    // skip until you find ':' standard Frame start, or
                    // | which will pass a self-receive message. 
                    while (char1 != ':' && char1 != '|' && !disposed) {
                        char1 = readByteProtected(istream);
                    }
                }
                sb.append((char)char1);
                // ; standard termination
                // ! one-shot Frame time senstitive
                if (char1 == ';' || char1 =='!') {
                    break; // end of CAN Frame character found
                }
            }
            return sb.toString();
        }

        // Defined this way to reduce new object creation
        @SuppressWarnings("MismatchedReadAndWriteOfArray")
        private final byte[] rcvBuffer = new byte[1];

        // adapted from jmri.jmrix.AbstractMRTrafficController
        private byte readByteProtected(DataInputStream istream) throws IOException {
            while (!disposed) { // loop will repeat until character found
                int nchars = istream.read(rcvBuffer, 0, 1);
                if (nchars == -1) {
                    // No more bytes can be read from the channel
                    throw new IOException("Connection not terminated normally");
                }
                if (nchars > 0) {
                    return rcvBuffer[0];
                }
            }
            return 0x00;
        }

        @Override
        public void forward(Memo m) {
            if ((! this.equals(m.source)) && output != null) {
                if (sendLineEndings) {
                    output.println(m.line);
                } else {
                    output.print(m.line);
                }
            }
        }
        
    }
    
    static public class Memo {
        public String line;
        public Forwarding source;
        
        Memo(String line, Forwarding source) {
            this.line = line;
            this.source = source;
        }
    }
    
    static public void main(String[] args) {
        Hub h = new Hub();
        
        h.start();
        
    }
}
