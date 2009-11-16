package org.openlcb;

/**
 * Base for NMRAnet nodes that have only a single connection to the
 * outside world.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class SingleLinkNode extends Node {

    public SingleLinkNode(NodeID node, Connection connection) {
        super(node);
        if (connection == null) 
            throw new IllegalArgumentException("Connection must be provided");
        this.connection = connection;
    }
    
    protected Connection connection;

    public void put(Message msg, Node node) {
    }
    
    /**
     * Initialize this node and put it in operation
     */
    public void initialize() {
        connection.put(new InitializationCompleteMessage(nodeID), this);
    }
}
