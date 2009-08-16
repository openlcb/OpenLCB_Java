package scenarios.can;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class CanScenarios extends TestCase {
    public void testStart() {
    }
    
    // from here down is testing infrastructure
    
    public CanScenarios(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {CanScenarios.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CanScenarios.class);

        suite.addTest(TwoOnASegment.suite());
        suite.addTest(NineOnASegment.suite());

        return suite;
    }
}
