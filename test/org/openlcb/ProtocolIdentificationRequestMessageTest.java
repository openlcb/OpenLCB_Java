package org.openlcb;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class ProtocolIdentificationRequestMessageTest {
    boolean result;
    
    NodeID nodeID1 = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID nodeID2 = new NodeID(new byte[]{0,0,0,0,0,0});

    @Test
    public void testEqualsSame() {
        Message m1 = new ProtocolIdentificationRequestMessage(
                               nodeID1, nodeID2 );
        Message m2 = new ProtocolIdentificationRequestMessage(
                               nodeID1, nodeID2 );
    
        Assert.assertTrue(m1.equals(m2));
    }

    @Test
    public void testNotEqualsDifferentSrcNode() {
        Message m1 = new ProtocolIdentificationRequestMessage(
                                nodeID1, nodeID2 );
        Message m2 = new ProtocolIdentificationRequestMessage(
                                nodeID2, nodeID2 );
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test
    public void testNotEqualsDifferentDestNode() {
        Message m1 = new ProtocolIdentificationRequestMessage(
                                nodeID2, nodeID1 );
        Message m2 = new ProtocolIdentificationRequestMessage(
                                nodeID2, nodeID2 );
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test
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
}
