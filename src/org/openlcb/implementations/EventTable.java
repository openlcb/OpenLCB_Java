package org.openlcb.implementations;

import org.openlcb.DefaultPropertyListenerSupport;
import org.openlcb.EventID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
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

    /// This property change notification is produced when the list of descriptions registered
    /// for a given event ID has changed (due to addition, removal or description change).
    public final static String UPDATED_EVENT_LIST = "UPDATED_EVENT_LIST";

    /**
     * Looks up a given event ID and tells what we know about it.
     *
     * @param event event ID.
     * @return the descriptor structure matching the given Event ID.
     */
    public
    @Nonnull
    EventInfo getEventInfo(EventID event) {
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

    /**
     * Convenience method to register a new event description.
     *
     * @param event       event ID for which we want to add a new entry
     * @param description the description of the new entry
     * @return Holder object that allows to change or remove the given entry. The caller must
     * keep this and call the release method before going out of scope.
     */
    public EventTableEntryHolder addEvent(EventID event, String description) {
        return getEventInfo(event).add(description);
    }

    /**
     * Collects all registered entries for the same event ID.
     */
    @ThreadSafe
    public class EventInfo extends DefaultPropertyListenerSupport {
        private final EventID eventId;
        private final List<EventTableEntry> entries = new ArrayList<>();

        EventInfo(EventID id) {
            eventId = id;
        }

        public EventID getEventId() {
            return eventId;
        }

        /**
         * Adds a new entry for this event ID.
         *
         * @param description the description for the new entry.
         * @return Holder object that allows to change or remove the given entry. The caller must
         * keep this and call the release method before going out of scope.
         */
        public EventTableEntryHolder add(String description) {
            EventTableEntry newEntry = new EventTableEntry(description);
            EventTableEntryHolder h = new EventTableEntryHolder(this, newEntry);
            newEntry.h = h;
            synchronized (entries) {
                entries.add(newEntry);
            }
            notifyUpdated();
            return h;
        }

        /**
         * Removes the entry represented by a given holder object. This method is not public,
         * please use Holder.release() as the client API.
         *
         * @param h the holder object.
         */
        void remove(EventTableEntryHolder h) {
            synchronized (entries) {
                entries.removeIf((EventTableEntry e) -> e.h == h);
            }
            notifyUpdated();
        }

        /**
         * Helper function used by the modifying functions.
         */
        void notifyUpdated() {
            firePropertyChange(UPDATED_EVENT_LIST, null, this);
        }

        /**
         * @return All entries associated with this event ID. The caller can then iterate over
         * the objects and access their description field.
         */
        public EventTableEntry[] getAllEntries() {
            synchronized (entries) {
                EventTableEntry[] ar = new EventTableEntry[entries.size()];
                entries.toArray(ar);
                return ar;
            }
        }
    }

    /**
     * Represents an owner-description pair for a given Event ID that is registered into the
     * Event Table.
     */
    public class EventTableEntry {
        /// The client can mutate this value.
        String description;
        /// This is the holder object that the client has a reference to.
        EventTableEntryHolder h;

        EventTableEntry(String d) {
            description = d;
        }

        /**
         * @return The currently associated description.
         */
        public String getDescription() {
            return description;
        }

        /**
         * @return the event ID to which this entry is registered.
         */
        public EventID getEvent() {
            return h.event.getEventId();
        }

        /**
         * @param holder Holder object that represents the ownership of the client of a specific
         *               entry in the current event table row.
         * @return true if the current entry is the one registered by the given holder object,
         * false if it is from a different owner.
         */
        public boolean isOwnedBy(EventTableEntryHolder holder) {
            return holder == h;
        }

        /**
         * Replace the description of the current entry, notifying clients who are listening.
         * @param newDescription user-visible string describing the EventID's usage represented
         *                       by this entry.
         */
        public void updateDescription(String newDescription) {
            synchronized (h.event.entries) {
                if (description.equals(newDescription)) return;
                description = newDescription;
            }
            h.event.notifyUpdated();
        }
    }

    /**
     * Resource holder class. Callers registering event table entries get a reference to an
     * object like this; they need to keep hold of that reference in order to make changes to the
     * entry or delete it. It also represents access control.
     */
    public class EventTableEntryHolder {
        final EventTableEntry entry;
        final EventInfo event;

        EventTableEntryHolder(EventInfo event, EventTableEntry e) {
            this.event = event;
            entry = e;
        }

        /**
         * Removes the pointed entry from the event table.
         */
        public void release() {
            event.remove(this);
        }

        /**
         * @return the pointed event table entry.
         */
        public EventTableEntry getEntry() {
            return entry;
        }

        /**
         * @return The object representing all entries for the current event ID.
         */
        public EventInfo getList() {
            return event;
        }
    }
}
