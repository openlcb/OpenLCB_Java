package org.openlcb.can;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        OpenLcbCanFrameTest.class,
        MessageBuilderTest.class,
        NIDaTest.class,
        NIDaAlgorithmTest.class,
        AliasMapTest.class,
        GridConnectTest.class,
        CanInterfaceTest.class,
        org.openlcb.can.impl.PackageTest.class 
})
/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class PackageTest {
}
