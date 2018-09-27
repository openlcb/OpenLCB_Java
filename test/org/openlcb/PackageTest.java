package org.openlcb;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        UtilitiesTest.class,
        VersionTest.class,
        EventIDTest.class,
        NodeIDTest.class,
        MessageTypeIdentifierTest.class,
        MessageTest.class,
        MessageDecoderTest.class,
        NodeTest.class,
        // specific message types (uses Node, IDs)
        InitializationCompleteMessageTest.class,
        OptionalIntRejectedMessageTest.class,
        VerifiedNodeIDNumberMessageTest.class,
        VerifyNodeIDNumberMessageTest.class,
        IdentifyProducersMessageTest.class,
        ProducerIdentifiedMessageTest.class,
        IdentifyConsumersMessageTest.class,
        ConsumerIdentifiedMessageTest.class,
        IdentifyEventsMessageTest.class,
        ProducerConsumerEventReportMessageTest.class,
        LearnEventMessageTest.class,
        DatagramMessageTest.class,
        ProtocolIdentificationRequestMessageTest.class,
        ProtocolIdentificationReplyMessageTest.class,
        ProtocolIdentificationTest.class,
        SimpleNodeIdentInfoRequestMessageTest.class,
        SimpleNodeIdentInfoReplyMessageTest.class,
        SimpleNodeIdentTest.class,
        ConfigurationPortalTest.class,
        SingleLinkNodeTest.class,
        GatewayTest.class,
        ThrottleTest.class,
        MimicNodeStoreTest.class,
        LoaderClientTest.class,
        CommonIdentifiersTest.class,       
        DatagramAcknowledgedMessageTest.class,
        DatagramRejectedMessageTest.class,  
        DefaultPropertyListenerSupportTest.class,	
        OlcbInterfaceTest.class, 
        StreamDataCompleteMessageTest.class,       
        StreamDataProceedMessageTest.class,       
        StreamDataSendMessageTest.class,    
        StreamInitiateReplyMessageTest.class,       
        StreamInitiateRequestMessageTest.class,  
        // test implementation classes
        org.openlcb.implementations.PackageTest.class,
        org.openlcb.messages.PackageTest.class,
        org.openlcb.swing.PackageTest.class,
        org.openlcb.cdi.PackageTest.class,
        org.openlcb.hub.PackageTest.class,       
        org.openlcb.protocols.PackageTest.class,       
        // test CAN classes
        org.openlcb.can.PackageTest.class
})
/**
 * @author  Bob Jacobsen   Copyright 2009, 2012
 */
public class PackageTest {
}
