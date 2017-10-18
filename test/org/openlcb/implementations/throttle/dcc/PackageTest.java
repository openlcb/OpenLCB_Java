package org.openlcb.implementations.throttle.dcc;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.JUnit4TestAdapter;

/**
 * @author  Bob Jacobsen   Copyright 2012
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

        suite.addTest(DccProxyCacheTest.suite());
        suite.addTest(new JUnit4TestAdapter(RemoteDccProxyTest.class));       

        return suite;
    }
}
