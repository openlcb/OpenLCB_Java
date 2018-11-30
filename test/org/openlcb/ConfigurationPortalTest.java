package org.openlcb;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2012
 */
public class ConfigurationPortalTest  {
    boolean result;
    
    NodeID nodeID1 = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID nodeID2 = new NodeID(new byte[]{1,2,3,4,5,7});

    Connection connection = new AbstractConnection(){
        public void put(Message m, Connection node) {
            msg = m;
        }
    };
    
    Message msg = null;
    
    ConfigurationPortal portal = new ConfigurationPortal(nodeID1, connection);
 
    @Before   
    public void setup() {
        msg = null;
    }
    
    @Test
    @Ignore("no test here")
    public void testCtor() {
    }

    @Test
    @Ignore("no test here")
    public void testRequestWrite() {
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
