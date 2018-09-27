package org.openlcb.cdi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        org.openlcb.cdi.jdom.PackageTest.class,
        org.openlcb.cdi.swing.CdiPanelTest.class,
        org.openlcb.cdi.impl.PackageTest.class,
        org.openlcb.cdi.cmd.PackageTest.class 
})
/**
 * @author  Bob Jacobsen   Copyright 2011
 */
public class PackageTest {
}
