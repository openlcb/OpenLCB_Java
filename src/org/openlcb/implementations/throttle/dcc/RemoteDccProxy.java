package org.openlcb.implementations.throttle.dcc;

import org.openlcb.NodeID;

/**
 * Created by bracz on 1/17/16.
 */
public class RemoteDccProxy {
    NodeID node;

    RemoteDccProxy(NodeID node) {
        this.node = node;
    }

    public NodeID getNodeId() {
        return node;
    }
}
