package org.openlcb.implementations;

import org.openlcb.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class DatagramMeteringBufferTest extends TestCase {
    
    NodeID hereID = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID farID  = new NodeID(new byte[]{1,1,1,1,1,1});
    
    int[] data;

    java.util.ArrayList<Message> repliesReturned1;
    Connection replyConnection1;

    Connection testConnection;

    java.util.ArrayList<Message> messagesForwarded;
    Connection forwardConnection;
        
    DatagramMeteringBuffer buffer;
    
    DatagramMessage datagram1;
    DatagramMessage datagram2;
    
    DatagramAcknowledgedMessage replyOK;
    DatagramRejectedMessage replyNAK;
    
    public void setUp() {

        repliesReturned1 = new java.util.ArrayList<Message>();
        replyConnection1 = new Connection(){
            public void put(Message msg, Connection sender) {
                repliesReturned1.add(msg);
            }
        };

        messagesForwarded = new java.util.ArrayList<Message>();
        forwardConnection = new Connection(){
            public void put(Message msg, Connection sender) {
                messagesForwarded.add(msg);
                testConnection = sender;
            }
        };

        testConnection = null;
        
        data = new int[32];
        
        buffer = new DatagramMeteringBuffer(forwardConnection);

        data[0] = 1;
        datagram1   = new DatagramMessage(hereID, farID, data);                                        
        data[0] = 2;
        datagram2   = new DatagramMessage(hereID, farID, data);                                        

        replyOK     = new DatagramAcknowledgedMessage(farID, hereID);
        replyNAK    = new DatagramRejectedMessage(farID, hereID, 0x010);
    }
    
    public void testSend() throws InterruptedException {
        buffer.put(datagram1, replyConnection1);

        Thread.currentThread().sleep(10);
    }

    public void testSendNonDatagramGoesThrough() throws InterruptedException {
        Message m = new InitializationCompleteMessage(hereID);
        buffer.put(m, replyConnection1);

        Thread.currentThread().sleep(10);
        Assert.assertEquals("forwarded messages", 1, messagesForwarded.size());
        Assert.assertTrue(messagesForwarded.get(0).equals(m));        
        Assert.assertTrue(testConnection != null);
    }

    public void testFirstDatagramSendGoesThrough() throws InterruptedException {
        buffer.put(datagram1, replyConnection1);

        Thread.currentThread().sleep(10);
        Assert.assertEquals("forwarded messages", 1, messagesForwarded.size());
        Assert.assertTrue(messagesForwarded.get(0).equals(datagram1));        
        Assert.assertTrue(testConnection != null);
    }

    public void testSendReplyOK() throws InterruptedException {
        buffer.put(datagram1, replyConnection1);

        Thread.currentThread().sleep(10);
        Assert.assertTrue(testConnection != null);

        testConnection.put(replyOK, null);

        Assert.assertEquals("reply messages", 1, repliesReturned1.size());
        Assert.assertTrue(repliesReturned1.get(0).equals(replyOK));        
    }

    public void testSendReplyNakRetransmit() throws InterruptedException {
        buffer.put(datagram1, replyConnection1);

        Thread.currentThread().sleep(10);

        testConnection.put(replyNAK, null);

        Assert.assertEquals("forwarded messages", 2, messagesForwarded.size());
        Assert.assertTrue(messagesForwarded.get(1).equals(datagram1));        
    }

    public void testSendReplyNakRetransmitreplyOK() throws InterruptedException {
        buffer.put(datagram1, replyConnection1);

        Thread.currentThread().sleep(10);

        testConnection.put(replyNAK, null);

        Assert.assertEquals("forwarded messages", 2, messagesForwarded.size());
        Assert.assertTrue(messagesForwarded.get(1).equals(datagram1));        

        testConnection.put(replyOK, null);

        Assert.assertEquals("reply messages", 1, repliesReturned1.size());
        Assert.assertTrue(repliesReturned1.get(0).equals(replyOK));        
    }

    public void testSendTwoNonDatagramGoesThrough() throws InterruptedException {
        Message m = new InitializationCompleteMessage(hereID);
        buffer.put(m, replyConnection1);
        buffer.put(m, replyConnection1);

        Thread.currentThread().sleep(10);
        Assert.assertEquals("forwarded messages", 2, messagesForwarded.size());
        Assert.assertTrue(messagesForwarded.get(0).equals(m));        
        Assert.assertTrue(messagesForwarded.get(1).equals(m));        
        Assert.assertTrue(testConnection != null);
    }

    public void testSendTwoBeforeReply() throws InterruptedException {
        buffer.put(datagram1, replyConnection1);
        buffer.put(datagram2, replyConnection1);

        Thread.currentThread().sleep(10);

        Assert.assertEquals("forwarded messages", 1, messagesForwarded.size());
        Assert.assertTrue(messagesForwarded.get(0).equals(datagram1));        
        Assert.assertTrue(testConnection != null);
        
        // now send the reply
        
        testConnection.put(replyOK, null);

        Thread.currentThread().sleep(10);

        Assert.assertEquals("forwarded messages", 2, messagesForwarded.size());
        Assert.assertTrue(messagesForwarded.get(1).equals(datagram2));        
    }

    
    // from here down is testing infrastructure
    
    public DatagramMeteringBufferTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DatagramMeteringBufferTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DatagramMeteringBufferTest.class);
        return suite;
    }
}
