
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.JUnit4TestAdapter;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class AllTest extends TestCase {
    public void testStart() {
    }
    
    // from here down is testing infrastructure
    
    public AllTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {AllTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AllTest.class);
        suite.addTest(tools.ToolsTest.suite());
        suite.addTest(org.openlcb.PackageTest.suite());
        suite.addTest(scenarios.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(org.openlcb.protocols.PackageTest.class));       
        return suite;
    }
}
