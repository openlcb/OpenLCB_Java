package org.openlcb.implementations;

import org.openlcb.*;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class SingleConsumerNodeTest {
    
    boolean result;
    
    NodeID nodeID = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID nodeIDb = new NodeID(new byte[]{0,0,0,0,0,0});
    
    EventID eventID = new EventID(new byte[]{1,0,0,0,0,0,1,0});
    EventID eventIDb = new EventID(new byte[]{1,0,0,0,0,0,2,0});
    
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
        SingleConsumerNode node = new SingleConsumerNode(
                                            nodeID,
                                            testConnection,
                                            eventID);
                                            
        node.initialize();
        
        Assert.assertTrue(messagesReceived.get(0)
                           .equals(new InitializationCompleteMessage(nodeID)));
        Assert.assertTrue(messagesReceived.get(1)
                           .equals(new ConsumerIdentifiedMessage(nodeID, eventID, EventState.Unknown)));
    }
    
    @Test 
    public void testConsumeRight() {
        Connection testConnection = new AbstractConnection(){
            public void put(Message msg, Connection sender) {
            }
        };
        SingleConsumerNode node = new SingleConsumerNode(
                                            nodeID,
                                            testConnection,
                                            eventID);
                                            
        node.initialize();
        node.put(new ProducerConsumerEventReportMessage(nodeIDb, eventID), testConnection);
        Assert.assertTrue(node.getReceived());
        Assert.assertTrue(!node.getReceived());  // check reset
    }

    @Test 
    public void testConsumeWrong() {
        Connection testConnection = new AbstractConnection(){
            public void put(Message msg, Connection sender) {
            }
        };
        SingleConsumerNode node = new SingleConsumerNode(
                                            nodeID,
                                            testConnection,
                                            eventID);
                                            
        node.initialize();
        node.put(new ProducerConsumerEventReportMessage(nodeIDb, eventIDb), testConnection);
        Assert.assertTrue(!node.getReceived());
    }
    
}
