package org.openlcb.implementations.throttle.dcc;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.openlcb.*;

import org.openlcb.implementations.throttle.AbstractNodeCache;
import org.openlcb.implementations.throttle.TrainNode;

/**
 * Maintain a cache of Train objects on OpenLCB network
 *
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class DccProxyCache extends AbstractNodeCache<TrainNode> {

    public DccProxyCache() {
        super(new EventID("01.01.00.00.00.00.04.01"));
    }

    protected TrainNode newObject(NodeID id) {
        return new TrainNode(id);
    }
}
