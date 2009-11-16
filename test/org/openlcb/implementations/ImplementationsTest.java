package org.openlcb.implementations;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class ImplementationsTest extends TestCase {
    public void testStart() {
    }
    
    // from here down is testing infrastructure
    
    public ImplementationsTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ImplementationsTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ImplementationsTest.class);

        suite.addTest(SingleConsumerNodeTest.suite());
        suite.addTest(SingleProducerNodeTest.suite());

        suite.addTest(ScatterGatherTest.suite());
        suite.addTest(EventFilterGatewayTest.suite());

        suite.addTest(DatagramTransmitterTest.suite());
        suite.addTest(DatagramReceiverTest.suite());

        suite.addTest(StreamTransmitterTest.suite());
        suite.addTest(StreamReceiverTest.suite());

        suite.addTest(BlueGoldEngineTest.suite());

        return suite;
    }
}
