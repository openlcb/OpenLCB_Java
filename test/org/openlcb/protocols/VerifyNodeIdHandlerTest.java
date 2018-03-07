package org.openlcb.protocols;

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
public class VerifyNodeIdHandlerTest {

    @Test
    public void testCTor() {
        NodeID nodeID = new NodeID(new byte[]{1,2,3,4,5,6});
        Connection testConnection = new AbstractConnection(){
            public void put(Message msg, Connection node) {
            }
        };
        OlcbInterface oi = new OlcbInterface(nodeID,testConnection);
        VerifyNodeIdHandler t = new VerifyNodeIdHandler(nodeID,oi);
        Assert.assertNotNull("exists",t);
        oi.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

}
