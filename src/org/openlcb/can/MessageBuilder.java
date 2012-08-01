package org.openlcb.can;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.openlcb.*;

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
    int getMTI(CanFrame f) { return ( f.getHeader() & 0x00FFF000 ) >> 12; }
    EventID getEventID(CanFrame f) { return new EventID(f.getData()); }
    
    List<Message> processFormat0(CanFrame f) {
        // reserved
        return null;
    }

    List<Message> processFormat1(CanFrame f) {
        // MTI
        List<Message> retlist = new java.util.ArrayList<Message>();
        NodeID source = map.getNodeID(getSourceID(f));
        NodeID dest = null;
        int mti = getMTI(f);
        if ( ((mti&0x008) != 0) && (f.getNumDataElements() >= 2) ) // addressed message 
            dest = map.getNodeID( ( (f.getElement(0) << 8) + f.getElement(1) ) & 0xFFF );

        MessageTypeIdentifier value = MessageTypeIdentifier.get(mti);
        if (value == null) System.out.println(" found null from "+mti);
        switch (value) {
            case InitializationComplete: 
                retlist.add(new InitializationCompleteMessage(source));
                return retlist;
            case VerifyNodeIdGlobal: 
                retlist.add(new VerifyNodeIDNumberMessage(source));
                return retlist;
            case VerifiedNodeId: 
                retlist.add(new VerifiedNodeIDNumberMessage(source));
                return retlist;

            case OptionalInteractionRejected: {
                    int d2 = f.getNumDataElements() >= 2 ? f.getElement(2) : 0;
                    int d3 = f.getNumDataElements() >= 3 ? f.getElement(3) : 0;
                    int d4 = f.getNumDataElements() >= 4 ? f.getElement(4) : 0;
                    int d5 = f.getNumDataElements() >= 5 ? f.getElement(5) : 0;
                    int retmti = ((d2&0xff)<<8) | (d3&0xff);
                    int code = ((d4&0xff)<<8) | (d5&0xff);;
                    retlist.add(new OptionalIntRejectedMessage(source, dest,retmti,code));
                    return retlist;
                }
            case ProtocolSupportInquiry: 
                retlist.add(new ProtocolIdentificationRequestMessage(source, dest));
                return retlist;
            case ProtocolSupportReply: 
                retlist.add(new ProtocolIdentificationReplyMessage(source,f.dataAsLong()));
                return retlist;

            case IdentifyConsumer:
                retlist.add(new IdentifyConsumersMessage(source, getEventID(f)));
                return retlist;
            case ConsumerIdentified: 
                retlist.add(new ConsumerIdentifiedMessage(source, getEventID(f)));
                return retlist;
            case IdentifyProducer: 
                retlist.add(new IdentifyProducersMessage(source, getEventID(f)));
                return retlist;
            case ProducerIdentified: 
                retlist.add(new ProducerIdentifiedMessage(source, getEventID(f)));
                return retlist;
            case ProducerConsumerEventReport: 
                retlist.add(new ProducerConsumerEventReportMessage(source, getEventID(f)));
                return retlist;

            case LearnEvent: 
                retlist.add(new LearnEventMessage(source, getEventID(f)));
                return retlist;

            case SimpleNodeIdentInfoRequest: 
                retlist.add(new SimpleNodeIdentInfoRequestMessage(source, dest));
                return retlist;
            case SimpleNodeIdentInfoReply: 
                byte[] content = f.getData();
                byte[] data = new byte[content.length-2];
                System.arraycopy(content, 2, data, 0, data.length);
                
                retlist.add(new SimpleNodeIdentInfoReplyMessage(source,data));
                return retlist;

            case DatagramReceivedOK: 
                retlist.add(new DatagramAcknowledgedMessage(source,dest));
                return retlist;
            case DatagramRejected: 
                retlist.add(new DatagramRejectedMessage(source,dest,(int)f.dataAsLong()));
                return retlist;
            default: return null;
        }
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
        // reserved
        return null;
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
        @Override
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
        @Override
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
        @Override
        public void handleVerifyNodeIDNumber(VerifyNodeIDNumberMessage msg, Connection sender){
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setVerifyNID(msg.getSourceNodeID());
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            retlist.add(f);
        }

        /**
         * Handle "Protocol Identification Inquiry (Request)" message
         */
        public void handleProtocolIdentificationRequest(ProtocolIdentificationRequestMessage msg, Connection sender){
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setOpenLcbMTI(MessageTypeIdentifier.ProtocolSupportInquiry.mti());
            f.setDestAlias(map.getAlias(msg.getDestNodeID()));
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            retlist.add(f);
        }

        /**
         * Handle "Producer/Consumer Event Report" message
         */
        @Override
        public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg, Connection sender){
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setPCEventReport(msg.getEventID());
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            retlist.add(f);
        }
        /**
         * Handle "Identify Consumers" message
         */
        @Override
        public void handleIdentifyConsumers(IdentifyConsumersMessage msg, Connection sender){
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setOpenLcbMTI(MessageTypeIdentifier.IdentifyConsumer.mti());
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            f.loadFromEid(msg.getEventID());
            retlist.add(f);
        }
        /**
         * Handle "Consumer Identified" message
         */
        @Override
        public void handleConsumerIdentified(ConsumerIdentifiedMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Identify Producers" message
         */
        @Override
        public void handleIdentifyProducers(IdentifyProducersMessage msg, Connection sender){
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setOpenLcbMTI(MessageTypeIdentifier.IdentifyProducer.mti());
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            f.loadFromEid(msg.getEventID());
            retlist.add(f);
        }
        /**
         * Handle "Producer Identified" message
         */
        @Override
        public void handleProducerIdentified(ProducerIdentifiedMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Identify Event" message
         */
        @Override
        public void handleIdentifyEvents(IdentifyEventsMessage msg, Connection sender){
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setOpenLcbMTI(MessageTypeIdentifier.IdentifyEventsGlobal.mti());
            f.setDestAlias(map.getAlias(msg.getDestNodeID()));
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            retlist.add(f);
        }
        /**
         * Handle "Learn Event" message
         */
        @Override
        public void handleLearnEvent(LearnEventMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Simple Node Ident Info Request" message
         */
        @Override
        public void handleSimpleNodeIdentInfoRequest(SimpleNodeIdentInfoRequestMessage msg, Connection sender){
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setOpenLcbMTI(MessageTypeIdentifier.SimpleNodeIdentInfoRequest.mti());
            f.setDestAlias(map.getAlias(msg.getDestNodeID()));
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            retlist.add(f);
        }
        /**
         * Handle "Datagram" message
         */
        @Override
        public void handleDatagram(DatagramMessage msg, Connection sender){
            // must loop over data to send 8 byte chunks
            int remains = msg.getData().length;
            int j = 0;
            boolean first = true;
            // always sends at least one datagram, even with zero bytes
            do {
                int size = Math.min(8, remains);
                int[] data = new int[size];
                for (int i = 0; i<size; i++) {
                    data[i] = msg.getData()[j++];
                }
                
                OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
                f.setDatagram(data, map.getAlias(msg.getDestNodeID()), first, remains <= 8);
                f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
                retlist.add(f);
                
                remains = remains - size;
                first = false;
            } while (remains > 0);
        }

        /**
         * Handle "Datagram Rejected" message
         */
        @Override
        public void handleDatagramRejected(DatagramRejectedMessage msg, Connection sender){
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setOpenLcbMTI(MessageTypeIdentifier.DatagramRejected.mti());
            f.setData(new byte[]{(byte)0, (byte)0, (byte)((msg.getCode()>>8)&0xFF), (byte)(msg.getCode()&0xFF)});
            f.setDestAlias(map.getAlias(msg.getDestNodeID()));
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            retlist.add(f);
        }
        /**
         * Handle "Datagram Acknowledged" message
         */
        @Override
        public void handleDatagramAcknowledged(DatagramAcknowledgedMessage msg, Connection sender){
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setOpenLcbMTI(MessageTypeIdentifier.DatagramReceivedOK.mti());
            f.setDestAlias(map.getAlias(msg.getDestNodeID()));
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            retlist.add(f);
        }
        /**
         * Handle "Stream Init Request" message
         */
        @Override
        public void handleStreamInitRequest(StreamInitRequestMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Stream Init Reply" message
         */
        @Override
        public void handleStreamInitReply(StreamInitReplyMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Stream Data Send" message
         */
        @Override
        public void handleStreamDataSend(StreamDataSendMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Stream Data Proceed" message
         */
        @Override
        public void handleStreamDataProceed(StreamDataProceedMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
        /**
         * Handle "Stream Data Complete" message
         */
        @Override
        public void handleStreamDataComplete(StreamDataCompleteMessage msg, Connection sender){
            defaultHandler(msg, sender);
        }
    }
 }
