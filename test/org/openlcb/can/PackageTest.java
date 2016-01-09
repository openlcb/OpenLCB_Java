package org.openlcb.can;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
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

        suite.addTest(OpenLcbCanFrameTest.suite());
        suite.addTest(MessageBuilderTest.suite());
        suite.addTest(NIDaTest.suite());
        suite.addTest(NIDaAlgorithmTest.suite());
        suite.addTest(AliasMapTest.suite());
        suite.addTest(new TestSuite(GridConnectTest.class));

        return suite;
    }
}
