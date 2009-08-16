package tools.cansim;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import tools.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class CanFrameTest extends TestCase {
    public void testStart() {
    }

    public void testSimpleEquals() {
        CanFrame cf12a = new CanFrame(12);
        CanFrame cf12b = new CanFrame(12);
        CanFrame cf13 = new CanFrame(13);
        
        Assert.assertTrue("12a equals 12a", cf12a.equals(cf12a));
        Assert.assertTrue("12a equals 12b", cf12a.equals(cf12b));
        Assert.assertTrue("12a not equals 13", !cf12a.equals(cf13));
    }
    
    public void testSimpleEqualObject() {
        Object cf12a = new CanFrame(12);
        Object cf12b = new CanFrame(12);
        Object cf13 = new CanFrame(13);
        
        Assert.assertTrue("12a equals 12a", cf12a.equals(cf12a));
        Assert.assertTrue("12a equals 12b", cf12a.equals(cf12b));
        Assert.assertTrue("12a not equals 13", !cf12a.equals(cf13));
    }
    
    // from here down is testing infrastructure
    
    public CanFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {CanFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CanFrameTest.class);
        return suite;
    }
}
