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
public class StreamReceiverTest extends TestCase {
    
    NodeID hereID = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID farID  = new NodeID(new byte[]{1,1,1,1,1,1});
    
    int[] data;

    java.util.ArrayList<Message> messagesReceived;
    
    public void testInitialization() {
        messagesReceived = new java.util.ArrayList<Message>();
        Connection testConnection = new AbstractConnection(){
            public void put(Message msg, Connection sender) {
                messagesReceived.add(msg);
            }
        };
        StreamReceiver rcv = new StreamReceiver(
                                            hereID,farID,
                                            testConnection);

        Assert.assertTrue(messagesReceived.size() == 0); // no startup messages
        
        // start operation
        Message m = new StreamInitiateRequestMessage(farID, hereID, (byte)64, (byte)11, (byte)0 );
        
        rcv.put(m, null);
        
        Assert.assertTrue(messagesReceived.size() == 1);
        Assert.assertTrue(messagesReceived.get(0)
                           .equals(new StreamInitiateReplyMessage(hereID, farID, (byte)64, (byte)11, (byte)3)));
    }

    public void testShortStream() {
        messagesReceived = new java.util.ArrayList<Message>();
        Connection testConnection = new AbstractConnection(){
            public void put(Message msg, Connection sender) {
                messagesReceived.add(msg);
            }
        };
        StreamReceiver rcv = new StreamReceiver(
                                            hereID,farID,
                                            testConnection);

        Assert.assertTrue(messagesReceived.size() == 0); // no startup messages
        
        // start operation
        Message m = new StreamInitiateRequestMessage(farID, hereID, 64, (byte)12, (byte)0);
        
        rcv.put(m, null);
        
        Assert.assertTrue(messagesReceived.size() == 1);
        Assert.assertTrue(messagesReceived.get(0)
                           .equals(new StreamInitiateReplyMessage(hereID, farID, 64, (byte)12, (byte)3)));

        // send one data message
        messagesReceived = new java.util.ArrayList<Message>();
        m = new StreamDataSendMessage(farID, hereID, new byte[64], (byte)0);
        
        rcv.put(m, null);
        
        Assert.assertTrue(messagesReceived.size() == 1);
        Assert.assertTrue(messagesReceived.get(0)
                           .equals(new StreamDataProceedMessage(hereID, farID, (byte)12, (byte)3)));
    }

    // from here down is testing infrastructure
    
    public StreamReceiverTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {StreamReceiverTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(StreamReceiverTest.class);
        return suite;
    }
}
