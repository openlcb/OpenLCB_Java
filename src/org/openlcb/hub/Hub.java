package org.openlcb.hub;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Simple multi-threaded OpenLCB hub implementation.
 * <P>
 * Multiple connections send lines terminated by newline,
 * each of which is echoed to all other connections.
 *
 * <P>
 * main() directly invokes an object of the class.
 * <p>
 * Current threading model does all the sending from a
 * a single thread.  If this is observed to back up &
 * halt all flow, individual transmit queues and threads
 * may be needed.
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision: 17977 $
 */

public class Hub {
    public final static int DEFAULT_PORT = 12021;
    final static int CAPACITY = 20;  // not too long, to reduce delay
    
    public Hub() {
        this(Hub.DEFAULT_PORT);
    }
    
    public Hub(int port) {
        this.port = port;
        // create array server thread
        Thread t = new Thread() {
            public void run() {
                while (true) {
                    try {
                        // as items arrive in queue, forward to every available connection
                        Memo m = queue.take();
                        for ( Forwarding e : threads) {
                            e.forward(m);
                        }
                    } catch (InterruptedException e) {
                        System.err.println("Hub: Interrupted in queue handling loop");
                        System.err.println(e);
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }
    
    ArrayBlockingQueue<Memo> queue = new ArrayBlockingQueue<Memo>(CAPACITY, true);  //fairness
    ArrayList<Forwarding> threads = new ArrayList<Forwarding>();
    int port;
    
    ServerSocket service;

    public void start() {
        try {
            service = new ServerSocket(port);
            while (true) {
                Socket clientSocket = service.accept();
                ReaderThread r = new ReaderThread(clientSocket);
                addForwarder(r);
                r.start();
                // not setting Daemon, so program will wait for thread to end before terminating
                notifyOwner("Connection started with "+getRemoteSocketAddress(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Hub: Exception in main loop");
            System.err.println(e);
        }
    }
    
    public int getPort() { return port; }
    public void addForwarder(Forwarding f) {
        threads.add(f);
    }
    
    public void notifyOwner(String line) {
        System.out.println(line);
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
            System.err.println(e);
        }
    }
    
    public interface Forwarding {
        public void forward(Memo m);
    }
    
    class ReaderThread extends Thread implements Forwarding {
    
        ReaderThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }
        
        Socket clientSocket;
        DataInputStream input;
        PrintStream output;
        
        public void run() {
            try {
                input = new DataInputStream(clientSocket.getInputStream());
                output = new PrintStream(clientSocket.getOutputStream());
        
                while (true) {
                    String line = input.readLine();
                    if (line == null) break;  // socket ended
                    queue.put(new Memo(line, this));
                }
     
            } catch (IOException e) {
                System.err.println("Hub: Error while handling input from "+getRemoteSocketAddress(clientSocket));
                System.err.println(e);
            } catch (InterruptedException e) {
                System.err.println("Hub: Interrupted while handling input from "+getRemoteSocketAddress(clientSocket));
                System.err.println(e);
            }
            threads.remove(this);
            notifyOwner("Connection ended with "+getRemoteSocketAddress(clientSocket));
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Hub: Error while closing socket at end of connection");
                System.err.println(e);
            }
        }
        
        public void forward(Memo m) {
            if (! this.equals(m.source)) {
                output.println(m.line); 
            }
        }
        
    }
    
    public class Memo {
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
