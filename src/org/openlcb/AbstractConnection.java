package org.openlcb;

/**
 * Partial implementation of Connection with null startup behavior
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
abstract public class AbstractConnection implements Connection {

    /**
     * Put a message to this connection.
     * @param sender Node that is sending the message, used
     *        for tracking, logging, etc.  
     *        (This models a two-ended connection to whatever
     *        communications link is used)
     */
    abstract public void put(Message msg, Connection sender);
    
    /**
     * Default registration behavior is an immediate call-back
     * with news that the connection is up.
     */
    public void registerStartNotification(ConnectionListener c) {
        c.connectionActive(this);
    }

}
