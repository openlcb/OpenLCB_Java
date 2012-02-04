package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class ProtocolIdentificationReplyMessageTest extends TestCase {
    boolean result;
    
    NodeID nodeID1 = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID nodeID2 = new NodeID(new byte[]{0,0,0,0,0,0});

    public void testEqualsSame() {
        Message m1 = new ProtocolIdentificationReplyMessage(
                               nodeID1, 12);
        Message m2 = new ProtocolIdentificationReplyMessage(
                               nodeID1, 12);
    
        Assert.assertTrue(m1.equals(m2));
    }

    public void testNotEqualsDifferentNode() {
        Message m1 = new ProtocolIdentificationReplyMessage(
                                nodeID1, 12);
        Message m2 = new ProtocolIdentificationReplyMessage(
                                nodeID2, 12);
    
        Assert.assertTrue( ! m1.equals(m2));
    }


    public void testNotEqualsDifferentValue() {
        Message m1 = new ProtocolIdentificationReplyMessage(
                                nodeID1, 12);
        Message m2 = new ProtocolIdentificationReplyMessage(
                                nodeID1, 13);
    
        Assert.assertTrue( ! m1.equals(m2));
    }


    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleProtocolIdentificationReply(ProtocolIdentificationReplyMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new ProtocolIdentificationReplyMessage(nodeID1, 21);
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }
    
    // from here down is testing infrastructure
    
    public ProtocolIdentificationReplyMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ProtocolIdentificationReplyMessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ProtocolIdentificationReplyMessageTest.class);
        return suite;
    }
}
