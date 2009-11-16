package scenarios.can;

import org.openlcb.*;
import org.openlcb.implementations.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Simulate two nodes interacting on a single CAN segment
 * <ul>
 * <li>Nodes 1 sends Event A to node 2
 * </ul>
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class TwoOnASegment extends TestCase {

    public void testBuild() {
    }    
    
    // from here down is testing infrastructure
    
    public TwoOnASegment(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {TwoOnASegment.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TwoOnASegment.class);
        return suite;
    }
}
