package org.openlcb.implementations.throttle;

import org.openlcb.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class TrainNodeTest extends TestCase {
    
    public void testCtor() {
        new TrainNode(new NodeID(new byte[]{1,2,3,4,5,6}));
    }
    
    public void testNodeMemory() {
        TrainNode node = new TrainNode(new NodeID(new byte[]{1,2,3,4,5,6}));
        Assert.assertTrue(new NodeID(new byte[]{1,2,3,4,5,6}).equals(node.getNode()));
    }
        
    // from here down is testing infrastructure
    
    public TrainNodeTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {TrainNodeTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TrainNodeTest.class);
        return suite;
    }
}
