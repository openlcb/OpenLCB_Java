package org.openlcb;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class SingleLinkNodeTest {
    
    boolean result;
    
    NodeID nodeID = new NodeID(new byte[]{1,2,3,4,5,6});

    @Test    
    public void testInitialization() {
        result = false;
        Connection testConnection = new AbstractConnection(){
            public void put(Message msg, Connection node) {
                if (msg.equals(new InitializationCompleteMessage(nodeID)))
                    result = true;
                else
                    Assert.fail("Wrong message: "+msg);
            }
        };
        SingleLinkNode node = new SingleLinkNode(
                                            nodeID,
                                            testConnection);
                                            
        node.initialize();
        
        Assert.assertTrue(result);
    }
    
}
