package org.nmra.net.implementations;

import org.nmra.net.*;

/**
 * Example of connecting a number of nodes.
 * <p>
 * Messages that the nodes send are sent to all other nodes, 
 * but not the originating node.
 *
 *<p>
 * The sequence is
 *<ul>
 *<li>ScatterGather sg = new ScatterGather();
 *<li>Connect c = sg.getConnection();
 *<li>Node n = new Node(c);
 *<li>sg.register(n);
 *</ul>
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class ScatterGather {
    /**
     * Provide a connection object for use by
     * a Node
     */
    public Connection getConnection() {
        SingleConnection c = new SingleConnection();
        issuedConnections.add(c);
        return c;
    }

    class SingleConnection implements Connection {
        public void put(Message msg, Connection sender) {
            // forward to all but the sender
            boolean match = false;
            for (Connection e : registeredConnections) {
                if (e.equals(sender))
                    match = true;
                else
                    e.put(msg, sender);
            }
            if (!match)
                throw new AssertionError("Sender not registered");
        }
    }

    java.util.ArrayList<SingleConnection> issuedConnections 
            = new java.util.ArrayList<SingleConnection>();
        
    java.util.ArrayList<Connection> registeredConnections 
            = new java.util.ArrayList<Connection>();
        
    public void register(Connection c) {
        registeredConnections.add(c);
    }
}
