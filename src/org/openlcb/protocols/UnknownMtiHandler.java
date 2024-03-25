package org.openlcb.protocols;

import org.openlcb.Connection;
import org.openlcb.Message;
import org.openlcb.MessageDecoder;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.UnknownMtiMessage;
import org.openlcb.OptionalIntRejectedMessage;

/**
 * Handler for unknown MTI requests to the local node.
 * <p>
 * Created by Bob Jacobsen 2/2024 from VerifyNodeIdHandler
 */
public class UnknownMtiHandler extends MessageDecoder {
    private final OlcbInterface iface;
    private final NodeID id;

    /**
     * Instantiates the handler.
     *
     * @param id    is the Node ID on behalf which to reply to messages.
     * @param iface is where to send replies to, and where to listen for incoming messages.
     */
    public UnknownMtiHandler(NodeID id, OlcbInterface iface) {
        this.iface = iface;
        this.id = id;
        iface.registerMessageListener(this);
    }

    @Override
    public void handleUnknownMTI(UnknownMtiMessage msg, Connection sender) {
        /* 
        * This is an unknown MTI message that could be to anybody
        */

        // Only reply if addressed to this node
        if (msg.getDestNodeID() == this.id) {
            int mti = msg.getOriginalMTI();
            int code = 0x1040; // See message std 3.5.5; permanent error, not implemented
            Message omsg = new OptionalIntRejectedMessage(this.id, msg.getSourceNodeID(), mti, code);
            iface.getOutputConnection().put(omsg, this);
        }
    }
}
