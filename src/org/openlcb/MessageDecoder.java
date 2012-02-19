package org.openlcb;

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
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class MessageDecoder implements Connection {
    
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
     */
    protected void defaultHandler(Message msg, Connection sender) {
    }
    
    /**
     * Handle "Initialization Complete" message
     */
    public void handleInitializationComplete(InitializationCompleteMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Verified Node ID Number" message
     */
    public void handleVerifiedNodeIDNumber(VerifiedNodeIDNumberMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Verify Node ID Number" message
     */
    public void handleVerifyNodeIDNumber(VerifyNodeIDNumberMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Producer/Consumer Event Report" message
     */
    public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg, Connection sender){
        defaultHandler(msg, sender);
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
    /**
     * Handle "Protocol Identification Request" message
     */
    public void handleProtocolIdentificationRequest(ProtocolIdentificationRequestMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Protocol Identification Reply" message
     */
    public void handleProtocolIdentificationReply(ProtocolIdentificationReplyMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Simple Node Ident Info Request" message
     */
    public void handleSimpleNodeIdentInfoRequest(SimpleNodeIdentInfoRequestMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
    /**
     * Handle "Simple Node Ident Info Reply" message
     */
    public void handleSimpleNodeIdentInfoReply(SimpleNodeIdentInfoReplyMessage msg, Connection sender){
        defaultHandler(msg, sender);
    }
}
