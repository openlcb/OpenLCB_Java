package tools;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class ToolsTest extends TestCase {
    public void testStart() {
    }
    
    // from here down is testing infrastructure
    
    public ToolsTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ToolsTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ToolsTest.class);
        suite.addTest(TimerTest.suite());
        suite.addTest(tools.cansim.CanSimTest.suite());
        return suite;
    }
}
