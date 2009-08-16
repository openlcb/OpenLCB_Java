package org.nmra.net.implementations;

import org.nmra.net.*;

import java.util.HashSet;

/**
 * Gateway that filters event messages that aren't needed.
 *<p>
 * Provides two connections, called "East" and "West"
 * <p>
 * Filtering algorithm is simple:<br>
 *  Once a "ConsumerIdentified" message for a particular EventID
 *         comes from one side,<br>
 *  ProducedConsumerEventReport messages with that EventID are
 *         forwarded <u>to</u> that side.
 * 
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class EventFilterGateway extends Gateway {
    public EventFilterGateway() {
    }
    
    /**
     * Provide a connection object for use by
     * the East node.
     */
    public Connection getEastConnection() {
        eastInputConnection = new EastConnection();
        return eastInputConnection;
    }

    /**
     * Provide a connection object for use by
     * the West node.
     */
    public Connection getWestConnection() {
        westInputConnection = new WestConnection();
        return westInputConnection;
    }

    HashSet<EventID> idsGoingWest = new HashSet<EventID>();
    HashSet<EventID> idsGoingEast = new HashSet<EventID>();
    
    class EastConnection extends MessageDecoder implements Connection {
        boolean forward;

        @Override
        public void put(Message msg, Connection sender) {
            forward = true;     // all messages forwarded, unless veto'd
            msg.applyTo(this, sender);  // distribute
            if (forward) sendMessageToWest(msg, sender);
        }

        @Override
        public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg,
                                                        Connection sender){
            if (! idsGoingWest.contains(msg.getEventID()))
                forward = false;
        }

        @Override
        public void handleConsumerIdentified(ConsumerIdentifiedMessage msg,
                                                        Connection sender){
            idsGoingEast.add(msg.getEventID());
        }
    }    

    class WestConnection extends MessageDecoder implements Connection {
        boolean forward;

        @Override
        public void put(Message msg, Connection sender) {
            forward = true;     // all messages forwarded, unless veto'd
            msg.applyTo(this, sender);  // distribute
            if (forward) sendMessageToEast(msg, sender);
        }

        @Override
        public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg,
                                                        Connection sender){
            if (! idsGoingEast.contains(msg.getEventID()))
                forward = false;
        }

        @Override
        public void handleConsumerIdentified(ConsumerIdentifiedMessage msg,
                                                        Connection sender){
            idsGoingWest.add(msg.getEventID());
        }
    }    
}