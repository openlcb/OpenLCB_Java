package org.openlcb.messages;

import java.util.logging.Logger;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.openlcb.AddressedMessage;
import org.openlcb.AddressedPayloadMessage;
import org.openlcb.Connection;
import org.openlcb.MessageDecoder;
import org.openlcb.MessageTypeIdentifier;
import org.openlcb.NodeID;
import org.openlcb.Utilities;
import org.openlcb.implementations.throttle.Float16;

/**
 * Traction Control Request message implementation.
 * <p>
 * Created by bracz on 12/29/15.
 */
@Immutable
@ThreadSafe
public class TractionControlRequestMessage extends AddressedPayloadMessage {
    private final static Logger logger = Logger.getLogger(TractionControlRequestMessage.class.getName());

    public final static byte CMD_SET_SPEED = 0x00;
    public final static byte CMD_SET_FN = 0x01;
    public final static byte CMD_ESTOP = 0x02;
    public final static byte CMD_GET_SPEED = 0x10;
    public final static byte CMD_GET_FN = 0x11;

    public final static byte CMD_CONTROLLER = 0x20;
    public final static byte SUBCMD_CONTROLLER_ASSIGN = 1;
    public final static byte SUBCMD_CONTROLLER_RELEASE = 2;
    public final static byte SUBCMD_CONTROLLER_QUERY = 3;
    public final static byte SUBCMD_CONTROLLER_CHANGE = 4;

    public final static byte CMD_CONSIST = 0x30;
    public final static byte SUBCMD_CONSIST_ATTACH = 1;
    public final static byte SUBCMD_CONSIST_DETACH = 2;
    public final static byte SUBCMD_CONSIST_QUERY = 3;
    public final static int CONSIST_FLAG_ALIAS = 0x01;
    public final static int CONSIST_FLAG_REVERSE = 0x02;
    public final static int CONSIST_FLAG_FN0 = 0x04;
    public final static int CONSIST_FLAG_FNN = 0x08;
    public final static int CONSIST_FLAG_HIDE = 0x80;

    public final static int CONSIST_FLAG_LISTENERS =
            CONSIST_FLAG_HIDE | CONSIST_FLAG_FN0 | CONSIST_FLAG_FNN;

    public final static byte CMD_MGMT = 0x40;
    public final static byte SUBCMD_MGMT_RESERVE = 1;
    public final static byte SUBCMD_MGMT_RELEASE = 2;
    public final static byte SUBCMD_MGMT_NOOP = 3;

    public final static byte CMD_LISTENER_FORWARD = (byte)0x80;

    /// 1 scale mph in meters per second for the getspeed/setspeed commands
    public final static double MPH = 0.44704;
    /// 1 scale km/h in meters per second for the getspeed/setspeed commands
    public final static double KMH = 0.277778;

    public TractionControlRequestMessage(NodeID source, NodeID dest, byte[] payload) {
        super(source, dest, payload);
        this.payload = payload.clone();
    }

    public static TractionControlRequestMessage createSetSpeed(NodeID source, NodeID dest, boolean
            isForward, double speed) {
        if (isForward) {
            if (speed < 0) speed = -speed;
        } else {
            if (speed >= 0) speed = -speed;
        }
        Float16 sp = new Float16(speed, isForward);
        logger.finest("Traction set speed: isFwd=" + isForward + " speed " + speed + " set speed" +
                " " + sp.getByte1() + "." + sp.getByte2());

        byte[] payload = new byte[]{CMD_SET_SPEED, sp.getByte1(), sp.getByte2()};
        return new TractionControlRequestMessage(source, dest, payload);
    }

    public static TractionControlRequestMessage createSetEstop(NodeID source, NodeID dest) {
        return new TractionControlRequestMessage(source, dest, new byte[]{CMD_ESTOP});
    }

    public static TractionControlRequestMessage createGetSpeed(NodeID source, NodeID dest) {
        return new TractionControlRequestMessage(source, dest, new byte[]{CMD_GET_SPEED});
    }

    public static TractionControlRequestMessage createSetFn(NodeID source, NodeID dest, int fn, int val) {
        byte[] payload = new byte[]{CMD_SET_FN, (byte) ((fn >> 16) & 0xff), (byte) ((fn >> 8) &
                0xff), (byte) (fn & 0xff), (byte) ((val >> 8) & 0xff), (byte) (val & 0xff)};
        return new TractionControlRequestMessage(source, dest, payload);
    }

    public static TractionControlRequestMessage createGetFn(NodeID source, NodeID dest, int fn) {
        byte[] payload = new byte[]{CMD_GET_FN, (byte) ((fn >> 16) & 0xff), (byte) ((fn >> 8) &
                0xff), (byte) (fn & 0xff)};
        return new TractionControlRequestMessage(source, dest, payload);
    }

    // Assigns the 'source' as controller id.
    public static TractionControlRequestMessage createAssignController(NodeID source, NodeID dest) {
        byte[] payload = new byte[]{CMD_CONTROLLER, SUBCMD_CONTROLLER_ASSIGN, 0, 1, 2, 3, 4, 5, 6};
        System.arraycopy(source.getContents(), 0, payload, 3, 6);
        return new TractionControlRequestMessage(source, dest, payload);
    }

    // Releases the controller, filling source as the controlling id.
    public static TractionControlRequestMessage createReleaseController(NodeID source, NodeID dest) {
        byte[] payload = new byte[]{CMD_CONTROLLER, SUBCMD_CONTROLLER_RELEASE, 0, 1, 2, 3, 4, 5, 6};
        System.arraycopy(source.getContents(), 0, payload, 3, 6);
        return new TractionControlRequestMessage(source, dest, payload);
    }

    public static TractionControlRequestMessage createQueryController(NodeID source, NodeID dest) {
        byte[] payload = new byte[]{CMD_CONTROLLER, SUBCMD_CONTROLLER_QUERY};
        return new TractionControlRequestMessage(source, dest, payload);
    }

    public static TractionControlRequestMessage createControllerChangeNotify(NodeID source, NodeID dest,
                                                                      NodeID newController) {
        byte[] payload = new byte[]{CMD_CONTROLLER, SUBCMD_CONTROLLER_CHANGE, 0, 1, 2, 3, 4, 5, 6};
        System.arraycopy(newController.getContents(), 0, payload, 3, 6);
        return new TractionControlRequestMessage(source, dest, payload);
    }

    public static TractionControlRequestMessage createConsistAttach(NodeID source, NodeID dest,
                                                                    NodeID consistEntry, int
                                                                            flags) {
        byte[] payload = new byte[]{CMD_CONSIST, SUBCMD_CONSIST_ATTACH, (byte)(flags & 0xff), 1, 2,
                3, 4, 5, 6};
        System.arraycopy(consistEntry.getContents(), 0, payload, 3, 6);
        return new TractionControlRequestMessage(source, dest, payload);
    }

    public static TractionControlRequestMessage createConsistDetach(NodeID source, NodeID dest,
                                                                    NodeID consistEntry) {
        byte[] payload = new byte[]{CMD_CONSIST, SUBCMD_CONSIST_DETACH, 0, 1, 2, 3, 4, 5, 6};
        System.arraycopy(consistEntry.getContents(), 0, payload, 3, 6);
        return new TractionControlRequestMessage(source, dest, payload);
    }

    /// Queries a specific index in the consist list.
    public static TractionControlRequestMessage createConsistIndexQuery(NodeID source, NodeID dest,
                                                                   int index) {
        byte[] payload = new byte[]{CMD_CONSIST, SUBCMD_CONSIST_QUERY, (byte)(index & 0xff)};
        return new TractionControlRequestMessage(source, dest, payload);
    }

    /// Queries the length of the consist list.
    public static TractionControlRequestMessage createConsistLengthQuery(NodeID source, NodeID dest) {
        byte[] payload = new byte[]{CMD_CONSIST, SUBCMD_CONSIST_QUERY};
        return new TractionControlRequestMessage(source, dest, payload);
    }

    /// Lock/Reserve message
    public static TractionControlRequestMessage createReserve(NodeID source, NodeID dest) {
        byte[] payload = new byte[]{CMD_MGMT, SUBCMD_MGMT_RESERVE};
        return new TractionControlRequestMessage(source, dest, payload);
    }

    /// Unlock/Release message
    public static TractionControlRequestMessage createRelease(NodeID source, NodeID dest) {
        byte[] payload = new byte[]{CMD_MGMT, SUBCMD_MGMT_RELEASE};
        return new TractionControlRequestMessage(source, dest, payload);
    }

    /// Noop message (used for heartbeat request).
    public static TractionControlRequestMessage createNoop(NodeID source, NodeID dest) {
        byte[] payload = new byte[]{CMD_MGMT, SUBCMD_MGMT_NOOP};
        return new TractionControlRequestMessage(source, dest, payload);
    }

    public byte getCmd() throws ArrayIndexOutOfBoundsException {
        int p = payload[0] & 0xff;
        // Removes the consist listener forward flag.
        return (byte)(p & 0x7F);
    }

    /**
     * Checks if this message is forwarded as part of the consist listener protocol.
     * @return true if this message is a forwarded traciton request, false if this is an original
     * (operator-initiated) command.
     */
    public boolean isListenerMessage() {
        return (payload[0] & CMD_LISTENER_FORWARD) != 0;
    }

    // Valid for messages that contain a subcommand.
    public byte getSubCmd() throws ArrayIndexOutOfBoundsException {
        return payload[1];
    }

    // Valid for SetSpeed message
    public Float16 getSpeed() throws ArrayIndexOutOfBoundsException {
        return new Float16((((int)payload[1]) << 8) | (payload[2] & 0xff));
    }

    // Valid only for get function request
    public int getFnNumber() {
        int retval = 0;
        retval = payload[1] & 0xff;
        retval <<= 8;
        retval |= (payload[2] & 0xff);
        retval <<= 8;
        retval |= (payload[3] & 0xff);
        return retval;
    }

    // Valid only for get function request
    public int getFnVal() {
        int retval = 0;
        retval = payload[4] & 0xff;
        retval <<= 8;
        retval |= (payload[5] & 0xff);
        return retval;
    }

    @Override
    public void applyTo(MessageDecoder decoder, Connection sender) {
        decoder.handleTractionControlRequest(this, sender);
    }

    @Override
    public MessageTypeIdentifier getEMTI() {
        return MessageTypeIdentifier.TractionControlRequest;
    }

    /// Converts a wire format speed value to a user visible string to be used for debug printouts.
    public static String speedToDebugString(Float16 sp) {
        StringBuilder p = new StringBuilder();
        if (sp.isPositive()) p.append('F'); else p.append('R');
        p.append(" ");
        double dsp = Math.abs(sp.getFloat());
        double mph = dsp / MPH;
        p.append(String.format("%.0f mph", mph));
        return p.toString();
    }

    /// Converts the wire format of a listener attach flag byte to a user readable debug string.
    public static String consistFlagsToDebugString(int flags) {
        StringBuilder b = new StringBuilder();
        if ((flags & CONSIST_FLAG_ALIAS) != 0){
            b.append("alias,");
        }
        if ((flags & CONSIST_FLAG_REVERSE) != 0){
            b.append("reverse,");
        }
        if ((flags & CONSIST_FLAG_FN0) != 0){
            b.append("link-f0,");
        }
        if ((flags & CONSIST_FLAG_FNN) != 0){
            b.append("link-f*,");
        }
        if ((flags & CONSIST_FLAG_HIDE) != 0){
            b.append("hide,");
        }
        if (b.length() > 0) {
            b.deleteCharAt(b.length() - 1);
        }
        return b.toString();
    }

    @Override
    public String toString() {
        StringBuilder p = new StringBuilder(getSourceNodeID().toString());
        p.append(" - ");
        p.append(getDestNodeID());
        p.append(" ");
        p.append(getEMTI().toString());
        p.append(" ");
        if (isListenerMessage()) {
            p.append("[listener] ");
        }
        try {
            switch (getCmd()) {
                case CMD_SET_SPEED: {
                    p.append("set speed ");
                    p.append(speedToDebugString(getSpeed()));
                    break;
                }
                case CMD_GET_SPEED:
                    p.append("get speed");
                    break;
                case CMD_ESTOP:
                    p.append("set estop");
                    break;
                case CMD_SET_FN: {
                    int fn = Utilities.NetworkToHostUint24(payload, 1);
                    int val = Utilities.NetworkToHostUint16(payload, 4);
                    p.append(String.format("set fn %d to %d", fn, val));
                    break;
                }
                case CMD_GET_FN: {
                    int fn = Utilities.NetworkToHostUint24(payload, 1);
                    p.append(String.format("get fn %d", fn));
                    break;
                }
                case CMD_CONTROLLER: {
                    switch(getSubCmd()) {
                        case SUBCMD_CONTROLLER_ASSIGN: {
                            long nid = Utilities.NetworkToHostUint48(payload, 3);
                            p.append("assign controller ");
                            p.append(new NodeID(nid).toString());
                            int flags = Utilities.NetworkToHostUint8(payload, 2);
                            if(flags != 0) {
                                p.append(String.format(" flags 0x%02x", flags));
                            }
                            break;
                        }
                        case SUBCMD_CONTROLLER_RELEASE: {
                            long nid = Utilities.NetworkToHostUint48(payload, 3);
                            p.append("release controller ");
                            p.append(new NodeID(nid).toString());
                            int flags = Utilities.NetworkToHostUint8(payload, 2);
                            if(flags != 0) {
                                p.append(String.format(" flags 0x%02x", flags));
                            }
                            break;
                        }
                        case SUBCMD_CONTROLLER_QUERY: {
                            p.append("query controller");
                            break;
                        }
                        case SUBCMD_CONTROLLER_CHANGE: {
                            long nid = Utilities.NetworkToHostUint48(payload, 3);
                            p.append("notify controller change to ");
                            p.append(new NodeID(nid).toString());
                            int flags = Utilities.NetworkToHostUint8(payload, 2);
                            if(flags != 0) {
                                p.append(String.format(" flags 0x%02x", flags));
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
                            long nid = Utilities.NetworkToHostUint48(payload, 3);
                            int flags = Utilities.NetworkToHostUint8(payload, 2);
                            p.append("listener attach ");
                            p.append(new NodeID(nid).toString());
                            if(flags != 0) {
                                p.append(" flags ");
                                p.append(consistFlagsToDebugString(flags));
                            }
                            break;
                        }
                        case SUBCMD_CONSIST_DETACH: {
                            long nid = Utilities.NetworkToHostUint48(payload, 3);
                            int flags = Utilities.NetworkToHostUint8(payload, 2);
                            p.append("listener detach ");
                            p.append(new NodeID(nid).toString());
                            if(flags != 0) {
                                p.append(String.format(" flags 0x%02x", flags));
                            }
                            break;
                        }
                        case SUBCMD_CONSIST_QUERY: {
                            p.append("listener query");
                            if (payload.length > 2) {
                                p.append(String.format(" index %d", payload[2] & 0xff));
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
                            p.append("management reserve");
                            break;
                        }
                        case SUBCMD_MGMT_RELEASE: {
                            p.append("management release");
                            break;
                        }
                        case SUBCMD_MGMT_NOOP: {
                            p.append("noop/heartbeat");
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
