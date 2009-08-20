package org.nmra.net;

/**
 * Default base for NRMAnet node implementations.
 * <p>
 * Provides a Connection for incoming Messages.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class Node extends MessageDecoder implements Connection {
    public Node(NodeID nodeID) {
        this.nodeID = nodeID;
    }
    
    // generally, users must provide nodeID in ctor
    // but for testing purposes provide a null ctor
    protected Node() {} 
    
    protected NodeID nodeID;
    
}
