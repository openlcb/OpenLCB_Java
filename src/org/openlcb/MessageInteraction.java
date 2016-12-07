package org.openlcb;

/**
 * Created by bracz on 12/7/16.
 */

public abstract class MessageInteraction extends Interaction {
    private final AddressedMessage msg;

    MessageInteraction(AddressedMessage msg) {
        this.msg = msg;
    }

    @Override
    public void sendRequest(Connection downstream) {
        downstream.put(msg, null);
    }

    @Override
    public NodeID dstNode() {
        return msg.getDestNodeID();
    }
}
