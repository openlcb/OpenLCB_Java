package org.openlcb.implementations;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.JUnit4TestAdapter;

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
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class);

        suite.addTest(SingleConsumerNodeTest.suite());
        suite.addTest(SingleProducerNodeTest.suite());

        suite.addTest(ScatterGatherTest.suite());
        suite.addTest(EventFilterGatewayTest.suite());

        suite.addTest(DatagramTransmitterTest.suite());
        suite.addTest(DatagramReceiverTest.suite());
        
        suite.addTest(DatagramMeteringBufferTest.suite());

        suite.addTest(DatagramServiceTest.suite());

        suite.addTest(StreamTransmitterTest.suite());
        suite.addTest(StreamReceiverTest.suite());

        suite.addTest(BlueGoldEngineTest.suite());

        suite.addTest(MemoryConfigurationServiceTest.suite());

        suite.addTest(org.openlcb.implementations.throttle.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(BitProducerConsumerTest.class));
        suite.addTest(new TestSuite(VersionedValueTest.class));
        suite.addTest(new JUnit4TestAdapter(FakeMemoryConfigurationServiceTest.class));       
        suite.addTest(new JUnit4TestAdapter(MemoryConfigSpaceRetrieverTest.class));       
        suite.addTest(new JUnit4TestAdapter(SingleConsumerTest.class));       
        suite.addTest(new JUnit4TestAdapter(SingleProducerTest.class));       
        suite.addTest(new JUnit4TestAdapter(VersionOutOfDateExceptionTest.class));       
        suite.addTest(new JUnit4TestAdapter(BlueGoldExtendedEngineTest.class));       

        return suite;
    }
}
