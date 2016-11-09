package org.openlcb;

/**
 * Created by bracz on 11/9/16.
 */
public abstract class EventMessage extends Message {
    protected final EventID eventID;

    public EventMessage(NodeID source, EventID eventID) {
        super(source);
        if (eventID == null)
            throw new IllegalArgumentException("EventID cannot be null");
        this.eventID = eventID;
    }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (!(o instanceof EventMessage)) return false;
        EventMessage p = (EventMessage) o;
        return (eventID.equals(p.eventID));
    }

    @Override
    public int hashCode() {
        return super.hashCode() | eventID.hashCode();
    }

    // because EventID is immutable, can directly return object
    public EventID getEventID() {
        return eventID;
    }
}
