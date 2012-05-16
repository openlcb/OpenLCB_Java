package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009, 2012
 * @version $Revision$
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
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class);

        suite.addTest(UtilitiesTest.suite());

        suite.addTest(EventIDTest.suite());
        suite.addTest(NodeIDTest.suite());

        suite.addTest(MessageTypeIdentifierTest.suite());

        suite.addTest(MessageTest.suite());

        suite.addTest(MessageDecoderTest.suite());
        suite.addTest(NodeTest.suite());

        // specific message types (uses Node, IDs)
        suite.addTest(InitializationCompleteMessageTest.suite());

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

        suite.addTest(MimicNodeStoreTest.suite());

        // test implementation classes
        suite.addTest(org.openlcb.implementations.PackageTest.suite());
        suite.addTest(org.openlcb.swing.PackageTest.suite());
        suite.addTest(org.openlcb.cdi.PackageTest.suite());
        
        // test CAN classes
        suite.addTest(org.openlcb.can.PackageTest.suite());

        return suite;
    }
}
