package scenarios.can;

import org.openlcb.*;
import org.openlcb.implementations.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Simulate nine nodes interacting on a single CAN bus OpenLCB segment.
 * 
 * <ul>
 * <li>Nodes 1,2,3 send Event A to 8,9
 * <li>Node 4 sends Event B to node 7
 * <li>Node 5 sends Event C to node 6
 * </ul>
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class NineOnASegment extends TestCase {

    public void testBuild() {
    }    
    
    // from here down is testing infrastructure
    
    public NineOnASegment(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NineOnASegment.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NineOnASegment.class);
        return suite;
    }
}
