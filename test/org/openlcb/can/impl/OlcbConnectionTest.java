package org.openlcb.can.impl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlcb.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class OlcbConnectionTest {

    @Test
    public void testCTor() {
        NodeID nodeID = new NodeID(new byte[]{1,2,3,4,5,6});
        OlcbConnection t = new OlcbConnection(nodeID,"test",5,new OlcbConnection.ConnectionListener(){
            @Override
            public void onConnect(){
            }
            @Override
            public void onDisconnect(){
            }
            @Override
            public void onStatusChange(String status){
            }
            @Override
            public void onConnectionPending(){
            }
});
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

}
