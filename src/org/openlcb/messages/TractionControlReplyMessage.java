package org.openlcb.messages;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.logging.Logger;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.openlcb.AddressedPayloadMessage;
import org.openlcb.Connection;
import org.openlcb.MessageDecoder;
import org.openlcb.MessageTypeIdentifier;
import org.openlcb.NodeID;
import org.openlcb.Utilities;
import org.openlcb.implementations.throttle.Float16;
import static org.openlcb.messages.TractionControlRequestMessage.speedToDebugString;
import static org.openlcb.messages.TractionControlRequestMessage.consistFlagsToDebugString;

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
    public final static int GET_SPEED_FLAG_ESTOP = 0x01;

    public final static byte CMD_GET_FN = TractionControlRequestMessage.CMD_GET_FN;

    public final static byte CMD_CONTROLLER = TractionControlRequestMessage.CMD_CONTROLLER;
    public final static byte SUBCMD_CONTROLLER_ASSIGN = TractionControlRequestMessage.SUBCMD_CONTROLLER_ASSIGN;
    public final static byte SUBCMD_CONTROLLER_QUERY = TractionControlRequestMessage.SUBCMD_CONTROLLER_QUERY;
    public final static byte SUBCMD_CONTROLLER_CHANGE = TractionControlRequestMessage.SUBCMD_CONTROLLER_CHANGE;

    public final static byte CMD_MGMT = TractionControlRequestMessage.CMD_MGMT;
    public final static byte SUBCMD_MGMT_RESERVE = TractionControlRequestMessage.SUBCMD_MGMT_RESERVE;
    public final static byte SUBCMD_MGMT_HEARTBEAT = 0x03;

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

    /**
     * Extract the consisted train's node ID from the consist attach/detach response.
     * @return the consisted train's node ID.
     */
    @Nullable
    public NodeID getConsistAttachNodeID() {
        if (payload.length < 8) return null;
        byte[] id = new byte[6];
        System.arraycopy(payload, 2, id, 0, 6);
        return new NodeID(id);
    }

    /**
     * Extract the consistattach/detach response code
     * @return a 16-bit openlcb error code, 0 for success.
     */
    public int getConsistAttachCode() {
        return Utilities.NetworkToHostUint16(payload, 8);
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

    @Override
    public String toString() {
        StringBuilder p = new StringBuilder(getSourceNodeID().toString());
        p.append(" - ");
        p.append(getDestNodeID());
        p.append(" ");
        p.append(getEMTI().toString());
        p.append(" ");
        try {
            switch (getCmd()) {
                case CMD_GET_SPEED: {
                    p.append("speed reply ");
                    p.append(speedToDebugString(getSetSpeed()));
                    if ((payload.length >= 4) && ((payload[3] & GET_SPEED_FLAG_ESTOP) != 0)) {
                        p.append(" estop");
                    }
                    if (payload.length >= 6) {
                        p.append(" commanded speed ");
                        p.append(speedToDebugString(getCommandedSpeed()));
                    }
                    if (payload.length >= 8) {
                        p.append(" actual speed ");
                        p.append(speedToDebugString(getActualSpeed()));
                    }
                    break;
                }
                case CMD_GET_FN: {
                    int fn = Utilities.NetworkToHostUint24(payload, 1);
                    int val = Utilities.NetworkToHostUint16(payload, 4);
                    p.append(String.format("fn %d is %d", fn, val));
                    break;
                }
                case CMD_CONTROLLER: {
                    switch(getSubCmd()) {
                        case SUBCMD_CONTROLLER_ASSIGN: {
                            p.append("controller assign");
                            int flags = Utilities.NetworkToHostUint8(payload, 2);
                            if(flags == 0) {
                                p.append(" OK");
                            } else {
                                p.append(String.format(" fail 0x%02x", flags));
                            }
                            break;
                        }
                        case SUBCMD_CONTROLLER_QUERY: {
                            long nid = Utilities.NetworkToHostUint48(payload, 3);
                            p.append("controller is ");
                            p.append(new NodeID(nid).toString());
                            int flags = Utilities.NetworkToHostUint8(payload, 2);
                            if(flags != 0) {
                                p.append(String.format(" flags 0x%02x", flags));
                            }
                            break;
                        }
                        case SUBCMD_CONTROLLER_CHANGE: {
                            p.append("change controller reply");
                            int flags = Utilities.NetworkToHostUint8(payload, 2);
                            if (flags == 0) {
                                p.append(" OK");
                            } else {
                                p.append(String.format(" reject 0x%02x", flags));
                            }
                            break;
                        }
                        default:
                            return super.toString();
                    }
                    break;
                }
                case CMD_CONSIST: {
                    switch (getSubCmd()) {
                        case SUBCMD_CONSIST_ATTACH: {
                            long nid = Utilities.NetworkToHostUint48(payload, 2);
                            p.append("listener attach ");
                            p.append(new NodeID(nid).toString());
                            int code = Utilities.NetworkToHostUint16(payload, 8);
                            p.append(String.format(" result 0x%04x", code));
                            break;
                        }
                        case SUBCMD_CONSIST_DETACH: {
                            long nid = Utilities.NetworkToHostUint48(payload, 2);
                            p.append("listener detach ");
                            p.append(new NodeID(nid).toString());
                            int code = Utilities.NetworkToHostUint16(payload, 8);
                            p.append(String.format(" result 0x%04x", code));
                            break;
                        }
                        case SUBCMD_CONSIST_QUERY: {
                            p.append("listener is");
                            int count = Utilities.NetworkToHostUint8(payload, 2);
                            p.append(String.format(" count %d", payload[2] & 0xff));
                            if (payload.length >= 4) {
                                p.append(String.format(" index %d", payload[3] & 0xff));
                            }
                            if ((payload.length >= 5) && (payload[4] != 0)) {
                                p.append(" flags ");
                                p.append(consistFlagsToDebugString(payload[4]));
                            }
                            if (payload.length >= 11) {
                                p.append(" is ");
                                p.append(new NodeID(Utilities.NetworkToHostUint48(payload, 5)).toString());
                            }
                            break;
                        }
                        default:
                            return super.toString();
                    }
                    break;
                }
                case CMD_MGMT: {
                    switch (getSubCmd()) {
                        case SUBCMD_MGMT_RESERVE: {
                            p.append("reserve reply");
                            if (payload[2] == 0) {
                                p.append(" OK");
                            } else {
                                p.append(String.format(" error 0x%02x", payload[2] & 0xff));
                            }
                            break;
                        }
                        case SUBCMD_MGMT_HEARTBEAT: {
                            p.append("heartbeat request");
                            if (payload.length >= 3) {
                                p.append(String.format(" in %d seconds", payload[2] & 0xff));
                            }
                            break;
                        }
                        default:
                            return super.toString();
                    }
                    break;
                }
                default:
                    return super.toString();
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            return super.toString();
        }
        return p.toString();
    }
}
