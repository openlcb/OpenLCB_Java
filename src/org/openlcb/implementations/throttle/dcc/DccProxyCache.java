package org.openlcb.implementations.throttle.dcc;

import org.openlcb.*;

import org.openlcb.implementations.throttle.AbstractNodeCache;
import org.openlcb.implementations.throttle.RemoteTrainNode;

/**
 * Maintain a cache of Train objects on OpenLCB network
 *
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class DccProxyCache extends AbstractNodeCache<RemoteTrainNode> {

    public DccProxyCache() {
        super(new EventID("01.01.00.00.00.00.04.01"));
    }

    protected RemoteTrainNode newObject(NodeID id) {
        return new RemoteTrainNode(id);
    }
}
