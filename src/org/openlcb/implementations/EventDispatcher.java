package org.openlcb.implementations;

import org.openlcb.Connection;
import org.openlcb.ConsumerIdentifiedMessage;
import org.openlcb.EventID;
import org.openlcb.EventMessage;
import org.openlcb.IdentifyConsumersMessage;
import org.openlcb.IdentifyProducersMessage;
import org.openlcb.LearnEventMessage;
import org.openlcb.Message;
import org.openlcb.MessageDecoder;
import org.openlcb.OlcbInterface;
import org.openlcb.ProducerConsumerEventReportMessage;
import org.openlcb.ProducerIdentifiedMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * The EventDidspatcher allows efficient handling of a very large number of listeners that are
 * each individually interested in a few events only. Typical usage is for producers and
 * consumers of individual events to register for the EventDispatcher instead of directly
 * registering with the Interface. This keeps the number of calls made for each event message
 * under control.
 * <p>
 * Non-event messages are forwarded to all listeners.
 * <p>
 * Created by bracz on 8/9/17.
 */

public class EventDispatcher extends MessageDecoder {
    /**
     * Constructor.
     * @param iface registers the dispatcher to this interface.
     */
    public EventDispatcher(OlcbInterface iface) {
        this.iface = iface;
        iface.registerMessageListener(this);
    }

    /**
     * Removes this object from the interface.
     */
    void release() {
        iface.unRegisterMessageListener(this);
    }

    private OlcbInterface iface;
    /// Main dispatcher data structure. Proxies event IDs to the individual handlers.
    private HashMap<EventID, List<Connection>> eventMap = new HashMap<>();
    /// Reverse map that allow us to figure out what event IDs are registered for a given decoder
    /// object.
    private HashMap<Connection, List<EventID>> decoderMap = new HashMap<>();

    /**
     * Registers a given listener to receive all messages relating to a given event.
     *
     * @param listener the listener to receive the message
     * @param event    the event for which the messages shall be received
     */
    public synchronized void registerListener(@Nonnull Connection listener, @Nonnull EventID
            event) {
        List<Connection> mdl = eventMap.get(event);
        if (mdl == null) {
            mdl = new ArrayList<>(1);
            eventMap.put(event, mdl);
        }
        mdl.add(listener);

        List<EventID> el = decoderMap.get(listener);
        if (el == null) {
            el = new ArrayList<>(2);
            decoderMap.put(listener, el);
        }
        el.add(event);
    }

    /**
     * Unregisters a given listener from all events that it has been previously registered for.
     *
     * @param listener listener to remove.
     */
    public synchronized void unRegisterListener(@Nonnull Connection listener) {
        List<EventID> el = decoderMap.get(listener);
        if (el == null) return;
        decoderMap.remove(listener);
        for (EventID e : el) {
            List<Connection> mdl = eventMap.get(e);
            while (mdl.remove(listener)) ;
        }
    }

    /**
     * Forwards a given event message to all listeners registered for that event.
     *
     * @param message message to forward
     * @param sender  sender of message (passed on to the listener)
     */
    private void putEventMessage(EventMessage message, Connection sender) {
        List<Connection> l;
        synchronized (this) {
            l = eventMap.get(message.getEventID());
        }
        if (l == null) return;
        synchronized (l) {
            for (Connection tgt :
                    l) {
                tgt.put(message, sender);
            }
        }
    }

    @Override
    protected void defaultHandler(Message msg, Connection sender) {
        // All other messages are forwarded to each registered listener.
        synchronized (this) {
            for (Connection dec : decoderMap.keySet()) {
                dec.put(msg, sender);
            }
        }
    }

    @Override
    public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg,
                                                  Connection sender) {
        putEventMessage(msg, sender);
    }

    @Override
    public void handleLearnEvent(LearnEventMessage msg, Connection sender) {
        putEventMessage(msg, sender);
    }

    @Override
    public void handleIdentifyConsumers(IdentifyConsumersMessage msg, Connection sender) {
        putEventMessage(msg, sender);
    }

    @Override
    public void handleConsumerIdentified(ConsumerIdentifiedMessage msg, Connection sender) {
        putEventMessage(msg, sender);
    }

    @Override
    public void handleIdentifyProducers(IdentifyProducersMessage msg, Connection sender) {
        putEventMessage(msg, sender);
    }

    @Override
    public void handleProducerIdentified(ProducerIdentifiedMessage msg, Connection sender) {
        putEventMessage(msg, sender);
    }
}
