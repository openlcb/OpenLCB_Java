package org.openlcb.implementations.throttle;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.openlcb.*;

/**
 * Represents a TrainNode, a node that implements the Train protocol.
 *
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class TrainNode {

    public TrainNode(NodeID node) {
        this.node = node;
    }    
    NodeID node;
    
    public NodeID getNode() { return node; }
}
