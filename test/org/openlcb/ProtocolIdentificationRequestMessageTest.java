package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class ProtocolIdentificationRequestMessageTest extends TestCase {
    boolean result;
    
    NodeID nodeID1 = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID nodeID2 = new NodeID(new byte[]{0,0,0,0,0,0});

    public void testEqualsSame() {
        Message m1 = new ProtocolIdentificationRequestMessage(
                               nodeID1, nodeID2 );
        Message m2 = new ProtocolIdentificationRequestMessage(
                               nodeID1, nodeID2 );
    
        Assert.assertTrue(m1.equals(m2));
    }

    public void testNotEqualsDifferentSrcNode() {
        Message m1 = new ProtocolIdentificationRequestMessage(
                                nodeID1, nodeID2 );
        Message m2 = new ProtocolIdentificationRequestMessage(
                                nodeID2, nodeID2 );
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    public void testNotEqualsDifferentDestNode() {
        Message m1 = new ProtocolIdentificationRequestMessage(
                                nodeID2, nodeID1 );
        Message m2 = new ProtocolIdentificationRequestMessage(
                                nodeID2, nodeID2 );
    
        Assert.assertTrue( ! m1.equals(m2));
    }


    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleProtocolIdentificationRequest(ProtocolIdentificationRequestMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new ProtocolIdentificationRequestMessage(nodeID1, nodeID2);
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }
    
    // from here down is testing infrastructure
    
    public ProtocolIdentificationRequestMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ProtocolIdentificationRequestMessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ProtocolIdentificationRequestMessageTest.class);
        return suite;
    }
}
