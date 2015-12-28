package org.openlcb;

/**
 * Interface for receiving OpenLCB messages.
 * <p>
 * Generally, nodes send messages by delivering them to Connections.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public interface Connection {

    /**
     * Put a message to this connection.
     * @param sender Node that is sending the message, used
     *        for tracking, logging, etc.  
     *        (This models a two-ended connection to whatever
     *        communications link is used)
     */
    public void put(Message msg, Connection sender);
    
    /**
     * Register to be informed when this connection is ready
     * to accept messages.  Unless you know via some other
     * mechanism that the connection is ready to go, you
     * must register a listener and get the call-back before
     * sending messages through the connection.
     */
    public void registerStartNotification(ConnectionListener c);

    /** 
     * Internal listener class definition
     */
    public abstract class ConnectionListener {
        public abstract void connectionActive(Connection c);
    }
}
