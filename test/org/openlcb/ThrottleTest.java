package org.openlcb;

import org.openlcb.implementations.DatagramService;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class ThrottleTest extends TestCase {


    NodeID hereID = new NodeID(new byte[]{1,2,3,4,5,10});
    NodeID farID = new NodeID(new byte[]{1,2,3,4,5,7});
    Connection testConnection;
    java.util.ArrayList<Message> messagesReceived;
    boolean flag;
    DatagramService datagramService;
    
    @Override
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


    public void testCtor() {
        new Throttle(farID, datagramService);
    }
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
    
    // from here down is testing infrastructure
    
    public ThrottleTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ThrottleTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ThrottleTest.class);
        return suite;
    }
}
