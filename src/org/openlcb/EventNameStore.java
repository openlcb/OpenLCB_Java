package org.openlcb;

/**
 * Provide a mapping from EventID <-> User readable event names
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

    public EventID getEventID(String eventName);
    
    public String getEventName(EventID eventID);
    
}
