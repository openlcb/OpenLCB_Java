package org.openlcb.cdi.jdom;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        JdomCdiRepTest.class,
        CdiMemConfigReaderTest.class,
        JdomCdiReaderTest.class, 
        XmlHelperTest.class     
})

/**
 * @author  Bob Jacobsen   Copyright 2011
 */
public class PackageTest {
}
