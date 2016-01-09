package org.openlcb;

/**
 * Created by bracz on 1/6/16.
 */
public enum EventState {
    Valid (0),
    Invalid (1),
    Unknown (3),
    Reserved (2);

    private final int modifier;

    EventState(int modifier) {
        this.modifier = modifier;
    }

    public int getModifier() {
        return modifier;
    }

    public MessageTypeIdentifier getProducerIdentifierMti() {
        int rawMti = MessageTypeIdentifier.ProducerIdentifiedValid.mti();
        rawMti &= ~3;
        rawMti |= modifier;
        return MessageTypeIdentifier.get(rawMti);
    }

    public MessageTypeIdentifier getConsumerIdentifierMti() {
        int rawMti = MessageTypeIdentifier.ConsumerIdentifiedValid.mti();
        rawMti &= ~3;
        rawMti |= modifier;
        return MessageTypeIdentifier.get(rawMti);
    }

    public EventState inverted() {
        switch(this) {
            case Valid: return Invalid;
            case Invalid: return Valid;
            default: return this;
        }
    }
}
