package org.openlcb;

/**
 * Base for OpenLCB gateway implementations.
 *<p>
 * Provides two connections, called "East" and "West"
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class Gateway extends MessageDecoder {
    public Gateway() {
    }
    
    /**
     * Provide a connection object for use by
     * the East node.
     */
    public Connection getEastConnection() {
        eastInputConnection = new AbstractConnection() {
            public void put(Message msg, Connection sender) {
                sendMessageToWest(msg, sender);
            }
        };
        return eastInputConnection;
    }

    // forward from east to west
    protected void sendMessageToWest(Message msg, Connection sender) {
        if (westOutputConnection == null)
            throw new AssertionError("west was null when message sent");
        westOutputConnection.put(msg, westInputConnection);
    }
    
    public void registerEast(Connection c) {
        if (eastOutputConnection != null)
            throw new AssertionError("east already registered");
        eastOutputConnection = c;
    }
 
    protected Connection eastOutputConnection;   // what I send to on east side
    protected Connection eastInputConnection; // to me, I show as sender on east side
    
    /**
     * Provide a connection object for use by
     * the West node.
     */
    public Connection getWestConnection() {
        westInputConnection = new AbstractConnection() {
            public void put(Message msg, Connection sender) {
                sendMessageToEast(msg, sender);
            }
        };
        return westInputConnection;
    }

    // forward from west to east
    protected void sendMessageToEast(Message msg, Connection sender) {
        if (eastOutputConnection == null)
            throw new AssertionError("east was null when message sent");
        eastOutputConnection.put(msg, eastInputConnection);
    }
    
    public void registerWest(Connection c) {
        if (westOutputConnection != null)
            throw new AssertionError("west already registered");
        westOutputConnection = c;
    }

    protected Connection westOutputConnection;   // what I send to on west side
    protected Connection westInputConnection; // to me, I show as sender on west side
    
}