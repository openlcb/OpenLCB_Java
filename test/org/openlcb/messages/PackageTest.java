package org.openlcb.messages;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openlcb.implementations.BlueGoldEngineTest;
import org.openlcb.implementations.DatagramMeteringBufferTest;
import org.openlcb.implementations.DatagramReceiverTest;
import org.openlcb.implementations.DatagramServiceTest;
import org.openlcb.implementations.DatagramTransmitterTest;
import org.openlcb.implementations.EventFilterGatewayTest;
import org.openlcb.implementations.MemoryConfigurationServiceTest;
import org.openlcb.implementations.ScatterGatherTest;
import org.openlcb.implementations.SingleConsumerNodeTest;
import org.openlcb.implementations.SingleProducerNodeTest;
import org.openlcb.implementations.StreamReceiverTest;
import org.openlcb.implementations.StreamTransmitterTest;

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

        suite.addTest(TractionControlRequestMessageTest.suite());

        return suite;
    }
}
