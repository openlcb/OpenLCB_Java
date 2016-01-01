package org.openlcb.implementations.throttle;

import org.openlcb.CommonIdentifiers;
import org.openlcb.Connection;
import org.openlcb.IdentifyProducersMessage;
import org.openlcb.MimicNodeStore;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;

/**
 * Maintain a cache of Train objects on OpenLCB network
 *
 * @author Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class TrainNodeCache extends AbstractNodeCache<RemoteTrainNode> {

    private final OlcbInterface iface;

    public TrainNodeCache(OlcbInterface _iface) {
        super(CommonIdentifiers.IS_TRAIN);
        iface = _iface;
        iface.registerMessageListener(this);
        // Sends a query to all train nodes on the network.
        iface.getOutputConnection().registerStartNotification(new ConnectionListener() {
            @Override
            public void connectionActive(Connection c) {
                c.put(new IdentifyProducersMessage(iface.getNodeId(), CommonIdentifiers.IS_TRAIN)
                        , TrainNodeCache.this);
            }
        });
    }

    protected RemoteTrainNode newObject(NodeID id) {
        MimicNodeStore.NodeMemo memo = iface.getNodeStore().findNode(id);
        if (memo != null) {
            // Instantiates the fetching of SNIP and PIP data.
            memo.getSimpleNodeIdent();
            memo.getProtocolIdentification();
        }
        return new RemoteTrainNode(id);
    }

}
