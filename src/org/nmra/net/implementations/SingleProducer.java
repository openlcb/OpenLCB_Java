package org.nmra.net.implementations;

import org.nmra.net.*;

/**
 * Example of a producer component which can be grouped within 
 * some larger Node to function.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class SingleProducer extends SingleProducerNode {

    public SingleProducer(NodeID node, Connection connection, EventID eventID) {
        super(node, connection, eventID);
    }

    @Override
    public void initialize() {
        // put in place of _node_ initialization in superclass
    }
}
