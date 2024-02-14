package org.openlcb.implementations.throttle;

import org.openlcb.*;
import org.openlcb.implementations.DatagramService;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2012
 */
public class ThrottleImplementationTest {
    
    NodeID hereID = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID farID = new NodeID(new byte[]{1,2,3,4,5,7});
    Connection testConnection;
    java.util.ArrayList<Message> messagesReceived;
    boolean flag;
    DatagramService service;
    MimicNodeStore store;

    @Before
    public void setUp() {
        messagesReceived = new java.util.ArrayList<Message>();
        flag = false;
        testConnection = new AbstractConnection(){
            @Override
            public void put(Message msg, Connection sender) {
                messagesReceived.add(msg);
            }
        };
        service = new DatagramService(hereID, testConnection);
        store = new MimicNodeStore(testConnection, hereID);
    }

    @After
    public void tearDown() {
       store.dispose();
       store = null;
       service = null;
       testConnection = null;
       messagesReceived = null;
    }

    @Test
    public void testCtors() {
        new ThrottleImplementation(1234, true, store, service);
        new ThrottleImplementation(3, true, store, service);
        new ThrottleImplementation(3, false, store, service);       
    }
    
    @Test
    public void testStartUnknownNode() {
        ThrottleImplementation t = new ThrottleImplementation(1234, true, store, service);
        t.start();
        
        Assert.assertEquals(messagesReceived.size(), 1);
        Assert.assertTrue(messagesReceived.get(0) instanceof VerifyNodeIDNumberGlobalMessage);
        VerifyNodeIDNumberGlobalMessage v = (VerifyNodeIDNumberGlobalMessage)messagesReceived.get(0);
        
        Assert.assertEquals(new NodeID(new byte[]{0x06, 0x01, 0,0, (byte)(1234/256 | 0xC0), (byte)(1234&0xFF)}), v.getContent());

    }
    
    @Test
    public void testSetSpeed100() {
        ThrottleImplementation t = new ThrottleImplementation(1234, true, store, service);
        t.start();
        messagesReceived = new java.util.ArrayList<Message>();
        
        t.setSpeed(100.0, true);
        Assert.assertEquals(messagesReceived.size(), 1);
        Assert.assertTrue(messagesReceived.get(0) instanceof DatagramMessage);
        
        int[] content = ((DatagramMessage)messagesReceived.get(0)).getData();
        Assert.assertEquals(4, content.length);
        Assert.assertEquals(0x30, content[0]);
        Assert.assertEquals(0x01, content[1]);
        Assert.assertEquals(0x56, content[2]);
        Assert.assertEquals(0x40, content[3]);
    }

    @Test
    public void testSetSpeedZero() {
        ThrottleImplementation t = new ThrottleImplementation(1234, true, store, service);
        t.start();
        messagesReceived = new java.util.ArrayList<Message>();
        
        t.setSpeed(0.0, true);
        Assert.assertEquals(messagesReceived.size(), 1);
        Assert.assertTrue(messagesReceived.get(0) instanceof DatagramMessage);
        
        int[] content = ((DatagramMessage)messagesReceived.get(0)).getData();
        Assert.assertEquals(4, content.length);
        Assert.assertEquals(0x30, content[0]);
        Assert.assertEquals(0x01, content[1]);
        Assert.assertEquals(0x00, content[2]);
        Assert.assertEquals(0x00, content[3]);
    }
    
    @Test
    public void testSetSpeedReverseZero() {
        ThrottleImplementation t = new ThrottleImplementation(1234, true, store, service);
        t.start();
        messagesReceived = new java.util.ArrayList<Message>();
        
        t.setSpeed(0.0, false);
        Assert.assertEquals(messagesReceived.size(), 1);
        Assert.assertTrue(messagesReceived.get(0) instanceof DatagramMessage);
        
        int[] content = ((DatagramMessage)messagesReceived.get(0)).getData();
        Assert.assertEquals(4, content.length);
        Assert.assertEquals(0x30, content[0]);
        Assert.assertEquals(0x01, content[1]);
        Assert.assertEquals(0x80, content[2]);
        Assert.assertEquals(0x00, content[3]);
    }
    
    @Test
    public void testSetF1On() {
        ThrottleImplementation t = new ThrottleImplementation(1234, true, store, service);
        t.start();
        messagesReceived = new java.util.ArrayList<Message>();
        
        t.setFunction(1, 1); // F0 is on
        
        Assert.assertEquals(messagesReceived.size(), 1);
        Assert.assertTrue(messagesReceived.get(0) instanceof DatagramMessage);
        
        int[] content = ((DatagramMessage)messagesReceived.get(0)).getData();
        Assert.assertEquals(8, content.length);
        Assert.assertEquals(0x20, content[0]);
        Assert.assertEquals(0x00, content[1]); // command
        Assert.assertEquals(0x00, content[2]);
        Assert.assertEquals(0x00, content[3]);
        Assert.assertEquals(0x00, content[4]);
        Assert.assertEquals(1,    content[5]);  // address
        Assert.assertEquals(0xF9, content[6]); // address space
        Assert.assertEquals(0x01, content[7]); // data
        
    }
    
    @Test
    public void testSetF2Off() {
        ThrottleImplementation t = new ThrottleImplementation(1234, true, store, service);
        t.start();
        messagesReceived = new java.util.ArrayList<Message>();
        
        t.setFunction(2, 0); // F0 is on
        
        Assert.assertEquals(messagesReceived.size(), 1);
        Assert.assertTrue(messagesReceived.get(0) instanceof DatagramMessage);
        
        int[] content = ((DatagramMessage)messagesReceived.get(0)).getData();
        Assert.assertEquals(8, content.length);
        Assert.assertEquals(0x20, content[0]);
        Assert.assertEquals(0x00, content[1]); // command
        Assert.assertEquals(0x00, content[2]);
        Assert.assertEquals(0x00, content[3]);
        Assert.assertEquals(0x00, content[4]);
        Assert.assertEquals(2,    content[5]);  // address
        Assert.assertEquals(0xF9, content[6]); // address space
        Assert.assertEquals(0x00, content[7]); // data
    }
}
