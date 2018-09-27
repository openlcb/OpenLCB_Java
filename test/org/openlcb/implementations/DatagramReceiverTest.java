package org.openlcb.implementations;

import org.openlcb.*;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class DatagramReceiverTest {
    
    NodeID hereID = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID farID  = new NodeID(new byte[]{1,1,1,1,1,1});
    
    int[] data;

    java.util.ArrayList<Message> messagesReceived;
   
    @Test 
    public void testTransfer() {
        messagesReceived = new java.util.ArrayList<Message>();
        Connection testConnection = new AbstractConnection(){
            public void put(Message msg, Connection sender) {
                messagesReceived.add(msg);
            }
        };
        DatagramReceiver rcv = new DatagramReceiver(
                                            hereID,farID,
                                            testConnection);

        Assert.assertTrue(messagesReceived.size() == 0); // no startup messages
        
        // start operation
        Message m = new DatagramMessage(farID, hereID, new int[34]);
        
        rcv.put(m, null);
        
        Assert.assertTrue(messagesReceived.size() == 1);
        Assert.assertTrue(messagesReceived.get(0)
                           .equals(new DatagramAcknowledgedMessage(hereID, farID)));
    }

}
