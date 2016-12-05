package org.openlcb;

/**
 * Common base class for Addressed messages carrying a payload. This base class is easy
 * to serialize and deserialize from an interface like CAN.
 *
 * Created by bracz on 12/29/15.
 */
public abstract class AddressedPayloadMessage extends AddressedMessage {
    protected byte[] payload;

    public byte[] getPayload() { return payload; }

    public AddressedPayloadMessage(NodeID source, NodeID dest, byte[] payload) {
        super(source, dest);
        if (payload == null) {
            this.payload = new byte[0];
        } else {
            this.payload = payload.clone();
        }
    }

    public String toString() {
        StringBuilder p = new StringBuilder(super.toString());
        p.append(" ");
        p.append(getEMTI().toString());
        if (payload.length > 0) {
            p.append(" with payload ");
            p.append(Utilities.toHexSpaceString(payload));
        } else {
            p.append(" with no payload");
        }
        return p.toString();
    }

    public abstract MessageTypeIdentifier getEMTI();

    @Override
    public int getMTI() {
        return getEMTI().mti();
    }
}
