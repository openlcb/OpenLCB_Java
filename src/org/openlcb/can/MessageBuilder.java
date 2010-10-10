package org.openlcb.can;

import org.openlcb.*;

import java.util.List;

/**
 * Converts CAN frame messages to regular messages
 * and vice-versa.
 *<p>
 * In general, the connection is one-to-one, but
 * sometimes (datagram expansion) more than one frame will be
 * created.
 * 
 * @author  Bob Jacobsen   Copyright 2010
 * @version $Revision$
 */
public class MessageBuilder {

    /**
     * The provided AliasMap will be updated
     * as inbound frames are processed.
     */
    public MessageBuilder(AliasMap map) {
        this.map = map;
    }

    AliasMap map;
    
    /** 
     * Accept a frame, and convert to 
     * a standard OpenLCB Message object.
     *
     * The returned Message is fully initialized
     * with content, and the original frame can 
     * be dropped.
     *
     * The List is always returned, even if empty.
     */
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

    /** 
     * Accept an OpenLCB Message, and convert to 
     * a standard frame object.
     *
     * The returned frame(s) are fully initialized
     * with content, and the original frame can 
     * be dropped.
     *
     * The List is always returned, and should never be empty.
     */
    public List<OpenLcbCanFrame> processMessage(Message msg) {

        FrameBuilder f = new FrameBuilder();
        return f.convert(msg);
    }
    
    private class FrameBuilder extends org.openlcb.MessageDecoder {
        /**
         * Catches messages that are not explicitly 
         * handled and throws an error
         */
        protected void defaultHandler(Message msg, Connection sender) {
            throw new java.lang.NoSuchMethodError("no handler for Message: "+msg.toString());
        }
        
        List<OpenLcbCanFrame> retlist;
        
        List<OpenLcbCanFrame> convert(Message msg) {
            retlist = new java.util.ArrayList<OpenLcbCanFrame>();
            
            // Uses the double dispatch mechanism built into Message
            put(msg, null);  // no Connection needed
            
            return retlist;
        }
                
        /**
         * Handle "Initialization Complete" message
         */
        public void handleInitializationComplete(InitializationCompleteMessage msg, Connection sender){
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setInitializationComplete(map.getAlias(msg.getSourceNodeID()), msg.getSourceNodeID());
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            retlist.add(f);
        }
        /**
         * Handle "Verified Node ID Number" message
         */
        public void handleVerifiedNodeIDNumber(VerifiedNodeIDNumberMessage msg, Connection sender){
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setVerifiedNID(msg.getSourceNodeID());
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            retlist.add(f);
        }
        /**
         * Handle "Verify Node ID Number" message
         */
        public void handleVerifyNodeIDNumber(VerifyNodeIDNumberMessage msg, Connection sender){
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setVerifyNID(msg.getSourceNodeID());
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            retlist.add(f);
        }
        /**
         * Handle "Producer/Consumer Event Report" message
         */
        public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg, Connection sender){
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setPCEventReport(msg.getEventID());
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            retlist.add(f);
        }
        /**
         * Handle "Identify Consumers" message
         */
        public void handleIdentifyConsumers(IdentifyConsumersMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Consumer Identified" message
         */
        public void handleConsumerIdentified(ConsumerIdentifiedMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Identify Producers" message
         */
        public void handleIdentifyProducers(IdentifyProducersMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Producer Identified" message
         */
        public void handleProducerIdentified(ProducerIdentifiedMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Identify Event" message
         */
        public void handleIdentifyEvents(IdentifyEventsMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Learn Event" message
         */
        public void handleLearnEvent(LearnEventMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Datagram" message
         */
        public void handleDatagram(DatagramMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Datagram Rejected" message
         */
        public void handleDatagramRejected(DatagramRejectedMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Datagram Acknowledged" message
         */
        public void handleDatagramAcknowledged(DatagramAcknowledgedMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Stream Init Request" message
         */
        public void handleStreamInitRequest(StreamInitRequestMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Stream Init Reply" message
         */
        public void handleStreamInitReply(StreamInitReplyMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Stream Data Send" message
         */
        public void handleStreamDataSend(StreamDataSendMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Stream Data Proceed" message
         */
        public void handleStreamDataProceed(StreamDataProceedMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Stream Data Complete" message
         */
        public void handleStreamDataComplete(StreamDataCompleteMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
    }
 }
