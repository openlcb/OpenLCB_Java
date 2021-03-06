package org.openlcb.implementations;

import org.openlcb.*;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class SingleProducerNodeTest {
    
    boolean result;
    
    NodeID nodeID = new NodeID(new byte[]{1,2,3,4,5,6});
    
    EventID eventID = new EventID(new byte[]{1,0,0,0,0,0,1,0});
    
    java.util.ArrayList<Message> messagesReceived;
   
    @Test 
    public void testInitialization() {
        result = false;
        messagesReceived = new java.util.ArrayList<Message>();
        Connection testConnection = new AbstractConnection(){
            public void put(Message msg, Connection sender) {
                messagesReceived.add(msg);
            }
        };
        SingleProducerNode node = new SingleProducerNode(
                                            nodeID,
                                            testConnection,
                                            eventID);
                                            
        node.initialize();
        
        Assert.assertTrue(messagesReceived.get(0)
                           .equals(new InitializationCompleteMessage(nodeID)));
        Assert.assertTrue(messagesReceived.get(1)
                           .equals(new ProducerIdentifiedMessage(nodeID, eventID, EventState.Unknown)));
    }
    
    @Test 
    public void testSend() {
        result = false;
        messagesReceived = new java.util.ArrayList<Message>();
        Connection testConnection = new AbstractConnection(){
            public void put(Message msg, Connection sender) {
                messagesReceived.add(msg);
            }
        };
        SingleProducerNode node = new SingleProducerNode(
                                            nodeID,
                                            testConnection,
                                            eventID);
                                            
        node.initialize();
        
        Assert.assertTrue(messagesReceived.get(0)
                           .equals(new InitializationCompleteMessage(nodeID)));
        Assert.assertTrue(messagesReceived.get(1)
                           .equals(new ProducerIdentifiedMessage(nodeID, eventID, EventState.Unknown)));
        node.send();
        Assert.assertTrue(messagesReceived.get(2)
                           .equals(new ProducerConsumerEventReportMessage(nodeID, eventID)));
        
    }
    
}
