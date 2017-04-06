package org.openlcb.implementations;

import org.openlcb.DefaultPropertyListenerSupport;
import org.openlcb.EventID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

/**
 * EventTable keeps an in-memory representation of all currently known event IDs on the bus. It
 * allows searching for event IDs by description and looking up descriptions for event IDs. The
 * entries in EventTable are owned by live Java objects using reference holders and disappear
 * when requested.
 * <p>
 * EventTable is thread-safe.
 * <p>
 * Created by bracz on 4/6/17.
 */

@ThreadSafe
public class EventTable {
    private final HashMap<Long, EventInfo> entries = new HashMap<>();

    public final static String UPDATED_EVENT_LIST = "UPDATED_EVENT_LIST";

    public EventInfo getEventInfo(EventID event) {
        synchronized (entries) {
            long key = event.toLong();
            EventInfo entry = entries.get(key);
            if (entry == null) {
                entry = new EventInfo(event);
                entries.put(key, entry);
            }
            return entry;
        }
    }

    public EventTableEntryHolder addEvent(EventID event, String description) {
        return getEventInfo(event).add(description);
    }

    /**
     * Collects all registered entries for the same event ID.
     */
    @ThreadSafe
    class EventInfo extends DefaultPropertyListenerSupport {
        private final EventID eventId;
        private final List<EventTableEntry> entries = new ArrayList<>();

        EventInfo(EventID id) {
            eventId = id;
        }

        public EventID getEventId() {
            return eventId;
        }

        public EventTableEntryHolder add(String description) {
            EventTableEntry newEntry = new EventTableEntry(description);
            EventTableEntryHolder h = new EventTableEntryHolder(this, newEntry);
            newEntry.h = h;
            synchronized (entries) {
                entries.add(newEntry);
            }
            firePropertyChange(UPDATED_EVENT_LIST, null, this);
            return h;
        }

        void remove(EventTableEntryHolder h) {
            synchronized (entries) {
                entries.removeIf((EventTableEntry e) -> e.h == h);
            }
            firePropertyChange(UPDATED_EVENT_LIST, null, this);
        }

        EventTableEntry[] getAllEntries() {
            synchronized (entries) {
                EventTableEntry[] ar = new EventTableEntry[entries.size()];
                entries.toArray(ar);
                return ar;
            }
        }
    }

    public class EventTableEntry {
        String description;
        EventTableEntryHolder h;

        EventTableEntry(String d) {
            description = d;
        }

        public String getDescription() { return description; }
        public EventID getEvent() { return h.event.getEventId(); }
    }

    public class EventTableEntryHolder {
        final EventTableEntry entry;
        final EventInfo event;

        EventTableEntryHolder(EventInfo event, EventTableEntry e) {
            this.event = event;
            entry = e;
        }

        public void release() {
            event.remove(this);
        }
    }
}
