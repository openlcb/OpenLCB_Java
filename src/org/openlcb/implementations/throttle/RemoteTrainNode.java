package org.openlcb.implementations.throttle;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.openlcb.*;

/**
 * Represents local view about a remote Train Node, a node that implements the Traction protocol.
 *
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class RemoteTrainNode {

    public RemoteTrainNode(NodeID node) {
        this.node = node;
    }    
    NodeID node;
    
    public NodeID getNodeId() { return node; }
}
