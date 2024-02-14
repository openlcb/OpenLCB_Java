package org.openlcb.protocols;

import org.openlcb.Connection;
import org.openlcb.Message;
import org.openlcb.MessageDecoder;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.VerifiedNodeIDNumberMessage;
import org.openlcb.VerifyNodeIDNumberGlobalMessage;
import org.openlcb.VerifyNodeIDNumberAddressedMessage;

/**
 * Handler for verify node ID requests to the local node.
 * <p>
 * Created by bracz on 12/28/15.
 */
public class VerifyNodeIdHandler extends MessageDecoder {
    private final OlcbInterface iface;
    private final NodeID id;

    /**
     * Instantiates the Verofy Node ID handler.
     *
     * @param id    is the Node ID on behalf which to reply to messages.
     * @param iface is where to send replies to, and where to listen for incoming messages.
     */
    public VerifyNodeIdHandler(NodeID id, OlcbInterface iface) {
        this.iface = iface;
        this.id = id;
        iface.registerMessageListener(this);
    }

    @Override
    public void handleVerifyNodeIDNumberGlobal(VerifyNodeIDNumberGlobalMessage msg, Connection sender) {
        /* This is the Verify Node ID number "global" message.
        *
        */

        // Only reply if requesting all nodes or one node where the ID is this specific node.
        if (msg.getContent() == null || msg.getContent().equals(id)) {
            Message omsg = new VerifiedNodeIDNumberMessage(id);
            iface.getOutputConnection().put(omsg, this);
        }
    }

    @Override
    public void handleVerifyNodeIDNumberAddressed(VerifyNodeIDNumberAddressedMessage msg, Connection sender) {
        /* This is the Verify Node ID number "addressed" message.
        *
        */
        // Only reply if addressed to this node
        if (msg.getDestNodeID().equals(id)) {
            Message omsg = new VerifiedNodeIDNumberMessage(id);
            iface.getOutputConnection().put(omsg, this);
        }
    }
}
