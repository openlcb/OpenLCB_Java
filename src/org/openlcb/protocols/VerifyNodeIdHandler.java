package org.openlcb.protocols;

import org.openlcb.Connection;
import org.openlcb.Message;
import org.openlcb.MessageDecoder;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.VerifiedNodeIDNumberMessage;
import org.openlcb.VerifyNodeIDNumberMessage;

/**
 * Created by bracz on 12/28/15.
 */
public class VerifyNodeIdHandler extends MessageDecoder {
    private final OlcbInterface iface;
    private final NodeID id;

    public VerifyNodeIdHandler(NodeID id, OlcbInterface iface) {
        this.iface = iface;
        this.id = id;
        iface.registerMessageListener(this);
    }

    @Override
    public void handleVerifyNodeIDNumber(VerifyNodeIDNumberMessage msg, Connection sender) {
        /// @TODO(balazs.racz) this needs to distinguish global and addressed versions of the
        /// message, as well as compare the data content, if any, with the actual node id.
        Message omsg = new VerifiedNodeIDNumberMessage(id);
        iface.getOutputConnection().put(omsg, this);
    }
}
