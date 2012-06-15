package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class OptionalIntRejectedMessageTest extends TestCase {
    boolean result;
    
    NodeID nodeID1 = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID nodeID2 = new NodeID(new byte[]{0,0,0,0,0,0});

    public void testEqualsSame() {
        Message m1 = new OptionalIntRejectedMessage(
                               nodeID1,nodeID2,0,1);
        Message m2 = new OptionalIntRejectedMessage(
                               nodeID1,nodeID2,0,1);
    
        Assert.assertTrue(m1.equals(m2));
    }

    public void testNotEqualsDifferentSrcNode() {
        Message m1 = new OptionalIntRejectedMessage(
                                nodeID1,nodeID2,0,1);
        Message m2 = new OptionalIntRejectedMessage(
                                nodeID2,nodeID2,0,1);
    
        Assert.assertTrue( ! m1.equals(m2));
    }
    
    public void testNotEqualsDifferentDstNode() {
        Message m1 = new OptionalIntRejectedMessage(
                                nodeID1,nodeID2,0,1);
        Message m2 = new OptionalIntRejectedMessage(
                                nodeID1,nodeID1,0,1);
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    public void testNotEqualsDifferentCode() {
        Message m1 = new OptionalIntRejectedMessage(
                                nodeID1,nodeID2,0,1);
        Message m2 = new OptionalIntRejectedMessage(
                                nodeID1,nodeID2,0,2);
    
        Assert.assertTrue( ! m1.equals(m2));
    }
    
    public void testNotEqualsDifferentMti() {
        Message m1 = new OptionalIntRejectedMessage(
                                nodeID1,nodeID2,0,1);
        Message m2 = new OptionalIntRejectedMessage(
                                nodeID1,nodeID2,2,1);
    
        Assert.assertTrue( ! m1.equals(m2));
    }
    

    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleOptionalIntRejected(OptionalIntRejectedMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new OptionalIntRejectedMessage(nodeID1, nodeID2, 0, 1);
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }
    
    // from here down is testing infrastructure
    
    public OptionalIntRejectedMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {OptionalIntRejectedMessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OptionalIntRejectedMessageTest.class);
        return suite;
    }
}
