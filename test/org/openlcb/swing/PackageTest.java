package org.openlcb.swing;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.JUnit4TestAdapter;

import javax.swing.*;

import org.openlcb.swing.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
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
        suite.addTest(MonPaneTest.suite());
        suite.addTest(NodeSelectorTest.suite());
        suite.addTest(EventIdTextFieldTest.suite());

        suite.addTest(org.openlcb.swing.networktree.PackageTest.suite());
        suite.addTest(org.openlcb.swing.memconfig.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(ConsumerPaneTest.class));       
        suite.addTest(new JUnit4TestAdapter(ProducerPaneTest.class));       
        suite.addTest(new JUnit4TestAdapter(NodeIdTextFieldTest.class));       

        return suite;
    }
}
