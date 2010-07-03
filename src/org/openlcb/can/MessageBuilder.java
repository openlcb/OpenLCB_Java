package org.openlcb.can;

import org.openlcb.*;

import java.util.List;

/**
 * Converts CAN frame messages to regular messages
 * and vice-versa.
 *<p>
 * In general, the connection is one-to-one, but
 * sometimes (datagram expansion) more than one will be
 * created.
 * The List is always returned, even if empty.
 * 
 * @author  Bob Jacobsen   Copyright 2010
 * @version $Revision$
 */
public class MessageBuilder {

    public MessageBuilder(AliasMap map) {
        this.map = map;
    }

    AliasMap map;
    
    public List<Message> processFrame(OpenLcbCanFrame f) {
        List<Message> retlist = new java.util.ArrayList<Message>();
        NodeID source = map.getNodeID(f.getSourceAlias());

        // check for special cases first
        
        // then decode standard MTIs
        switch(1) {

        case OpenLcb.MTI_INITIALIZATION_COMPLETE:
            retlist.add(new InitializationCompleteMessage(source));
            break;
        
        case OpenLcb.MTI_VERIFY_NID:
            retlist.add(new VerifyNodeIDNumberMessage(source));
            break;
        case OpenLcb.MTI_VERIFIED_NID:      // also 30B2 
            retlist.add(new VerifiedNodeIDNumberMessage(source));
            break;
        
        case OpenLcb.MTI_IDENTIFY_CONSUMERS:
            retlist.add(new IdentifyConsumersMessage(source, f.getEventID()));
            break;
            
        case OpenLcb.MTI_CONSUMER_IDENTIFIED:
            retlist.add(new ConsumerIdentifiedMessage(source, f.getEventID()));
            break;
        
        case OpenLcb.MTI_IDENTIFY_PRODUCERS:
            retlist.add(new IdentifyProducersMessage(source, f.getEventID()));
            break;
        
        case OpenLcb.MTI_PRODUCER_IDENTIFIED:
            retlist.add(new ProducerIdentifiedMessage(source, f.getEventID()));
            break;
        
        case OpenLcb.MTI_IDENTIFY_EVENTS:   // also 32B2
            retlist.add(new IdentifyEventsMessage(source));
            break;
        
        case OpenLcb.MTI_LEARN_EVENT:
            retlist.add(new LearnEventMessage(source, f.getEventID()));
            break;
        case OpenLcb.MTI_PC_EVENT_REPORT:
            retlist.add(new ProducerConsumerEventReportMessage(source, f.getEventID()));
            break;
        
        case OpenLcb.MTI_DATAGRAM:
        case OpenLcb.MTI_DATAGRAM_RCV_OK:
        case OpenLcb.MTI_DATAGRAM_REJECTED:
    
        case OpenLcb.MTI_STREAM_INIT_REQUEST:
        case OpenLcb.MTI_STREAM_INIT_REPLY:
        case OpenLcb.MTI_STREAM_DATA_SEND:
        case OpenLcb.MTI_STREAM_DATA_PROCEED:
        case OpenLcb.MTI_STREAM_DATA_COMPLETE:
        
        //case OpenLcb.MTI_IDENTIFY_CONSUMERS_RANGE:
        //case OpenLcb.MTI_IDENTIFY_PRODUCERS_RANGE:
        
        default:
            // this is an error
        }
        return retlist;
    }

    public List<OpenLcbCanFrame> processMessage(Message f) {
        List<OpenLcbCanFrame> retlist = new java.util.ArrayList<OpenLcbCanFrame>();
        switch(f.getMTI()) {

        case OpenLcb.MTI_INITIALIZATION_COMPLETE:

        case OpenLcb.MTI_VERIFY_NID:
        case OpenLcb.MTI_VERIFIED_NID:      // also 30B2 
        
        case OpenLcb.MTI_IDENTIFY_CONSUMERS:
        case OpenLcb.MTI_IDENTIFY_CONSUMERS_RANGE:
        case OpenLcb.MTI_CONSUMER_IDENTIFIED:
        
        case OpenLcb.MTI_IDENTIFY_PRODUCERS:
        case OpenLcb.MTI_IDENTIFY_PRODUCERS_RANGE:
        case OpenLcb.MTI_PRODUCER_IDENTIFIED:
        
        case OpenLcb.MTI_IDENTIFY_EVENTS:   // also 32B2
        
        case OpenLcb.MTI_LEARN_EVENT:
        case OpenLcb.MTI_PC_EVENT_REPORT:
        
        case OpenLcb.MTI_DATAGRAM:
        case OpenLcb.MTI_DATAGRAM_RCV_OK:
        case OpenLcb.MTI_DATAGRAM_REJECTED:
    
        case OpenLcb.MTI_STREAM_INIT_REQUEST:
        case OpenLcb.MTI_STREAM_INIT_REPLY:
        case OpenLcb.MTI_STREAM_DATA_SEND:
        case OpenLcb.MTI_STREAM_DATA_PROCEED:
        case OpenLcb.MTI_STREAM_DATA_COMPLETE:
        }
        return retlist;
    }
 }
