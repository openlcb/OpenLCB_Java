package org.openlcb.cdi.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openlcb.cdi.jdom.CdiMemConfigReaderTest;
import org.openlcb.cdi.jdom.JdomCdiRepTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        RangeCacheUtilTest.class,
        ConfigRepresentationTest.class,
        MemorySpaceCacheTest.class,    
        DemoReadWriteAccessTest.class   
})
/**
 * @author  Bob Jacobsen   Copyright 2011
 */
public class PackageTest {
}
