package org.openlcb.implementations;

import org.openlcb.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class SingleProducerNodeTest extends TestCase {
    
    boolean result;
    
    NodeID nodeID = new NodeID(new byte[]{1,2,3,4,5,6});
    
    EventID eventID = new EventID(new byte[]{1,0,0,0,0,0,1,0});
    
    java.util.ArrayList<Message> messagesReceived;
    
    public void testInitialization() {
        result = false;
        messagesReceived = new java.util.ArrayList<Message>();
        Connection testConnection = new Connection(){
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
                           .equals(new ProducerIdentifiedMessage(nodeID, eventID)));
    }
    
    public void testSend() {
        result = false;
        messagesReceived = new java.util.ArrayList<Message>();
        Connection testConnection = new Connection(){
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
                           .equals(new ProducerIdentifiedMessage(nodeID, eventID)));
        node.send();
        Assert.assertTrue(messagesReceived.get(2)
                           .equals(new ProducerConsumerEventReportMessage(nodeID, eventID)));
        
    }
    

    // from here down is testing infrastructure
    
    public SingleProducerNodeTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SingleProducerNodeTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SingleProducerNodeTest.class);
        return suite;
    }
}
