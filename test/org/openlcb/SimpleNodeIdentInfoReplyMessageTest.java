package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class SimpleNodeIdentInfoReplyMessageTest extends TestCase {
    boolean result;
    
    NodeID nodeID1 = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID nodeID2 = new NodeID(new byte[]{0,0,0,0,0,0});

    public void testEqualsSame() {
        Message m1 = new SimpleNodeIdentInfoReplyMessage(
                               nodeID1, nodeID2, new byte[]{1,2});
        Message m2 = new SimpleNodeIdentInfoReplyMessage(
                               nodeID1, nodeID2, new byte[]{1,2});
    
        Assert.assertTrue(m1.equals(m2));
    }

    public void testNotEqualsDifferentNode() {
        Message m1 = new SimpleNodeIdentInfoReplyMessage(
                                nodeID1, nodeID2, new byte[]{1,2});
        Message m2 = new SimpleNodeIdentInfoReplyMessage(
                                nodeID2, nodeID2, new byte[]{1,2});
    
        Assert.assertTrue( ! m1.equals(m2));
    }


    public void testNotEqualsDifferentValue() {
        Message m1 = new SimpleNodeIdentInfoReplyMessage(
                                nodeID1, nodeID2, new byte[]{1,2});
        Message m2 = new SimpleNodeIdentInfoReplyMessage(
                                nodeID1, nodeID2, new byte[]{3,1});
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    public void testNotEqualsDifferentValueLength() {
        Message m1 = new SimpleNodeIdentInfoReplyMessage(
                                nodeID1, nodeID2, new byte[]{1,2});
        Message m2 = new SimpleNodeIdentInfoReplyMessage(
                                nodeID1, nodeID2, new byte[]{1,2,3});
    
        Assert.assertTrue( ! m1.equals(m2));
    }
    public void testNotEqualsDifferentValueLengthBis() {
        Message m1 = new SimpleNodeIdentInfoReplyMessage(
                                nodeID1, nodeID2, new byte[]{1,2,3});
        Message m2 = new SimpleNodeIdentInfoReplyMessage(
                                nodeID1, nodeID2, new byte[]{1,2});
    
        Assert.assertTrue( ! m1.equals(m2));
    }


    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleSimpleNodeIdentInfoReply(SimpleNodeIdentInfoReplyMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new SimpleNodeIdentInfoReplyMessage(nodeID1, nodeID2, new byte[]{1,2});
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }

    public void testPrint() {
        Message m = new SimpleNodeIdentInfoReplyMessage(nodeID1, nodeID2, new byte[]{4,'a','b',
                'c',0,'d','e','f','g',0,0,2,'u',0,'v',0});
        String s = m.toString();
        Assert.assertEquals("01.02.03.04.05.06 - 00.00.00.00.00.00 Simple Node Ident Info with" +
                " content '4,abc,defg,,2,u,v,'", s);
    }

    // from here down is testing infrastructure
    
    public SimpleNodeIdentInfoReplyMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SimpleNodeIdentInfoReplyMessageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SimpleNodeIdentInfoReplyMessageTest.class);
        return suite;
    }
}
