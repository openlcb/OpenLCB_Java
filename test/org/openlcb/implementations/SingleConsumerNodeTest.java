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
public class SingleConsumerNodeTest extends TestCase {
    
    boolean result;
    
    NodeID nodeID = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID nodeIDb = new NodeID(new byte[]{0,0,0,0,0,0});
    
    EventID eventID = new EventID(new byte[]{1,0,0,0,0,0,1,0});
    EventID eventIDb = new EventID(new byte[]{1,0,0,0,0,0,2,0});
    
    java.util.ArrayList<Message> messagesReceived;
    
    public void testInitialization() {
        result = false;
        messagesReceived = new java.util.ArrayList<Message>();
        Connection testConnection = new Connection(){
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
                           .equals(new ConsumerIdentifiedMessage(nodeID, eventID)));
    }
    
    public void testConsumeRight() {
        Connection testConnection = new Connection(){
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

    public void testConsumeWrong() {
        Connection testConnection = new Connection(){
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
    
    // from here down is testing infrastructure
    
    public SingleConsumerNodeTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SingleConsumerNodeTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SingleConsumerNodeTest.class);
        return suite;
    }
}
