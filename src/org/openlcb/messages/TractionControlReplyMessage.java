package org.openlcb.messages;

import java.util.logging.Logger;
import javax.annotation.Nullable;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.openlcb.AddressedPayloadMessage;
import org.openlcb.Connection;
import org.openlcb.MessageDecoder;
import org.openlcb.MessageTypeIdentifier;
import org.openlcb.NodeID;
import org.openlcb.implementations.throttle.Float16;

/**
 * Traction Control Reply message implementation.
 * <p>
 * Created by bracz on 12/29/15.
 */
@Immutable
@ThreadSafe
public class TractionControlReplyMessage extends AddressedPayloadMessage {
    private final static Logger logger = Logger.getLogger(TractionControlReplyMessage.class.getName());
    public final static byte CMD_GET_SPEED = TractionControlRequestMessage.CMD_GET_SPEED;
    public final static byte CMD_GET_FN = TractionControlRequestMessage.CMD_GET_FN;

    public final static byte CMD_CONTROLLER = TractionControlRequestMessage.CMD_CONTROLLER;
    public final static byte SUBCMD_CONTROLLER_ASSIGN = TractionControlRequestMessage.SUBCMD_CONTROLLER_ASSIGN;
    public final static byte SUBCMD_CONTROLLER_QUERY = TractionControlRequestMessage.SUBCMD_CONTROLLER_QUERY;
    public final static byte SUBCMD_CONTROLLER_CHANGE = TractionControlRequestMessage.SUBCMD_CONTROLLER_CHANGE;

    public final static byte CMD_MGMT = TractionControlRequestMessage.CMD_MGMT;
    public final static byte SUBCMD_MGMT_RESERVE = TractionControlRequestMessage.SUBCMD_MGMT_RESERVE;

    public final static byte CMD_CONSIST = TractionControlRequestMessage.CMD_CONSIST;
    public final static byte SUBCMD_CONSIST_ATTACH = TractionControlRequestMessage.SUBCMD_CONSIST_ATTACH;
    public final static byte SUBCMD_CONSIST_DETACH = TractionControlRequestMessage
            .SUBCMD_CONSIST_DETACH;
    public final static byte SUBCMD_CONSIST_QUERY = TractionControlRequestMessage
            .SUBCMD_CONSIST_QUERY;


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
        Float16 f = new Float16(payload[1], payload[2]);
        logger.finest("Incoming float16 " + payload[1] + "." + payload[2] + "= " + f.getFloat());
        return f;
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

    /** @return the length of the consist list.
     * Valid only for consist query reply message
     */
    public int getConsistLength() {
        byte uintval = payload[2];
        int retval = uintval < 0 ? uintval + 256 : uintval;
        return retval;
    }

    /** @return the index of the returned node in the consist list, or -1 if there is no consist
     * entry in the response.
     * Valid only for consist query reply message
     */
    public int getConsistIndex() {
        if (payload.length < 4) return -1;
        byte uintval = payload[3];
        int retval = uintval < 0 ? uintval + 256 : uintval;
        return retval;
    }

    /**
     * Extract the consisted train's node ID from the consist list query response.
     * @return the consisted train's node ID from the consist query response, or null if the
     * response did not contain a node ID.
     */
    @Nullable
    public NodeID getConsistQueryNodeID() {
        if (payload.length < 11) return null;
        byte[] id = new byte[6];
        System.arraycopy(payload, 5, id, 0, 6);
        return new NodeID(id);
    }

    /**
     * Extract the consist entry's flag byte. Call it only if the consist index is nonnegative.
     * @return consist flags
     */
    public int getConsistQueryFlags() {
        if (payload.length < 11) return 0;
        return payload[4];
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
