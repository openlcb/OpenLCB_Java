package org.openlcb;

/**
 * Manages configuration interactions with a node
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class ConfigurationPortal {

    /**
     * @param node NodeID of node being configured
     * @param connection Connection for sending messages to node
     */
    public ConfigurationPortal(NodeID node, Connection connection) {
        if (connection == null) 
            throw new IllegalArgumentException("Connection must be provided");
        this.connection = connection;
    }
    
    protected Connection connection;

}
