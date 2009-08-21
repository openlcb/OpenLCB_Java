package scenarios;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Primary test runner for this package.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class Scenarios extends TestCase {
    public void testStart() {
    }
    
    // BlueGoldCheck not JUnit so can run standalone
    public void testBlueGold() throws Exception {
        BlueGoldCheck.runTest();
    }
    
    // from here down is testing infrastructure
    
    public Scenarios(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {Scenarios.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(Scenarios.class);
        suite.addTest(NineOnALink.suite());
        
        suite.addTest(TwoBuses.suite());
        suite.addTest(TwoBusesFiltered.suite());

        suite.addTest(scenarios.can.CanScenarios.suite());

        // BlueGoldCheck done above
        
        return suite;
    }
}
