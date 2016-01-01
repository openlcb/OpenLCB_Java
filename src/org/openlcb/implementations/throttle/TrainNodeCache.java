package org.openlcb.implementations.throttle;

import org.openlcb.*;

/**
 * Maintain a cache of Train objects on OpenLCB network
 *
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class TrainNodeCache extends AbstractNodeCache<RemoteTrainNode> {

    private final OlcbInterface iface;

    public TrainNodeCache(OlcbInterface iface) {
        super(CommonIdentifiers.IS_TRAIN);
        this.iface = iface;
    }
    
    protected RemoteTrainNode newObject(NodeID id) {
        return new RemoteTrainNode(id);
    }

}
