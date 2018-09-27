package org.openlcb.implementations.throttle;

import org.openlcb.*;

import org.junit.*;

/**
 * 
 * @author  Bob Jacobsen   Copyright 2012
 */
public class RemoteTrainNodeTest {
 
    @Test	
    public void testCtor() {
        new RemoteTrainNode(new NodeID(new byte[]{1,2,3,4,5,6}), null);
    }
    
    @Test	
    public void testNodeMemory() {
        RemoteTrainNode node = new RemoteTrainNode(new NodeID(new byte[]{1,2,3,4,5,6}), null);
        Assert.assertTrue(new NodeID(new byte[]{1,2,3,4,5,6}).equals(node.getNodeId()));
    }
        
}
