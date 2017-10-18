package org.openlcb.cdi;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.JUnit4TestAdapter;

/**
 * @author  Bob Jacobsen   Copyright 2011
 * @version $Revision: 34 $
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
        
        suite.addTest(org.openlcb.cdi.jdom.PackageTest.suite());
        suite.addTest(org.openlcb.cdi.swing.CdiPanelTest.suite());
        suite.addTest(org.openlcb.cdi.impl.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(org.openlcb.cdi.cmd.PackageTest.class));       

        return suite;
    }
}
