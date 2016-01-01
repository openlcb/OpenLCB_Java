package org.openlcb.messages;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import org.openlcb.AddressedMessage;
import org.openlcb.AddressedPayloadMessage;
import org.openlcb.Connection;
import org.openlcb.MessageDecoder;
import org.openlcb.MessageTypeIdentifier;
import org.openlcb.NodeID;
import org.openlcb.implementations.throttle.Float16;

/**
 * Traction Control Reply message implementation.
 * <p/>
 * Created by bracz on 12/29/15.
 */
@Immutable
@ThreadSafe
public class TractionControlReplyMessage extends AddressedPayloadMessage {
    public final static byte CMD_GET_SPEED = TractionControlRequestMessage.CMD_GET_SPEED;
    public final static byte CMD_GET_FN = TractionControlRequestMessage.CMD_GET_FN;

    public final static byte CMD_CONTROLLER = TractionControlRequestMessage.CMD_CONTROLLER;
    public final static byte SUBCMD_CONTROLLER_ASSIGN = TractionControlRequestMessage.SUBCMD_CONTROLLER_ASSIGN;
    public final static byte SUBCMD_CONTROLLER_QUERY = TractionControlRequestMessage.SUBCMD_CONTROLLER_QUERY;
    public final static byte SUBCMD_CONTROLLER_CHANGE = TractionControlRequestMessage.SUBCMD_CONTROLLER_CHANGE;

    public final static byte CMD_MGMT = TractionControlRequestMessage.CMD_MGMT;
    public final static byte SUBCMD_MGMT_RESERVE = TractionControlRequestMessage.SUBCMD_MGMT_RESERVE;


    public TractionControlReplyMessage(NodeID source, NodeID dest, byte[] payload) {
        super(source, dest, payload);
    }

    public byte getCmd() throws ArrayIndexOutOfBoundsException {
        return payload[0];
    }

    // Valid for messages that contain a subcommand.
    public byte getSubCmd() throws ArrayIndexOutOfBoundsException {
        return payload[1];
    }

    public byte getAssignControllerReply() throws ArrayIndexOutOfBoundsException {
        return payload[2];
    }

    public NodeID getCurrentControllerReply() throws ArrayIndexOutOfBoundsException {
        if (payload.length < 8) throw new ArrayIndexOutOfBoundsException();
        byte[] nid = new byte[6];
        System.arraycopy(payload, 3, nid, 0, 6);
        return new NodeID(nid);
    }

    public byte getReserveReply() throws ArrayIndexOutOfBoundsException {
        return payload[2];
    }

    // Valid only for set speed reply message
    public Float16 getSetSpeed() throws ArrayIndexOutOfBoundsException {
        return new Float16(payload[1], payload[2]);
    }

    // Valid only for set speed reply message
    public Float16 getCommandedSpeed() throws ArrayIndexOutOfBoundsException {
        return new Float16(payload[4], payload[5]);
    }

    // Valid only for set speed reply message
    public Float16 getActualSpeed() throws ArrayIndexOutOfBoundsException {
        return new Float16(payload[6], payload[7]);
    }

    // Valid only for get function response
    public int getFnNumber() {
        int retval = 0;
        retval = payload[1] & 0xff;
        retval <<= 8;
        retval |= (payload[2] & 0xff);
        retval <<= 8;
        retval |= (payload[3] & 0xff);
        return retval;
    }

    // Valid only for get function response
    public int getFnVal() {
        int retval = 0;
        retval = payload[4] & 0xff;
        retval <<= 8;
        retval |= (payload[5] & 0xff);
        return retval;
    }

    @Override
    public void applyTo(MessageDecoder decoder, Connection sender) {
        decoder.handleTractionControlReply(this, sender);
    }

    @Override
    public MessageTypeIdentifier getEMTI() {
        return MessageTypeIdentifier.TractionControlReply;
    }
}
