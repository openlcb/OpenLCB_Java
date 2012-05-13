// MessageTypeIdentifier.java

package org.openlcb;

import java.util.ArrayList;
import java.util.List;

/**
 * Message Type Identifiers
 *
 * Central place to carry the (only) numerical values for 
 * Message Type Identifiers
 *
 * @see             "http://www.openlcb.org/trunk/specs/"
 * @author			Bob Jacobsen   Copyright (C) 2012
 * @version			$Revision: 18542 $
 *
 */
public enum MessageTypeIdentifier {

        // Arguments are 
        //    addressed?
        //    contains Event ID?
        //    Flags in Header?
        //    Simple node message?
        //    Priority Group
        //    Type number
        
        InitializationComplete      ( false, false, false, false, 0x0, 0x08, "InitializationComplete"), 
        VerifyNodeIdAddressed       ( true,  false, false, false, 0x0, 0x0A, "VerifyNodeIdAddressed"),
        VerifyNodeIdGlobal          ( false, false, false, true,  0x0, 0x0A, "VerifyNodeIdGlobal"),
        VerifiedNodeId              ( false, false, false, true,  0x0, 0x0B, "VerifiedNodeId"),
        OptionalInteractionRejected ( true,  false, false, false, 0x0, 0x0C, "OptionalInteractionRejected"),
        TerminateDueToError         ( true,  false, false, false, 0x0, 0x0D, "TerminateDueToError"),

        ProtocolSupportInquiry      ( true,  false, false, false, 0x1, 0x0E, "ProtocolSupportInquiry"),
        ProtocolSupportReply        ( true,  false, false, false, 0x1, 0x0F, "ProtocolSupportReply"),
        
        IdentifyConsumer            ( false, true,  false, true,  0x1, 0x04, "IdentifyConsumer"),
        ConsumerIdentifyRange       ( false, true,  false, false, 0x1, 0x05, "ConsumerIdentifyRange"),
        ConsumerIdentified          ( false, true,  true,  false, 0x1, 0x06, "ConsumerIdentified"),
        
        IdentifyProducer            ( false, true,  false, true,  0x1, 0x08, "IdentifyProducer"),
        ProducerIdentifyRange       ( false, true,  false, false, 0x1, 0x09, "ProducerIdentifyRange"),
        ProducerIdentified          ( false, true,  true,  false, 0x1, 0x0A, "ProducerIdentified"),
        
        IdentifyEventsAddressed     ( true,  false, false, false, 0x1, 0x0B, "IdentifyEventsAddressed"),
        IdentifyEventsGlobal        ( false, false, false, true,  0x1, 0x0B, "IdentifyEventsGlobal"),
        
        LearnEvent                  ( false, true,  false, true,  0x1, 0x0C, "LearnEvent"),
        ProducerConsumerEventReport ( false, true,  false, true,  0x1, 0x0D, "ProducerConsumerEventReport"),
        
        SimpleNodeIdentInfoRequest  ( true,  false, false, false, 0x2, 0x12, "SimpleNodeIdentInfoRequest"),
        SimpleNodeIdentInfoReply    ( true,  false, false, false, 0x2, 0x13, "SimpleNodeIdentInfoReply"),

        Datagram                    ( true,  false, false, false, 0x2, 0x00, "Datagram"),
        DatagramReceivedOK          ( true,  false, false, false, 0x2, 0x0C, "DatagramReceivedOK"),
        DatagramRejected            ( true,  false, false, false, 0x2, 0x0D, "DatagramRejected"),

        StreamInitiateRequest       ( true,  false, false, false, 0x2, 0x0E, "StreamInitiateRequest"),
        StreamInitiateReply         ( true,  false, false, false, 0x2, 0x0F, "StreamDataSend"),
        StreamDataSend              ( true,  false, false, false, 0x3, 0x09, "StreamDataSend"),
        StreamDataProceed           ( true,  false, false, false, 0x3, 0x0A, "StreamDataProceed"),
        StreamDataComplete          ( true,  false, false, false, 0x3, 0x0B, "StreamDataComplete");
       
        MessageTypeIdentifier(boolean addressed, boolean hasEventID, boolean hasFlagsInHeader,
                              boolean isSimpleModeMessage, int priorityGroup, int typeNumber, String name) {
            this.addressed = addressed;
            this.hasEventID = hasEventID;
            this.hasFlagsInHeader = hasFlagsInHeader;
            this.isSimpleModeMessage = isSimpleModeMessage;
            this.priorityGroup = priorityGroup;
            this.typeNumber = typeNumber;
            this.name = name;
        }
        
        boolean addressed;
        boolean hasEventID;
        boolean hasFlagsInHeader;
        boolean isSimpleModeMessage;
        int     priorityGroup;
        int     typeNumber;
        
        String name;
        
        public String toString() {
            return name;
        }

        public long mti() {
            long retval = 0x1000;
            if (addressed)           retval = retval | 0x0004;
            if (hasEventID)          retval = retval | 0x0002;
            if (hasFlagsInHeader)    retval = retval | 0x0001;
            if (isSimpleModeMessage) retval = retval | 0x0800;
            
            retval = retval | (priorityGroup << 4+5);
            retval = retval | (typeNumber << 4);
            
            return retval;
        }
}
