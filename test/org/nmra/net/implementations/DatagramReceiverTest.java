package org.nmra.net.implementations;

import org.nmra.net.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class DatagramReceiverTest extends TestCase {
    
    NodeID hereID = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID farID  = new NodeID(new byte[]{1,1,1,1,1,1});
    
    int[] data;

    java.util.ArrayList<Message> messagesReceived;
    
    public void testTransfer() {
        messagesReceived = new java.util.ArrayList<Message>();
        Connection testConnection = new Connection(){
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

    // from here down is testing infrastructure
    
    public DatagramReceiverTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DatagramReceiverTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DatagramReceiverTest.class);
        return suite;
    }
}
