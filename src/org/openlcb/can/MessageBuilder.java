package org.openlcb.can;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.openlcb.*;
import org.openlcb.messages.TractionControlReplyMessage;
import org.openlcb.messages.TractionControlRequestMessage;
import org.openlcb.messages.TractionProxyReplyMessage;
import org.openlcb.messages.TractionProxyRequestMessage;

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
    // dph
    HashMap<NodeID, List<Integer>> streamData = new HashMap<NodeID, List<Integer>>();
    
    int getSourceID(CanFrame f) { return f.getHeader()&0x00000FFF; }
    int getMTI(CanFrame f) { return ( f.getHeader() & 0x00FFF000 ) >> 12; }
    EventID getEventID(CanFrame f) { return new EventID(f.getData()); }
    
    List<Message> processFormat0(CanFrame f) {
        // reserved
        return null;
    }

    class AccumulationMemo {
        long header;
        NodeID source;
        NodeID dest;
        byte[] data;
        
        public AccumulationMemo(long header, NodeID source, NodeID dest, byte[] data) {
            this.header = header;
            this.source = source;
            this.dest = dest;
            this.data = data;
        }
        
        public boolean equals(Object obj) {
            if (! (obj instanceof AccumulationMemo) )
                    return false;
            
            AccumulationMemo other = (AccumulationMemo)obj;
            if (header != other.header) return false;
            if ( ! source.equals(other.source)) return false;
            if ( ! dest.equals(other.dest)) return false;
            return true;
        }
        
        // data varies in length, not considered.
        public int hashCode() {
            return (int)(header+dest.hashCode()+source.hashCode());
        }
    }
    
    HashMap<Integer, AccumulationMemo> accumulations = new HashMap<Integer, AccumulationMemo>();
    
    List<Message> processFormat1(CanFrame f) {
        // MTI
        List<Message> retlist = new java.util.ArrayList<Message>();
        NodeID source = map.getNodeID(getSourceID(f));
        NodeID dest = null;
        int mti = getMTI(f);
        byte[] data = f.getData();

        byte[] content = null;

        if ( ((mti&0x008) != 0) && (f.getNumDataElements() >= 2) ) {
            // addressed message 
            dest = map.getNodeID( ( (f.getElement(0) << 8) + (f.getElement(1) & 0xff) ) & 0xFFF );
            AccumulationMemo mnew = new AccumulationMemo(f.getHeader(), source, dest, data);
            // is header already in map?
            AccumulationMemo mold = accumulations.get(f.getHeader());
            if (mold == null) {
                // no - start accumulation
                accumulations.put(f.getHeader(),mnew);
                mold = mnew;
            } else {
                // combine data into old one
                byte[] newdata = new byte[mold.data.length+mnew.data.length-2];  // skip address
                System.arraycopy(mold.data, 0, newdata, 0, mold.data.length);
                System.arraycopy(mnew.data, 2, newdata, mold.data.length, mnew.data.length-2);
                mold.data = newdata;
            }
            // see if final bit active
            if ( (f.getElement(0) & 0x10 ) != 0) {
                // no, accumulate
                return retlist; // which is null right now
            } 
            // we're going to continue processing with the accumulated data
            data = mold.data;
            accumulations.remove(f.getHeader());

            content = new byte[data.length-2];
            System.arraycopy(data, 2, content, 0, content.length);
        }
        
        MessageTypeIdentifier value = MessageTypeIdentifier.get(mti);
        if (value == null) {
            // something bad happened
            String mtiString = "000"+Integer.toHexString(mti).toUpperCase();
            mtiString = mtiString.substring(mtiString.length()-3);
            System.out.println(" failed to parse MTI 0x"+mtiString);
            return retlist;  // nothing in it from this
        }
        
        switch (value) {
            case InitializationComplete: 
                retlist.add(new InitializationCompleteMessage(source));
                return retlist;
            case VerifyNodeIdGlobal:
                // check for content
                if (data.length >= 6) {
                    NodeID node = new NodeID(data);
                    retlist.add(new VerifyNodeIDNumberMessage(source, node));
                } else {
                    retlist.add(new VerifyNodeIDNumberMessage(source));
                }
                return retlist;
            case VerifiedNodeId: 
                retlist.add(new VerifiedNodeIDNumberMessage(source));
                return retlist;

            case OptionalInteractionRejected: {
                    int d2 = data.length >= 3 ? f.getElement(2) : 0;
                    int d3 = data.length >= 4 ? f.getElement(3) : 0;
                    int d4 = data.length >= 5 ? f.getElement(4) : 0;
                    int d5 = data.length >= 6 ? f.getElement(5) : 0;
                    int retmti = ((d2&0xff)<<8) | (d3&0xff);
                    int code = ((d4&0xff)<<8) | (d5&0xff);;
                    retlist.add(new OptionalIntRejectedMessage(source, dest,retmti,code));
                    return retlist;
                }
            case ProtocolSupportInquiry: 
                retlist.add(new ProtocolIdentificationRequestMessage(source, dest));
                return retlist;
            case ProtocolSupportReply: 
                retlist.add(new ProtocolIdentificationReplyMessage(source, dest, f.dataAsLong()));
                return retlist;
            case TractionControlRequest:
                retlist.add(new TractionControlRequestMessage(source, dest, content));
                return retlist;
            case TractionControlReply:
                retlist.add(new TractionControlReplyMessage(source, dest, content));
                return retlist;
            case TractionProxyRequest:
                retlist.add(new TractionProxyRequestMessage(source, dest, content));
                return retlist;
            case TractionProxyReply:
                retlist.add(new TractionProxyReplyMessage(source, dest, content));
                return retlist;
            case IdentifyConsumer:
                retlist.add(new IdentifyConsumersMessage(source, getEventID(f)));
                return retlist;
            case ConsumerIdentifiedUnknown:
            case ConsumerIdentifiedValid: 
            case ConsumerIdentifiedInvalid: 
                retlist.add(new ConsumerIdentifiedMessage(source, getEventID(f)));
                return retlist;
            case IdentifyProducer: 
                retlist.add(new IdentifyProducersMessage(source, getEventID(f)));
                return retlist;
            case ProducerIdentifiedUnknown: 
            case ProducerIdentifiedValid: 
            case ProducerIdentifiedInvalid: 
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
                retlist.add(new SimpleNodeIdentInfoReplyMessage(source, dest, content));
                return retlist;

            case DatagramReceivedOK: 
                retlist.add(new DatagramAcknowledgedMessage(source,dest));
                return retlist;
            case DatagramRejected: 
                retlist.add(new DatagramRejectedMessage(source,dest,(int)f.dataAsLong()));
                return retlist;
         // dph: add all stream messages reply and proceed.
            case StreamInitiateRequest:
                retlist.add(new StreamInitiateRequestMessage(source,dest,content[2]<<8+content[3],content[4], content[5]));
                return retlist;
            case StreamInitiateReply:
                retlist.add(new StreamInitiateRequestMessage(source,dest,content[2]<<8+content[3],content[4], content[5]));
                return retlist;
            // case StreamData is Format 7
            case StreamDataProceed:
                retlist.add(new StreamDataProceedMessage(source,dest,content[2], content[3]));
                return retlist;
            case StreamDataComplete:
                retlist.add(new StreamDataCompleteMessage(source,dest,content[2], content[3]));
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
        NodeID source = map.getNodeID(getSourceID(f));
        int bufSize = 64; // need to define this  !!!!!!!!!!!!!!!!!!!!!!!!!!
        List<Integer> list = streamData.get(source);
        if (list == null) {
            list = new ArrayList<Integer>();
        }
        int n = Math.min(bufSize, f.getNumDataElements());
        if(n < bufSize) {
            // won't fill buffer, so add all
            for (int i = 0; i < f.getNumDataElements(); i++) list.add(f.getElement(i));
            return null;
        } else {
            // got a full buffer, fill it and send it on
            for (int i = 0; i < n; i++) list.add(f.getElement(i));
            int[] data = new int[list.size()];
            for(int i=0; i<bufSize; i++) data[i] = list.get(i);
            List<Message> retlist = new java.util.ArrayList<Message>();
            NodeID dest = map.getNodeID( (f.getHeader() & 0x00FFF000) >> 12);
            retlist.add(new DatagramMessage(source, dest, data));
            // make a new List and fill it with the rest of received data
            list = new ArrayList<Integer>();
            for (int i=n; i<f.getNumDataElements(); i++) list.add(f.getElement(i));
            return retlist;
        }
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

        private void handleAddressedPayloadMessage(AddressedPayloadMessage msg, Connection sender) {
            byte[] payload = msg.getPayload();
            if (payload == null) {
                payload = new byte[0];
            }
            for (int i = 0; i < Math.max(1, payload.length); i += 6) {
                // Note that the order of these calls are carefully chosen so that internally
                // OpenLcbCanFrame does not overwrite parts of the payload with each other.
                OpenLcbCanFrame f = new OpenLcbCanFrame(map.getAlias(msg.getSourceNodeID()));
                f.setOpenLcbMTI(msg.getEMTI().mti());
                int thislen = Math.min(6, payload.length - i);
                byte[] data = new byte[thislen + 2];
                System.arraycopy(payload, i, data, 2, thislen);
                f.setData(data);
                f.setDestAlias(map.getAlias(msg.getDestNodeID()));
                f.setContinuation(i == 0, (i + 6 >= payload.length));
                retlist.add(f);
            }
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
            if (msg.getContent() != null) 
                f.setData(msg.getContent().getContents());
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

        @Override
        /**
         * Handle "Traction Control Request" message
         */
        public void handleTractionControlRequest(TractionControlRequestMessage msg, Connection sender) {
            handleAddressedPayloadMessage(msg, sender);
        }

        /**
         * Handle "Traction Control Reply" message
         */
        public void handleTractionControlReply(TractionControlReplyMessage msg, Connection sender) {
            handleAddressedPayloadMessage(msg, sender);
        }

        /**
         * Handle "Traction Proxy Request" message
         */
        public void handleTractionProxyRequest(TractionProxyRequestMessage msg, Connection sender) {
            handleAddressedPayloadMessage(msg, sender);
        }

        /**
         * Handle "Traction Proxy Reply" message
         */
        public void handleTractionProxyReply(TractionProxyReplyMessage msg, Connection sender) {
            handleAddressedPayloadMessage(msg, sender);
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
        //final int STREAMBUFFERSIZE 10;
        @Override
        public void handleStreamInitiateRequest(StreamInitiateRequestMessage msg, Connection sender){
            //defaultHandler(msg, sender);
            // dph
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setOpenLcbMTI(MessageTypeIdentifier.StreamInitiateRequest.mti());
            // dest(2), maxBuffer(2),flags(2),sourceStream, reserved
            f.setData(new byte[]{ (byte)0, (byte)0, 0, 64, 0, 0, msg.getSourceStreamID(), (byte)0 });
            f.setDestAlias(map.getAlias(msg.getDestNodeID()));
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            retlist.add(f);
        }
        /**
         * Handle "Stream Init Reply" message
         */
        @Override
        public void handleStreamInitiateReply(StreamInitiateReplyMessage msg, Connection sender){
            //defaultHandler(msg, sender);
            // dph
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setOpenLcbMTI(MessageTypeIdentifier.StreamInitiateReply.mti());
            // dest(2), maxBufferSize(2), flags(2),sourceStream, destinationStream
            f.setData(new byte[]{ (byte)0, (byte)0, 0, 64, msg.getSourceStreamID(), msg.getDestinationStreamID()  } );
            f.setDestAlias(map.getAlias(msg.getDestNodeID()));
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            retlist.add(f);
        }
        /**
         * Handle "Stream Data Send" message
         */
        @Override
        public void handleStreamDataSend(StreamDataSendMessage msg, Connection sender){
            // dph
            // must loop over data to send 8 byte chunks
            // do we need to check the destinationStreamID????????????????????
            int remains = msg.getData().length;
            int j = 0;
            // always sends at least one stream message, even with zero bytes  ???????
            do {
                int size = Math.min(8, remains);
                byte[] data = new byte[size];
                for (int i = 0; i<size; i++) {
                    data[i] = msg.getData()[j++];
                }
                
                OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
                f.setOpenLcbMTI(MessageTypeIdentifier.StreamDataSend.mti());
                f.setData(data);
                f.setDestAlias(map.getAlias(msg.getDestNodeID()));
                f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
                retlist.add(f);
                
                remains = remains - size;
            } while (remains > 0);
        }
        /**
         * Handle "Stream Data Proceed" message
         */
        @Override
        public void handleStreamDataProceed(StreamDataProceedMessage msg, Connection sender){
            // dph
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setOpenLcbMTI(MessageTypeIdentifier.StreamDataProceed.mti());
            // sourceStream, destinationStream, flags(2)
            f.setData(new byte[]{(byte)0, (byte)0, msg.getSourceStreamID(), msg.getDestinationStreamID(), 0, 0 });
            f.setDestAlias(map.getAlias(msg.getDestNodeID()));
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            retlist.add(f);
        }
        /**
         * Handle "Stream Data Complete" message
         */
        @Override
        public void handleStreamDataComplete(StreamDataCompleteMessage msg, Connection sender){
            // dph
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setOpenLcbMTI(MessageTypeIdentifier.StreamDataComplete.mti());
            // sourceStream, destinationStream, flags(2)
            f.setData(new byte[]{(byte)0, (byte)0, msg.getSourceStreamID(), msg.getDestinationStreamID(), 0, 0 });
            f.setDestAlias(map.getAlias(msg.getDestNodeID()));
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            retlist.add(f);
        }
    }
 }
