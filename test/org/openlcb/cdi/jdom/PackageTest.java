package org.openlcb.cdi.jdom;

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
        
        suite.addTest(JdomCdiRepTest.suite());
        suite.addTest(CdiMemConfigReaderTest.suite());
        suite.addTest(new JUnit4TestAdapter(JdomCdiReaderTest.class));       
        suite.addTest(new JUnit4TestAdapter(XmlHelperTest.class));       

        return suite;
    }
}
