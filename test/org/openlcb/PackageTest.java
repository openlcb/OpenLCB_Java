package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.JUnit4TestAdapter;

/**
 * @author  Bob Jacobsen   Copyright 2009, 2012
 */
public class PackageTest extends TestCase {
    public void testStart() {
    }
    
    // from here down is testing infrastructure
    
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class);

        suite.addTest(UtilitiesTest.suite());
        suite.addTest(VersionTest.suite());

        suite.addTest(EventIDTest.suite());
        suite.addTest(NodeIDTest.suite());

        suite.addTest(MessageTypeIdentifierTest.suite());

        suite.addTest(MessageTest.suite());

        suite.addTest(MessageDecoderTest.suite());
        suite.addTest(NodeTest.suite());

        // specific message types (uses Node, IDs)
        suite.addTest(InitializationCompleteMessageTest.suite());

        suite.addTest(OptionalIntRejectedMessageTest.suite());

        suite.addTest(VerifiedNodeIDNumberMessageTest.suite());
        suite.addTest(VerifyNodeIDNumberMessageTest.suite());

        suite.addTest(IdentifyProducersMessageTest.suite());
        suite.addTest(ProducerIdentifiedMessageTest.suite());
        suite.addTest(IdentifyConsumersMessageTest.suite());
        suite.addTest(ConsumerIdentifiedMessageTest.suite());
        suite.addTest(IdentifyEventsMessageTest.suite());
        suite.addTest(ProducerConsumerEventReportMessageTest.suite());

        suite.addTest(LearnEventMessageTest.suite());

        suite.addTest(DatagramMessageTest.suite());

        suite.addTest(ProtocolIdentificationRequestMessageTest.suite());
        suite.addTest(ProtocolIdentificationReplyMessageTest.suite());
        suite.addTest(ProtocolIdentificationTest.suite());
        
        suite.addTest(SimpleNodeIdentInfoRequestMessageTest.suite());
        suite.addTest(SimpleNodeIdentInfoReplyMessageTest.suite());
        suite.addTest(SimpleNodeIdentTest.suite());

        suite.addTest(ConfigurationPortalTest.suite());

        suite.addTest(SingleLinkNodeTest.suite());

        suite.addTest(GatewayTest.suite());

        suite.addTest(ThrottleTest.suite());

        suite.addTest(MimicNodeStoreTest.suite());
        
        suite.addTest(LoaderClientTest.suite());
        suite.addTest(new JUnit4TestAdapter(CommonIdentifiersTest.class));       
        suite.addTest(new JUnit4TestAdapter(DatagramAcknowledgedMessageTest.class));       
        suite.addTest(new JUnit4TestAdapter(DatagramRejectedMessageTest.class));       
        suite.addTest(new JUnit4TestAdapter(DefaultPropertyListenerSupportTest.class));       
        suite.addTest(new JUnit4TestAdapter(OlcbInterfaceTest.class));       
        suite.addTest(new JUnit4TestAdapter(StreamDataCompleteMessageTest.class));       
        suite.addTest(new JUnit4TestAdapter(StreamDataProceedMessageTest.class));       
        suite.addTest(new JUnit4TestAdapter(StreamDataSendMessageTest.class));       
        suite.addTest(new JUnit4TestAdapter(StreamInitiateReplyMessageTest.class));       
        suite.addTest(new JUnit4TestAdapter(StreamInitiateRequestMessageTest.class));       

        // test implementation classes
        suite.addTest(org.openlcb.implementations.PackageTest.suite());
        suite.addTest(org.openlcb.messages.PackageTest.suite());
        suite.addTest(org.openlcb.swing.PackageTest.suite());
        suite.addTest(org.openlcb.cdi.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(org.openlcb.hub.PackageTest.class));       
        suite.addTest(new JUnit4TestAdapter(org.openlcb.protocols.PackageTest.class));       
        
        // test CAN classes
        suite.addTest(org.openlcb.can.PackageTest.suite());

        return suite;
    }
}
