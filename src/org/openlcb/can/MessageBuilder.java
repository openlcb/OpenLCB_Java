package org.openlcb.can;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openlcb.*;
import org.openlcb.implementations.DatagramUtils;
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
 * @author  David Harris
 */
public class MessageBuilder implements AliasMap.Watcher {

    private final static Logger logger = Logger.getLogger(MessageBuilder.class.getName());
    /**
     * The provided AliasMap will be updated
     * as inbound frames are processed.
     * @param map    the alias map in use by the interface
     */
    public MessageBuilder(AliasMap map) {
        this.map = map;
        map.addWatcher(this);
    }

    AliasMap map;


    private static class BlockedMessage {
        BlockedMessage(Message m) {
            message = m;
        }
        /// Addressed message that was sent earlier.
        Message message;
        /// When this message was sent.
        long timestampMsec;

        /// Notifies that this message is now sent. This is not used yet; will be necessary to
        // implement a linked list sorted by timestamp.
        void release() {}
    }

    /// Stores addressed messages where we don't know the destination alias to.
    private final Map<NodeID, List<BlockedMessage> > blockedMessages = new HashMap<>();

    /// Stores addressed messages that we recently got the destination alias.
    private List<Message> unblockedMessages = new ArrayList<>();

    /// This value is atomically set to true if we found new unblocked messages. Can be reset by
    // a call to foundUnblockedMessage.
    private boolean haveUnblockedMessages = false;

    /// Callback from the alias map when a new alias is inserted. This is used to determine if we
    // are holding on to some messages that are unsent due to missing destination alias.
    @Override
    public void aliasAdded(NodeID id, int alias) {
        if (alias <= 0) {
            // Ignores invalid aliases.
            return;
        }
        synchronized (blockedMessages) {
            List<BlockedMessage> l = blockedMessages.get(id);
            if (l == null) {
                return;
            }
            for (BlockedMessage bm: l) {
                unblockedMessages.add(bm.message);
                haveUnblockedMessages = true;
            }
            l.clear();
        }
    }

    /// @return true exactly once after we found some unblocked messages.
    boolean foundUnblockedMessage() {
        synchronized (blockedMessages) {
            boolean ret = haveUnblockedMessages;
            haveUnblockedMessages = false;
            return ret;
        }
    }

    /**
     * Accept a frame, and convert to
     * a standard OpenLCB Message object.
     *
     * The returned Message is fully initialized
     * with content, and the original frame can
     * be dropped.
     *
     * The List is always returned, even if empty.
     * @param f    frame that came
     * @return messages decoded from the arriving frame and internal state
     */
    public List<Message> processFrame(CanFrame f) {
        // check for special cases first
        if ( (f.getHeader() & 0x08000000) != 0x08000000 ) return null;  // not OpenLCB frame

        // break into types
        int format = ( f.getHeader() & 0x07000000 ) >> 24;

        switch (format) {
            case 0:
                return processFormat0(f);   // reserved
            case 1:
                return processFormat1(f);   // global/addressed
            case 2:
                return processFormat2(f);   // datagram only
            case 3:
                return processFormat3(f);   // datagram first
            case 4:
                return processFormat4(f);   // datagram middle
            case 5:
                return processFormat5(f);   // datagram last
            case 6:
                return processFormat6(f);   // reserved
            case 7:
                return processFormat7(f);   // stream data
            default:  // should not happen
                return null;
        }
    }

    HashMap<NodeID, List<Integer>> datagramData = new HashMap<NodeID, List<Integer>>();

    HashMap<NodeID, List<Integer>> streamData = new HashMap<NodeID, List<Integer>>();

    HashMap<NodeID, List<Byte>> pcerData = new HashMap<NodeID, List<Byte>>();
    HashMap<NodeID, EventID> pcerEventID = new HashMap<NodeID, EventID>();

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
        // global or addressed MTI
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
            logger.log(Level.SEVERE, "Failed to parse MTI 0x{0}", mtiString);

            // return internal-only message
            retlist.add(new UnknownMtiMessage(source, dest, mti, content ) );
            return retlist;
        }

        switch (value) {
            case InitializationComplete:
                retlist.add(new InitializationCompleteMessage(source));
                return retlist;

            case InitializationCompleteSimple:
                retlist.add(new InitializationCompleteSimpleMessage(source));
                return retlist;

            case VerifyNodeIdAddressed:
                // check for content
                if (data.length >= 6) {
                    NodeID node = new NodeID(data);
                    retlist.add(new VerifyNodeIDNumberAddressedMessage(source, dest, node));
                } else {
                    retlist.add(new VerifyNodeIDNumberAddressedMessage(source, dest));
                }
                return retlist;
            
            case VerifyNodeIdGlobal:
                // check for content
                if (data.length >= 6) {
                    NodeID node = new NodeID(data);
                    retlist.add(new VerifyNodeIDNumberGlobalMessage(source, node));
                } else {
                    retlist.add(new VerifyNodeIDNumberGlobalMessage(source));
                }
                return retlist;
                            
            case VerifiedNodeId:
                retlist.add(new VerifiedNodeIDNumberMessage(source));
                return retlist;

            case VerifiedNodeIdSimple:
                retlist.add(new VerifiedNodeIDNumberSimpleMessage(source));
                return retlist;

            case OptionalInteractionRejected: {
                    int d2 = data.length >= 3 ? f.getElement(2) : 0;
                    int d3 = data.length >= 4 ? f.getElement(3) : 0;
                    int d4 = data.length >= 5 ? f.getElement(4) : 0;
                    int d5 = data.length >= 6 ? f.getElement(5) : 0;

                    int code = ((d2&0xff)<<8) | (d3&0xff);
                    int retmti = ((d4&0xff)<<8) | (d5&0xff);;

                    retlist.add(new OptionalIntRejectedMessage(source, dest,retmti,code));
                    return retlist;
                }
            case ProtocolSupportInquiry:
                retlist.add(new ProtocolIdentificationRequestMessage(source, dest));
                return retlist;
            case ProtocolSupportReply:
                long flags = f.dataAsLong();
                flags = flags << (8 * Math.max(0, 8-f.getNumDataElements()));
                retlist.add(new ProtocolIdentificationReplyMessage(source, dest, flags));
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
            case ConsumerRangeIdentified:
                retlist.add(new ConsumerRangeIdentifiedMessage(source, getEventID(f)));
                return retlist;
            case ConsumerIdentifiedUnknown:
                retlist.add(new ConsumerIdentifiedMessage(source, getEventID(f), EventState.Unknown));
                return retlist;
            case ConsumerIdentifiedValid:
                retlist.add(new ConsumerIdentifiedMessage(source, getEventID(f), EventState.Valid));
                return retlist;
            case ConsumerIdentifiedInvalid:
                retlist.add(new ConsumerIdentifiedMessage(source, getEventID(f), EventState.Invalid));
                return retlist;
            case IdentifyProducer:
                retlist.add(new IdentifyProducersMessage(source, getEventID(f)));
                return retlist;
            case ProducerRangeIdentified:
                retlist.add(new ProducerRangeIdentifiedMessage(source, getEventID(f)));
                return retlist;
            case ProducerIdentifiedUnknown:
                retlist.add(new ProducerIdentifiedMessage(source, getEventID(f), EventState.Unknown));
                return retlist;
            case ProducerIdentifiedValid:
                retlist.add(new ProducerIdentifiedMessage(source, getEventID(f), EventState.Valid));
                return retlist;
            case ProducerIdentifiedInvalid:
                retlist.add(new ProducerIdentifiedMessage(source, getEventID(f), EventState.Invalid));
                return retlist;
            case ProducerConsumerEventReport:
                retlist.add(new ProducerConsumerEventReportMessage(source, getEventID(f)));
                return retlist;
            case PCERfirst:
                receivedPCERfirst(source, f);
                return retlist;
            case PCERmiddle:
                receivedPCERmiddle(source, f);
                return retlist;
            case PCERlast:
                retlist.add(receivedPCERlast(source, f));
                return retlist;
                
            case IdentifyEventsAddressed:
                retlist.add(new IdentifyEventsAddressedMessage(source, dest));
                return retlist;
            case IdentifyEventsGlobal:
                retlist.add(new IdentifyEventsGlobalMessage(source));
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
                if (content != null && content.length > 0) {
                    retlist.add(new DatagramAcknowledgedMessage(source, dest, DatagramUtils
                            .byteToInt(content[0])));
                } else {
                    retlist.add(new DatagramAcknowledgedMessage(source, dest));
                }
                return retlist;
            case DatagramRejected:
                retlist.add(new DatagramRejectedMessage(source,dest,(int)f.dataAsLong()));
                return retlist;
            // add all stream messages reply and proceed.
            case StreamInitiateRequest:
                retlist.add(new StreamInitiateRequestMessage(source,dest,Utilities.NetworkToHostUint16(content, 2),content[4],
                        (content.length > 5 ? content[5] : -1)));
                return retlist;
            case StreamInitiateReply:
                retlist.add(new StreamInitiateReplyMessage(source,dest,Utilities.NetworkToHostUint16(content, 0),content[4], content[5]));
                return retlist;
            // case StreamData is Format 7
            case StreamDataProceed:
                retlist.add(new StreamDataProceedMessage(source,dest,content[2], content[3]));
                return retlist;
            case StreamDataComplete:
                retlist.add(new StreamDataCompleteMessage(source,dest,content.length > 2 ?
                        content[2] : -1, content.length > 3 ? content[3] : -1));
                return retlist;

            default:
                logger.warning(String.format(" received known but unhandled MTI 0x%03X: %s", mti, value.toString()));
                
                // return internal-only message
                retlist.add(new UnknownMtiMessage(source, dest, mti, content ) );
                return retlist;
        }
    }

    void receivedPCERfirst(NodeID source, CanFrame f) {
        // PCER first-segment
        List<Byte> list = pcerData.get(source);
        if (list == null) {
            list = new ArrayList<Byte>();
            pcerData.put(source, list);
        } else {
            logger.warning("PCER already in process for only-segment");
        }
        EventID eid = getEventID(f);
        pcerEventID.put(source, eid);
        return;
    }

    void receivedPCERmiddle(NodeID source, CanFrame f) {
        // PCER middle-segment
        List<Byte> list = pcerData.get(source);
        if (list == null) {
            logger.warning("PCER not started for middle segment");
            list = new ArrayList<Byte>();
            pcerData.put(source, list);
        }
        for (int i = 0; i < f.getNumDataElements(); i++) {
            list.add((byte)(0xFF & f.getElement(i)) );
        }
        return;
    }

    Message receivedPCERlast(NodeID source, CanFrame f) {
        // datagram last
        List<Byte> list = pcerData.get(source);
        if (list == null) {
            list = new ArrayList<Byte>();
        }
        for (int i = 0; i < f.getNumDataElements(); i++) {
            list.add((byte)(0xFF & f.getElement(i)) );
        }

        pcerData.put(source, null); // not accumulating any more
        
        EventID eID = pcerEventID.get(source);

        return new ProducerConsumerEventReportMessage(source, eID, list);
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
        // @todo need to define this  !!!!!!!!!!!!!!!!!!!!!!!!!!
        int bufSize = 64;
        int destID = f.getElement(0);
        // @todo support more than one stream per destination, use destID as key.
        List<Integer> list = streamData.get(source);
        if (list == null) {
            list = new ArrayList<Integer>();
        }
        int n = Math.min(bufSize, f.getNumDataElements());
        if(n < bufSize) {
            // won't fill buffer, so add all
            for (int i = 1; i < f.getNumDataElements(); i++) list.add(f.getElement(i));
            return null;
        } else {
            // got a full buffer, fill it and send it on
            for (int i = 1; i < n; i++) list.add(f.getElement(i));
            int[] data = new int[list.size()];
            for(int i=0; i<bufSize; i++) data[i] = list.get(i);
            List<Message> retlist = new java.util.ArrayList<Message>();
            NodeID dest = map.getNodeID( (f.getHeader() & 0x00FFF000) >> 12);
            //retlist.add(new DatagramMessage(source, dest, data));
            retlist.add(new StreamDataSendMessage(source, dest, (byte)destID, data));
            // make a new List and fill it with the rest of received data
            list = new ArrayList<Integer>();
            for (int i=n; i<f.getNumDataElements(); i++) list.add(f.getElement(i));
            return retlist;
        }
        // @todo the list variable needs to be saved into the streamData map, otherwise we lose
        // the accumulated bytes.
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
     * @param msg    OpenLCB Message object to send
     * @return CAN frames (one or more) representing that message
     */
    public List<OpenLcbCanFrame> processMessage(Message msg) {
        // Checks and flushes unblocked messages first.
        List<Message> pending = null;
        synchronized (blockedMessages) {
            if (!unblockedMessages.isEmpty()) {
                pending = unblockedMessages;
                unblockedMessages = new ArrayList<>();
            }
        }
        List<OpenLcbCanFrame> r = null;
        if (pending != null) {
            r = new ArrayList<>();
            for (Message m : pending) {
                FrameBuilder f = new FrameBuilder();
                r.addAll(f.convert(m));
            }
        }

        // Processes the new message.
        FrameBuilder f = new FrameBuilder();
        if (r == null) {
            return f.convert(msg);
        } else {
            r.addAll(f.convert(msg));
            return r;
        }
    }

    /// @return a message that can be enqueued but does not turn into any outgoing frame. The
    // interface can use this message to wake up its internal threads when we detected that
    // blocked messages need to be sent out.
    public Message getTriggerMessage() {
        return new NullMessage();
    }

    /// Message that we can enqueue to wake up the outgoing frame builder.
    private class NullMessage extends Message {
        @Override
        public int getMTI() {
            return 0;
        }
    };

    private class FrameBuilder extends org.openlcb.MessageDecoder {
        /**
         * Verifies that we know the destination alias for an addressed message. If it is not
         * known, then generates a lookup frame, and enqueues the message for a later send.
         * @param m message to send.
         * @return true if this message can be sent. False if this message was enqueued for a
         * later send instead.
         */
        private boolean checkForDestinationAndQueue(Message m) {
            if (!(m instanceof AddressedMessage)) {
                // not addressed -> send immediately
                return true;
            }
            AddressedMessage am = (AddressedMessage)m;
            if (map.getAlias(am.getDestNodeID()) > 0) {
                // Have destination alias -> send immediately
                return true;
            }
            // We don't know the destination alias.

            // Sends a node id verify message.
            VerifyNodeIDNumberGlobalMessage om = new VerifyNodeIDNumberGlobalMessage(m.getSourceNodeID(),
                    ((AddressedMessage) m).getDestNodeID());
            handleVerifyNodeIDNumberGlobal(om, null);

            // Enqueues the outgoing message.
            synchronized (blockedMessages) {
                List<BlockedMessage> bl = blockedMessages.get(am.getDestNodeID());
                if (bl == null) {
                    bl = new ArrayList<>();
                    blockedMessages.put(am.getDestNodeID(), bl);
                }

                bl.add(new BlockedMessage(m));
            }

            // Tells the caller to skip sending this message now.
            return false;
        }

        /**
         * Catches messages that are not explicitly
         * handled and throws an error
         */
        @Override
        protected void defaultHandler(Message msg, Connection sender) {
            if (msg instanceof NullMessage) {
                // This should not turn into any outgoing frames.
                return;
            }
            if (msg instanceof AddressedPayloadMessage) {
                handleAddressedPayloadMessage((AddressedPayloadMessage)msg, sender);
            } else {
                // all global messages handled explicitly
                throw new java.lang.NoSuchMethodError("no handler for Message: " + msg.toString());
            }
        }

        List<OpenLcbCanFrame> retlist;

        List<OpenLcbCanFrame> convert(Message msg) {
            retlist = new java.util.ArrayList<OpenLcbCanFrame>();

            // Uses the double dispatch mechanism built into Message
            put(msg, null);  // no Connection needed

            return retlist;
        }

        private void handleAddressedPayloadMessage(AddressedPayloadMessage msg, Connection sender) {
            if (!checkForDestinationAndQueue(msg)) {
                return;
            }
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
        public void handleVerifyNodeIDNumberGlobal(VerifyNodeIDNumberGlobalMessage msg, Connection sender){
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setVerifyNID(msg.getSourceNodeID());
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            if (msg.getContent() != null)
                f.setData(msg.getContent().getContents());
            retlist.add(f);
        }

        /**
         * Handle "Producer/Consumer Event Report" message
         */
        @Override
        public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg, Connection sender){
            int payloadSize = msg.getPayloadSize();
            if (payloadSize == 0) {
                OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
                f.setPCEventReport(msg.getEventID());
                f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
                retlist.add(f);
            } else {
                // longer than 0, have to loop and create multiples.
                // first frame with EID
                int frameLength = Math.min(8, msg.getPayloadSize());
                OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
                f.setPCEventReport(msg.getEventID(), MessageTypeIdentifier.PCERfirst);
                f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
                retlist.add(f);                
                
                
                // rest of frames
                ArrayList<Byte> data = new ArrayList<Byte>(msg.getPayloadList());
                
                while (data.size() > 0) {
                    int loopLength = Math.min(8, data.size());
                    byte[] array = new byte[loopLength];
                    for (int i = 0; i<loopLength; i++) {
                        array[i] = data.remove(0);
                    }
                    if (data.size() == 0) {
                        // done
                        f = new OpenLcbCanFrame(0x00);
                        f.setPCEventReport(MessageTypeIdentifier.PCERlast, array);
                        f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
                        retlist.add(f);
                        return;
                    } else {
                        // not done
                        f = new OpenLcbCanFrame(0x00);
                        f.setPCEventReport(MessageTypeIdentifier.PCERmiddle, array);
                        f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
                        retlist.add(f);
                        // and repeat
                    }
                }
                logger.warning("should have returned within loop");
            }
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
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setOpenLcbMTI(msg.getEventState().getConsumerIdentifierMti().mti());
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            f.loadFromEid(msg.getEventID());
            retlist.add(f);
        }

        @Override
        public void handleConsumerRangeIdentified(ConsumerRangeIdentifiedMessage msg, Connection
                sender) {
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setOpenLcbMTI(MessageTypeIdentifier.ConsumerRangeIdentified.mti());
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            f.loadFromEid(msg.getEventID());
            retlist.add(f);
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

        @Override
        public void handleProducerRangeIdentified(ProducerRangeIdentifiedMessage msg, Connection
                sender) {
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setOpenLcbMTI(MessageTypeIdentifier.ProducerRangeIdentified.mti());
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            f.loadFromEid(msg.getEventID());
            retlist.add(f);
        }

        /**
         * Handle "Producer Identified" message
         */
        @Override
        public void handleProducerIdentified(ProducerIdentifiedMessage msg, Connection sender){
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            f.setOpenLcbMTI(msg.getEventState().getProducerIdentifierMti().mti());
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            f.loadFromEid(msg.getEventID());
            retlist.add(f);
        }
        /**
         * Handle "Identify Event (Addressed)" message
         */
        @Override
        public void handleIdentifyEventsAddressed(IdentifyEventsAddressedMessage msg,
                                                  Connection sender){
            OpenLcbCanFrame f = new OpenLcbCanFrame(0x00);
            if (msg.getDestNodeID() != null) {
                f.setOpenLcbMTI(MessageTypeIdentifier.IdentifyEventsAddressed.mti());
                f.setDestAlias(map.getAlias(msg.getDestNodeID()));
            } else {
                f.setOpenLcbMTI(MessageTypeIdentifier.IdentifyEventsGlobal.mti());
            }
            f.setSourceAlias(map.getAlias(msg.getSourceNodeID()));
            retlist.add(f);
        }

        /**
         * Handle "Identify Event (Global)" message
         */
        @Override
        public void handleIdentifyEventsGlobal(IdentifyEventsGlobalMessage msg, Connection sender) {
            OpenLcbCanFrame f = new OpenLcbCanFrame(map.getAlias(msg.getSourceNodeID()));
            f.setOpenLcbMTI(MessageTypeIdentifier.IdentifyEventsGlobal.mti());
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
         * Handle "Datagram" message
         */
        @Override
        public void handleDatagram(DatagramMessage msg, Connection sender){
            if (!checkForDestinationAndQueue(msg)) {
                return;
            }
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
         * Handle "Stream Data Send" message
         */
        @Override
        public void handleStreamDataSend(StreamDataSendMessage msg, Connection sender){
            if (!checkForDestinationAndQueue(msg)) {
                return;
            }
            // must loop over data to send 8 byte chunks
            int remains = msg.getData().length;
            int j = 0;
            // always sends at least one stream message, even with zero bytes  ???????
            do {
                int size = Math.min(7, remains);
                byte[] data = new byte[size+1];
                data[0] = msg.getDestinationStreamID();
                for (int i = 0; i<size; i++) {
                    data[i+1] = (byte)msg.getData()[j++];
                }

                OpenLcbCanFrame f = new OpenLcbCanFrame(map.getAlias(msg.getSourceNodeID()));
                f.setStream(data, map.getAlias(msg.getDestNodeID()));
                retlist.add(f);

                remains = remains - size;
            } while (remains > 0);
        }
    }
 }
