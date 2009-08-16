package tools.cansim;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class CanSimTest extends TestCase {
    public void testStart() {
    }
    
    // from here down is testing infrastructure
    
    public CanSimTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {CanSimTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CanSimTest.class);
        suite.addTest(CanSegmentTest.suite());
        suite.addTest(CanFrameTest.suite());
        return suite;
    }
}
