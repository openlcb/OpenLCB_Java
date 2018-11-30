package org.openlcb.implementations;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SingleConsumerNodeTest.class,
        SingleProducerNodeTest.class,
        ScatterGatherTest.class,
        EventFilterGatewayTest.class,
        DatagramTransmitterTest.class,
        DatagramReceiverTest.class,
        DatagramMeteringBufferTest.class,
        DatagramServiceTest.class,
        StreamTransmitterTest.class,
        StreamReceiverTest.class,
        MemoryConfigurationServiceTest.class,
        org.openlcb.implementations.throttle.PackageTest.class,
        BitProducerConsumerTest.class,
        VersionedValueTest.class,
        FakeMemoryConfigurationServiceTest.class,  
        MemoryConfigSpaceRetrieverTest.class,     
        SingleConsumerTest.class,
        SingleProducerTest.class,       
        VersionOutOfDateExceptionTest.class,      
        BlueGoldExtendedEngineTest.class,      
    })
/**
 * @author  Bob Jacobsen   Copyright 2009, 2012
 */
public class PackageTest {
}
