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
        //    Simple node message?
        //    Upper special bits (nibble)
        //    Priority Group
        //    Type number
        //    Modifier (0-3)
        //    Name
        
        InitializationComplete      ( false, false, false, 0, 0,  8, 0, "InitializationComplete"), 
        VerifyNodeIdAddressed       ( true,  false, false, 0, 1,  4, 0, "VerifyNodeIdAddressed"),
        VerifyNodeIdGlobal          ( false, false, true,  0, 1,  4, 0, "VerifyNodeIdGlobal"),
        VerifiedNodeId              ( false, false, true,  0, 0, 11, 0, "VerifiedNodeId"),
        OptionalInteractionRejected ( true,  false, false, 0, 0,  3, 0, "OptionalInteractionRejected"),
        TerminateDueToError         ( true,  false, false, 0, 0,  5, 0, "TerminateDueToError"),

        ProtocolSupportInquiry      ( true,  false, false, 0, 2,  1, 0, "ProtocolSupportInquiry"),
        ProtocolSupportReply        ( true,  false, false, 0, 1, 19, 0, "ProtocolSupportReply"),
        
        IdentifyConsumer            ( false, true,  true,  0, 2,  7, 0, "IdentifyConsumer"),
        ConsumerIdentifyRange       ( false, true,  false, 0, 1,  5, 0, "ConsumerIdentifyRange"),
        ConsumerIdentified          ( false, true,  false, 0, 1,  6, 0, "ConsumerIdentified"),
        
        IdentifyProducer            ( false, true,  true,  0, 2,  8, 0, "IdentifyProducer"),
        ProducerIdentifyRange       ( false, true,  false, 0, 1,  9, 0, "ProducerIdentifyRange"),
        ProducerIdentified          ( false, true,  false, 0, 1, 10, 0, "ProducerIdentified"),
        
        IdentifyEventsAddressed     ( true,  false, false, 0, 2, 11, 0, "IdentifyEventsAddressed"),
        IdentifyEventsGlobal        ( false, false, true,  0, 2, 11, 0, "IdentifyEventsGlobal"),
        
        LearnEvent                  ( false, true,  true,  0, 1,  12, 0, "LearnEvent"),
        ProducerConsumerEventReport ( false, true,  true,  0, 1,  13, 0, "ProducerConsumerEventReport"),
        
        SimpleNodeIdentInfoRequest  ( true,  false, false, 0, 3,  15, 0, "SimpleNodeIdentInfoRequest"),
        SimpleNodeIdentInfoReply    ( true,  false, false, 0, 2,  16, 0, "SimpleNodeIdentInfoReply"),

        Datagram                    ( true,  false, false, 1, 3,   2, 0, "Datagram"),
        DatagramReceivedOK          ( true,  false, false, 0, 2,  17, 0, "DatagramReceivedOK"),
        DatagramRejected            ( true,  false, false, 0, 2,  18, 0, "DatagramRejected"),

        StreamInitiateRequest       ( true,  false, false, 0, 3,   6, 0, "StreamInitiateRequest"),
        StreamInitiateReply         ( true,  false, false, 0, 2,   3, 0, "StreamInitiateReply"),
        StreamDataSend              ( true,  false, false, 1, 3,  28, 0, "StreamDataSend"),
        StreamDataProceed           ( true,  false, false, 0, 2,   4, 0, "StreamDataProceed"),
        StreamDataComplete          ( true,  false, false, 0, 2,   5, 0, "StreamDataComplete");
       
        private static java.util.Map<Integer, MessageTypeIdentifier> mapping;
        private static  java.util.Map<Integer, MessageTypeIdentifier> getMap() {
            if (mapping == null)
                mapping = new java.util.HashMap<Integer, MessageTypeIdentifier>();
            return mapping;
        }
        
        MessageTypeIdentifier(boolean addressed, boolean hasEventID, boolean isSimpleModeMessage, 
                                int upper, int priorityGroup, int typeNumber, int modifier, String name) {
            this.addressed = addressed;
            this.hasEventID = hasEventID;
            this.modifier = modifier;
            this.isSimpleModeMessage = isSimpleModeMessage;
            this.priorityGroup = priorityGroup;
            this.typeNumber = typeNumber;
            this.upper = upper;
            this.name = name;
            
            getMap().put(Integer.valueOf(mti()), this);
        }
        
        boolean addressed;
        boolean hasEventID;
        int     modifier;
        boolean isSimpleModeMessage;
        int     priorityGroup;
        int     typeNumber;
        int     upper;
        
        String name;
        
        public String toString() {
            return name;
        }

        public int mti() {
            int retval = 0x0000;

            retval = retval | (upper << 12);
            retval = retval | (priorityGroup << 10);
            retval = retval | (typeNumber << 5);

            if (addressed)           retval = retval | 0x0008;
            if (hasEventID)          retval = retval | 0x0004;
            if (modifier != 0)       retval = retval | modifier;
            if (isSimpleModeMessage) retval = retval | 0x0010;
                    
            return retval;
        }
        
        /**
         * Provide the enum object matching a particular MTI value
         */
        public static MessageTypeIdentifier get(int mti) {
            return mapping.get(Integer.valueOf(mti));
        }
        
}
