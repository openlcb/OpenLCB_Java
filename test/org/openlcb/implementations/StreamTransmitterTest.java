package org.openlcb.implementations;

import org.junit.Assert;
import org.junit.Test;
import org.openlcb.AbstractConnection;
import org.openlcb.Connection;
import org.openlcb.Message;
import org.openlcb.NodeID;
import org.openlcb.StreamDataCompleteMessage;
import org.openlcb.StreamDataProceedMessage;
import org.openlcb.StreamDataSendMessage;
import org.openlcb.StreamInitiateReplyMessage;
import org.openlcb.StreamInitiateRequestMessage;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class StreamTransmitterTest {
    NodeID hereID = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID farID  = new NodeID(new byte[]{1,1,1,1,1,1});
    byte destID = 13;

    int[] data;

    java.util.ArrayList<Message> messagesReceived;
   
    @Test 
    public void testInitialization() {
        messagesReceived = new java.util.ArrayList<Message>();
        Connection testConnection = new AbstractConnection(){
            @Override
            public void put(Message msg, Connection sender) {
                messagesReceived.add(msg);
            }
        };
        new StreamTransmitter(hereID, farID, 64, data, testConnection);
                                                    
        Assert.assertTrue(messagesReceived.size() == 1); // startup message
        Assert.assertEquals(new StreamInitiateRequestMessage(
                hereID, farID, 64, (byte)4, (byte)0), messagesReceived.get(0));
    }
    
    @Test 
    public void testShortStream() {
        messagesReceived = new java.util.ArrayList<Message>();
        Connection testConnection = new AbstractConnection(){
            @Override
            public void put(Message msg, Connection sender) {
                messagesReceived.add(msg);
            }
        };
        
        data = new int[256];
        
        StreamTransmitter xmt = new StreamTransmitter(hereID,farID, 256, data, testConnection);
                                                    
        Assert.assertEquals("init messages", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0)
                .equals(new StreamInitiateRequestMessage(hereID, farID, 256, (byte)4, (byte)0)));
                           
        // OK 256 byte buffers
        Message m = new StreamInitiateReplyMessage(farID, hereID, 256, (byte)4, destID);
        messagesReceived = new java.util.ArrayList<Message>();

        xmt.put(m, null);

        Assert.assertEquals("1st messages", 2, messagesReceived.size());
        Assert.assertEquals(messagesReceived.get(0), new StreamDataSendMessage(
                hereID, farID, destID, data));
        Assert.assertEquals(messagesReceived.get(1), new StreamDataCompleteMessage(
                hereID, farID, (byte)4, destID));
    }
    
    @Test 
    public void testTwoMsgStream() {
        messagesReceived = new java.util.ArrayList<Message>();
        Connection testConnection = new AbstractConnection(){
            @Override
            public void put(Message msg, Connection sender) {
                messagesReceived.add(msg);
            }
        };
        
        data = new int[512];
        
        StreamTransmitter xmt = new StreamTransmitter(hereID,farID, 256, data, testConnection);
                                                    
        Assert.assertEquals("init messages", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0)
                .equals(new StreamInitiateRequestMessage(hereID, farID, 256, (byte)4, (byte)0)));
                           
        // OK 256 byte buffers
        Message m = new StreamInitiateReplyMessage(farID, hereID, 256, (byte)4, (byte)13);
        messagesReceived = new java.util.ArrayList<Message>();

        xmt.put(m, null);

        // should get a data message
        Assert.assertEquals("1st messages", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0)
                .equals(new StreamDataSendMessage(hereID, farID, (byte)13, new int[256])));

        // reply to proceed
        m = new StreamDataProceedMessage(farID, hereID, (byte)4, (byte)0);
        messagesReceived = new java.util.ArrayList<Message>();

        xmt.put(m, null);

        // 2nd message should be followed by a Stream Data Complete message
        Assert.assertEquals("2nd messages", 2, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0)
                .equals(new StreamDataSendMessage(hereID, farID, (byte)13, new int[256])));
        Assert.assertTrue(messagesReceived.get(1)
                .equals(new StreamDataCompleteMessage(hereID, farID, (byte)4, (byte) 13)));
    }
}
