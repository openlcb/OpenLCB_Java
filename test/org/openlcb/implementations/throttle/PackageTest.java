package org.openlcb.implementations.throttle;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        Float16Test.class,
        ThrottleSpeedDatagramTest.class,
        ThrottleImplementationTest.class,
        RemoteTrainNodeTest.class,
        RemoteTrainNodeCacheTest.class,
        org.openlcb.implementations.throttle.dcc.PackageTest.class,
        ThrottleFunctionDatagramTest.class, 
        TractionThrottleTest.class,
        TrainNodeCacheTest.class,      
        FdiParserTest.class,      
})
/**
 * @author  Bob Jacobsen   Copyright 2012
 */
public class PackageTest {
}
