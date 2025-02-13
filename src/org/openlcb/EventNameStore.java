package org.openlcb;

/**
 * Provide a mapping from EventID from/to User readable event names
 *
 * Intended to be implemented and provided externally to this library.
 * The event names here obey some external syntax, and are sourced
 * externally.
 *
 * This is deliberately separate from the
 * {@link org.openlcb.implementations.EventTable}
 * which is an internal structure specific to this library.
 *
 *
 * @author	Bob Jacobsen   Copyright (C) 2024
 */
public interface EventNameStore {

    /**
     * @param eventName Either a previously stored event name that is 
     *      is associated to an event ID, or the dotted-hex form of an Event ID
     * @return an eventID from tne matching name, if any
     *          otherwise directly return the doted-hex input.
     */
    public EventID getEventID(String eventName);
    
    /**
     * @param A valid event ID, not null
     * @return If a name has been associated with this event ID, return that name, 
     *      otherwise an event ID from parsing the eventName as dotted-hex.
     */
    public String getEventName(EventID eventID);
    
}
