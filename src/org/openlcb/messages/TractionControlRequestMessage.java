package org.openlcb.messages;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import org.openlcb.AddressedPayloadMessage;
import org.openlcb.Connection;
import org.openlcb.MessageDecoder;
import org.openlcb.MessageTypeIdentifier;
import org.openlcb.NodeID;
import org.openlcb.implementations.throttle.Float16;

/**
 * Traction Control Request message implementation.
 * <p/>
 * Created by bracz on 12/29/15.
 */
@Immutable
@ThreadSafe
public class TractionControlRequestMessage extends AddressedPayloadMessage {
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

    public final static byte CMD_MGMT = 0x20;
    public final static byte SUBCMD_MGMT_RESERVE = 1;
    public final static byte SUBCMD_MGMT_RELEASE = 2;


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
        Float16 sp = new Float16(speed);
        byte[] payload = new byte[]{CMD_SET_SPEED, sp.getByte1(), sp.getByte2()};
        return new TractionControlRequestMessage(source, dest, payload);
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

    public byte getCmd() throws ArrayIndexOutOfBoundsException {
        return payload[0];
    }

    // Valid for messages that contain a subcommand.
    public byte getSubCmd() throws ArrayIndexOutOfBoundsException {
        return payload[1];
    }

    // Valid for SetSpeed message
    public Float16 getSpeed() throws ArrayIndexOutOfBoundsException {
        return new Float16((((int)payload[1]) << 8) | (payload[2] & 0xff));
    }

    @Override
    public void applyTo(MessageDecoder decoder, Connection sender) {
        decoder.handleTractionControlRequest(this, sender);
    }

    @Override
    public MessageTypeIdentifier getEMTI() {
        return MessageTypeIdentifier.TractionControlRequest;
    }
}
