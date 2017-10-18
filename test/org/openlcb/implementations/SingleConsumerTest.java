package org.openlcb.implementations;

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
public class SingleConsumerTest {

    @Test
    public void testCTor() {
        NodeID nid = new NodeID(new byte[]{1,2,3,4,5,6});
        EventID eid = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        Connection testConnection = new AbstractConnection(){
            public void put(Message msg, Connection node) {
            }
        };
        SingleConsumer t = new SingleConsumer(nid,testConnection,eid);
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
