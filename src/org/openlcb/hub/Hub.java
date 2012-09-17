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
    public final static int PORT = 12345;
    final static int CAPACITY = 20;  // not too long, to reduce delay
    
    public Hub() {
        // create array server thread
        Thread t = new Thread() {
            public void run() {
                while (true) {
                    try {
                        Memo m = queue.take();
                        for ( ReaderThread e : threads) {
                            e.forward(m);
                        }
                    } catch (InterruptedException e) {
                        System.err.println(e);
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }
    
    ArrayBlockingQueue<Memo> queue = new ArrayBlockingQueue<Memo>(CAPACITY, true);  //fairness
    ArrayList<ReaderThread> threads = new ArrayList<ReaderThread>();
    
    ServerSocket service;

    public void start() {
        try {
            service = new ServerSocket(PORT);
            while (true) {
                Socket clientSocket = service.accept();
                ReaderThread r = new ReaderThread(clientSocket);
                threads.add(r);
                r.start();
                System.out.println("Connection started with "+getRemoteSocketAddress(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("main loop failed "+e);
        }
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
    
    class ReaderThread extends Thread {
    
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
                System.err.println(e);
            } catch (InterruptedException e) {
                System.err.println(e);
            }
            threads.remove(this);
            System.out.println("Connection ended with "+getRemoteSocketAddress(clientSocket));
        }
        
        void forward(Memo m) {
            if (m.source != this) {
                output.println(m.line); 
            }
        }
        
    }
    
    class Memo {
        String line;
        ReaderThread source;
        
        Memo(String line, ReaderThread source) {
            this.line = line;
            this.source = source;
        }
    }
    
    static public void main(String[] args) {
        Hub h = new Hub();
        
        h.start();
        
    }
}
