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
public class RemoteTrainNodeTest extends TestCase {
    
    public void testCtor() {
        new RemoteTrainNode(new NodeID(new byte[]{1,2,3,4,5,6}));
    }
    
    public void testNodeMemory() {
        RemoteTrainNode node = new RemoteTrainNode(new NodeID(new byte[]{1,2,3,4,5,6}));
        Assert.assertTrue(new NodeID(new byte[]{1,2,3,4,5,6}).equals(node.getNodeId()));
    }
        
    // from here down is testing infrastructure
    
    public RemoteTrainNodeTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {RemoteTrainNodeTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RemoteTrainNodeTest.class);
        return suite;
    }
}
