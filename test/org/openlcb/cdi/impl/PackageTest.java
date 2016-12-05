package org.openlcb.cdi.impl;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openlcb.cdi.jdom.CdiMemConfigReaderTest;
import org.openlcb.cdi.jdom.JdomCdiRepTest;

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
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class);
        
        suite.addTest(RangeCacheUtilTest.suite());
        suite.addTest(ConfigRepresentationTest.suite());

        return suite;
    }
}
