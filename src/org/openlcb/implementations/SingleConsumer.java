package org.openlcb.implementations;

import org.openlcb.*;

/**
 * Example of a NMRAnet node that consumes one Event.
 *<p>
 * The event doesn't cause much to happen, but e.g.
 * a {@link org.nmra.net.swing.ConsumerPane} can display
 * it.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class SingleConsumer extends SingleConsumerNode {

    public SingleConsumer(NodeID node, Connection connection, EventID eventID) {
        super(node, connection, eventID);
    }
    
    @Override
    public void initialize() {
        // put in place of _node_ initialization in superclass
    }
}
