package org.openlcb;

import org.openlcb.messages.TractionControlReplyMessage;
import org.openlcb.messages.TractionControlRequestMessage;
import org.openlcb.messages.TractionProxyReplyMessage;
import org.openlcb.messages.TractionProxyRequestMessage;

/**
 * This class provides a basic double-dispatch mechanism for handling
 * messages.  OpenLCB messages in this implementation
 * are of separate type.  To simplify the operation of
 * specific Node implementions, this base class provides
 * a dispatch mechanism, in cooperation with the Message types,
 * to call a specific method in subclasses for each message type.
 * Using this, a subclass need not use e.g. a switch statement
 * or tentative casting to determine the type of message received.
 *
 * @author  Bob Jacobsen   Copyright 2009, 2012
 * @version $Revision$
 */
public class MessageDecoder extends AbstractConnection {
    
    /**
     * Process message.
     * <p>
     * Base implementation refers 
     * back to message to implement its
     * own specific type.
     * @param msg Input to be processed by type
     * @param sender Passed through for specific message processing,
     * e.g. to send a reply back to the originator
     */
    public void put(Message msg, Connection sender) {
        msg.applyTo(this, sender);
    }

    /**
     * Called by all the message-type-specific methods 
     * in this class.  Subclasses can use this method as
     * a "none of the above" processor, reducing the number of
     * methods they need to override.
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    protected void defaultHandler(Message msg, Connection sender) {
    }
    
    /**
     * Handle "Initialization Complete" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleInitializationComplete(InitializationCompleteMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Verified Node ID Number" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleVerifiedNodeIDNumber(VerifiedNodeIDNumberMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Verify Node ID Number" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleVerifyNodeIDNumber(VerifyNodeIDNumberMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Producer/Consumer Event Report" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Identify Consumers" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleIdentifyConsumers(IdentifyConsumersMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Consumer Identified" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleConsumerIdentified(ConsumerIdentifiedMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Consumer range Identified" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleConsumerRangeIdentified(ConsumerRangeIdentifiedMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Identify Producers" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleIdentifyProducers(IdentifyProducersMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Producer Identified" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleProducerIdentified(ProducerIdentifiedMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Producer Range Identified" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleProducerRangeIdentified(ProducerRangeIdentifiedMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Identify Event" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleIdentifyEvents(IdentifyEventsMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Learn Event" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleLearnEvent(LearnEventMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Datagram" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleDatagram(DatagramMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Datagram Rejected" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleDatagramRejected(DatagramRejectedMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Datagram Acknowledged" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleDatagramAcknowledged(DatagramAcknowledgedMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Stream Init Request" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleStreamInitiateRequest(StreamInitiateRequestMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Stream Init Reply" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleStreamInitiateReply(StreamInitiateReplyMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Stream Data Send" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleStreamDataSend(StreamDataSendMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Stream Data Proceed" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleStreamDataProceed(StreamDataProceedMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Stream Data Complete" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleStreamDataComplete(StreamDataCompleteMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Protocol Identification Request" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleProtocolIdentificationRequest(ProtocolIdentificationRequestMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Protocol Identification Reply" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleProtocolIdentificationReply(ProtocolIdentificationReplyMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Simple Node Ident Info Request" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleSimpleNodeIdentInfoRequest(SimpleNodeIdentInfoRequestMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Simple Node Ident Info Reply" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleSimpleNodeIdentInfoReply(SimpleNodeIdentInfoReplyMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Optional Interaction Rejected" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleOptionalIntRejected(OptionalIntRejectedMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }

    /**
     * Handle "Traction Control Request" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleTractionControlRequest(TractionControlRequestMessage msg, Connection sender) {
        defaultHandler(msg, sender);
    }

    /**
     * Handle "Traction Control Reply" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleTractionControlReply(TractionControlReplyMessage msg, Connection sender) {
        defaultHandler(msg, sender);
    }

    /**
     * Handle "Traction Proxy Request" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleTractionProxyRequest(TractionProxyRequestMessage msg, Connection sender) {
        defaultHandler(msg, sender);
    }

    /**
     * Handle "Traction Proxy Reply" message
     * @param msg       message to handle
     * @param sender    connection where it came from
     */
    public void handleTractionProxyReply(TractionProxyReplyMessage msg, Connection sender) {
        defaultHandler(msg, sender);
    }
}
