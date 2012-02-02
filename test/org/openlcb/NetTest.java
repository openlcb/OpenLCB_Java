package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class NetTest extends TestCase {
    public void testStart() {
    }
    
    // from here down is testing infrastructure
    
    public NetTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NetTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NetTest.class);

        suite.addTest(EventIDTest.suite());
        suite.addTest(NodeIDTest.suite());

        suite.addTest(MessageTest.suite());

        suite.addTest(MessageDecoderTest.suite());
        suite.addTest(NodeTest.suite());

        // specific message types (uses Node, IDs)
        suite.addTest(ConsumerIdentifiedMessageTest.suite());
        suite.addTest(IdentifyConsumersMessageTest.suite());
        suite.addTest(IdentifyEventsMessageTest.suite());
        suite.addTest(IdentifyProducersMessageTest.suite());
        suite.addTest(InitializationCompleteMessageTest.suite());
        suite.addTest(ProtocolIdentificationTest.suite());
        suite.addTest(ProducerConsumerEventReportMessageTest.suite());
        suite.addTest(ProducerIdentifiedMessageTest.suite());
        suite.addTest(VerifiedNodeIDNumberMessageTest.suite());
        suite.addTest(VerifyNodeIDNumberMessageTest.suite());
        suite.addTest(LearnEventMessageTest.suite());
        
        suite.addTest(SingleLinkNodeTest.suite());

        suite.addTest(GatewayTest.suite());

        suite.addTest(MimicNodeStoreTest.suite());

        // test implementation classes
        suite.addTest(org.openlcb.implementations.ImplementationsTest.suite());
        suite.addTest(org.openlcb.swing.SwingTest.suite());
        suite.addTest(org.openlcb.cdi.PackageTest.suite());
        
        // test CAN classes
        suite.addTest(org.openlcb.can.CanTest.suite());

        return suite;
    }
}
