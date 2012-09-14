package org.openlcb.implementations.throttle;

import org.openlcb.*;
import org.openlcb.implementations.DatagramService;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class ThrottleImplementationTest extends TestCase {
    
    NodeID hereID = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID farID = new NodeID(new byte[]{1,2,3,4,5,7});
    Connection testConnection;
    java.util.ArrayList<Message> messagesReceived;
    boolean flag;
    DatagramService service;
    MimicNodeStore store;
    
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
        service = new DatagramService(hereID, testConnection);
        store = new MimicNodeStore(testConnection, hereID);
    }

    public void testCtors() {
        new ThrottleImplementation(1234, true, store, service);
        new ThrottleImplementation(3, true, store, service);
        new ThrottleImplementation(3, false, store, service);       
    }
    
    public void testStartUnknownNode() {
        ThrottleImplementation t = new ThrottleImplementation(1234, true, store, service);
        t.start();
        
        Assert.assertEquals(messagesReceived.size(), 1);
        Assert.assertTrue(messagesReceived.get(0) instanceof VerifyNodeIDNumberMessage);
        VerifyNodeIDNumberMessage v = (VerifyNodeIDNumberMessage)messagesReceived.get(0);
        
        Assert.assertEquals(new NodeID(new byte[]{0x06, 0x01, 0,0, 1234/256, (byte)(1234&0xFF)}), v.getContent());

    }
    
    public void testSetSpeed() {
        ThrottleImplementation t = new ThrottleImplementation(1234, true, store, service);
        t.start();
        messagesReceived = new java.util.ArrayList<Message>();
        
        t.setSpeed(0.0);
        Assert.assertEquals(messagesReceived.size(), 1);
        Assert.assertTrue(messagesReceived.get(0) instanceof DatagramMessage);
        
        int[] content = ((DatagramMessage)messagesReceived.get(0)).getData();
        Assert.assertEquals(4, content.length);
        Assert.assertEquals(0x30, content[0]);
        Assert.assertEquals(0x01, content[1]);
        Assert.assertEquals(0x00, content[2]);
        Assert.assertEquals(0x00, content[3]);
    }
    
    // from here down is testing infrastructure
    
    public ThrottleImplementationTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ThrottleImplementationTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ThrottleImplementationTest.class);
        return suite;
    }
}
