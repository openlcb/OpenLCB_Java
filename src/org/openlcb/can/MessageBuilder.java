package org.openlcb.can;

import org.openlcb.*;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

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
    public List<Message> processFrame(CanFrame f) {
        // check for special cases first
        if ( (f.getHeader() & 0x08000000) != 0x08000000 ) return null;  // not OpenLCB frame
        
        // break into types
        int format = ( f.getHeader() & 0x07000000 ) >> 24;

        switch (format) {
            case 0:
                return processFormat0(f);
            case 1:
                return processFormat1(f);
            case 2:
                return processFormat2(f);
            case 3:
                return processFormat3(f);
            case 4:
                return processFormat4(f);
            case 5:
                return processFormat5(f);
            case 6:
                return processFormat6(f);
            case 7:
                return processFormat7(f);
            default:  // should not happen
                return null;
        }
    }
    
    HashMap<NodeID, List<Integer>> datagramData = new HashMap<NodeID, List<Integer>>();
    
    int getSourceID(CanFrame f) { return f.getHeader()&0x00000FFF; }
    int getType(CanFrame f) { return ( f.getHeader() & 0x00FF0000 ) >> 16; }
    EventID getEventID(CanFrame f) { return new EventID(f.getData()); }
    
    List<Message> processFormat0(CanFrame f) {
        // simple MTI
        List<Message> retlist = new java.util.ArrayList<Message>();
        NodeID source = map.getNodeID(getSourceID(f));

        int type = getType(f);
        switch (type) {
            case 0x8A: 
                retlist.add(new VerifyNodeIDNumberMessage(source));
                return retlist;
            case 0x8B: 
                retlist.add(new VerifiedNodeIDNumberMessage(source));
                return retlist;
            case 0xA4: 
                retlist.add(new IdentifyConsumersMessage(source, getEventID(f)));
                return retlist;
            case 0xA8: 
                retlist.add(new IdentifyProducersMessage(source, getEventID(f)));
                return retlist;
            case 0xAC: 
                retlist.add(new LearnEventMessage(source, getEventID(f)));
                return retlist;
            case 0xAD: 
                retlist.add(new ProducerConsumerEventReportMessage(source, getEventID(f)));
                return retlist;
            case 0x08: 
                retlist.add(new InitializationCompleteMessage(source));
                return retlist;
            case 0x26: 
                retlist.add(new ConsumerIdentifiedMessage(source, getEventID(f)));
                return retlist;
            case 0x2A: 
                retlist.add(new ProducerIdentifiedMessage(source, getEventID(f)));
                return retlist;
            default: return null;
        }
    }
    List<Message> processFormat1(CanFrame f) {
        // reserved
        return null;
    }
    List<Message> processFormat2(CanFrame f) {
        // datagram only-segment
        NodeID source = map.getNodeID(getSourceID(f));
        List<Integer> list = datagramData.get(source);
        if (list == null) {
            list = new ArrayList<Integer>();
            // don't need to put it back, as we're doing just one
        } else {
            // this is actually an error, datagram already in process for only-segment
        }
        for (int i = 0; i < f.getNumDataElements(); i++) {
            list.add(f.getElement(i));
        }
        
        // done, forward
        
        int[] data = new int[list.size()];
        for (int i=0; i<list.size(); i++) {
            data[i] = list.get(i);
        }
        List<Message> retlist = new java.util.ArrayList<Message>();
        NodeID dest = map.getNodeID( (f.getHeader() & 0x00FFF000) >> 12);
        retlist.add(new DatagramMessage(source, dest, data));
        return retlist;
    }
    List<Message> processFormat3(CanFrame f) {
        // datagram first-segment
        NodeID source = map.getNodeID(getSourceID(f));
        List<Integer> list = datagramData.get(source);
        if (list == null) {
            list = new ArrayList<Integer>();
            datagramData.put(source, list);
        } else {
            // this is actually an error, datagram already in process for only-segment
        }
        for (int i = 0; i < f.getNumDataElements(); i++) {
            list.add(f.getElement(i));
        }
        return null;
    }
    List<Message> processFormat4(CanFrame f) {
        // datagram middle-segment
        NodeID source = map.getNodeID(getSourceID(f));
        List<Integer> list = datagramData.get(source);
        if (list == null) {
            // this is actually an error, should be already started
            list = new ArrayList<Integer>();
            datagramData.put(source, list);
        }
        for (int i = 0; i < f.getNumDataElements(); i++) {
            list.add(f.getElement(i));
        }
        return null;
    }
    List<Message> processFormat5(CanFrame f) {
        // datagram last
        NodeID source = map.getNodeID(getSourceID(f));
        List<Integer> list = datagramData.get(source);
        if (list == null) {
            list = new ArrayList<Integer>();
        }
        for (int i = 0; i < f.getNumDataElements(); i++) {
            list.add(f.getElement(i));
        }
        
        datagramData.put(source, null); // not accumulating any more
        
        int[] data = new int[list.size()];
        for (int i=0; i<list.size(); i++) {
            data[i] = list.get(i);
        }
        List<Message> retlist = new java.util.ArrayList<Message>();
        NodeID dest = map.getNodeID( (f.getHeader() & 0x00FFF000) >> 12);
        retlist.add(new DatagramMessage(source, dest, data));
        return retlist;
    }
    List<Message> processFormat6(CanFrame f) {
        List<Message> retlist = new java.util.ArrayList<Message>();
        NodeID source = map.getNodeID(getSourceID(f));
        int type = f.getElement(0);
        switch (type) {
            case 0x2E: 
                retlist.add(new ProtocolIdentificationRequestMessage(source));
                return retlist;
            case 0x2F: 
                retlist.add(new ProtocolIdentificationReplyMessage(source,f.bodyAsLong()));
                return retlist;
            case 0x52: 
                retlist.add(new SimpleNodeIdentInfoRequestMessage(source));
                return retlist;
            case 0x53: 
                byte[] content = f.getData();
                byte[] data = new byte[content.length-1];
                System.arraycopy(content, 1, data, 0, data.length);
                
                retlist.add(new SimpleNodeIdentInfoReplyMessage(source,data));
                return retlist;
            default: return null;
        }
    }
    List<Message> processFormat7(CanFrame f) {
        // stream data
        return null;
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
            // must loop over data to send 8 byte chunks
            int remains = msg.getData().length;
            int j = 0;
            // always sends at least one datagram, even with zero bytes
            do {
                int size = Math.min(8, remains);
                int[] data = new int[size];
                for (int i = 0; i<size; i++) {
                    data[i] = msg.getData()[j++];
                }
                
                OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
                f.setDatagram(data, map.getAlias(msg.getDestNodeID()), remains <= 8);
                f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
                retlist.add(f);
                
                remains = remains - size;
            } while (remains > 0);
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
