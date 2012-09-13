package org.openlcb.implementations.throttle;

import org.openlcb.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class Float16Test extends TestCase {
    
    public void testStart() {
    }
    
    // from here down is testing infrastructure
    
    public Float16Test(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {Float16Test.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(Float16Test.class);
        return suite;
    }
}
