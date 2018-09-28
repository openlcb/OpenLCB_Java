package org.openlcb;

import org.openlcb.implementations.DatagramService;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class ThrottleTest  {

    NodeID hereID = new NodeID(new byte[]{1,2,3,4,5,10});
    NodeID farID = new NodeID(new byte[]{1,2,3,4,5,7});
    Connection testConnection;
    java.util.ArrayList<Message> messagesReceived;
    boolean flag;
    DatagramService datagramService;
    
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
        datagramService = new DatagramService(hereID, testConnection);
    }

    @After
    public void tearDown(){
        messagesReceived = null;
        testConnection = null;
        datagramService = null;
    } 

    @Test
    public void testCtor() {
        Assert.assertNotNull("throttle exists",new Throttle(farID, datagramService));
    }

    @Test
    public void testSend() {
        Throttle t = new Throttle(farID, datagramService);
        
        t.setSpeed(0.0f);
        
        // should have sent datagram
        Assert.assertEquals("Message count",1,messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0) instanceof DatagramMessage);

        // check format of datagram write
        int[] content = ((DatagramMessage)messagesReceived.get(0)).getData();
        Assert.assertTrue(content.length >= 6);
        Assert.assertEquals("datagram type", 0x30, content[0]);
        Assert.assertEquals("set speed command", 0x01, (content[1]));

    }
}
