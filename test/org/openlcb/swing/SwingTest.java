package org.openlcb.swing;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;

import org.openlcb.swing.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class SwingTest extends TestCase {
    public void testStart() {
    }
    
    
    // from here down is testing infrastructure
    
    public SwingTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SwingTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SwingTest.class);
        suite.addTest(MonPaneTest.suite());

        return suite;
    }
}
