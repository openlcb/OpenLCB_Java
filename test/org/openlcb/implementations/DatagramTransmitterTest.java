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
public class DatagramTransmitterTest extends TestCase {
    
    NodeID hereID = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID farID  = new NodeID(new byte[]{1,1,1,1,1,1});
    
    int[] data;

    java.util.ArrayList<Message> messagesReceived;
    
    public void testSendOK() {
        messagesReceived = new java.util.ArrayList<Message>();
        Connection testConnection = new Connection(){
            public void put(Message msg, Connection sender) {
                messagesReceived.add(msg);
            }
        };

        data = new int[32];

        DatagramTransmitter xmt = new DatagramTransmitter(
                                            hereID,farID,
                                            data,
                                            testConnection);
                                                    
        Assert.assertEquals("init messages", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0)
                           .equals(new DatagramMessage(hereID, farID, data)));

        // Accepted
        Message m = new DatagramAcknowledgedMessage(farID, hereID);
        messagesReceived = new java.util.ArrayList<Message>();

        xmt.put(m, null);

        Assert.assertEquals("1st messages", 0, messagesReceived.size());
        
    }
    
    public void testOneResendNeeded() {
        messagesReceived = new java.util.ArrayList<Message>();
        Connection testConnection = new Connection(){
            public void put(Message msg, Connection sender) {
                messagesReceived.add(msg);
            }
        };
        
        data = new int[32];
        
        DatagramTransmitter xmt = new DatagramTransmitter(
                                            hereID,farID,
                                            data,
                                            testConnection);
                                                    
        Assert.assertEquals("init messages", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0)
                           .equals(new DatagramMessage(hereID, farID, data)));
                           
        // Reject once
        Message m = new DatagramRejectedMessage(farID, hereID,0x021);
        messagesReceived = new java.util.ArrayList<Message>();

        xmt.put(m, null);

        Assert.assertEquals("1st messages", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0)
                           .equals(new DatagramMessage(hereID, farID, data)));

        // Accepted
        m = new DatagramAcknowledgedMessage(farID, hereID);
        messagesReceived = new java.util.ArrayList<Message>();

        xmt.put(m, null);

        Assert.assertEquals("2nd messages", 0, messagesReceived.size());
    }
    
    // from here down is testing infrastructure
    
    public DatagramTransmitterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DatagramTransmitterTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DatagramTransmitterTest.class);
        return suite;
    }
}
