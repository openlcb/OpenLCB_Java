package org.openlcb;

/**
 * Interface for receiving NMRAnet messages.
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

}
