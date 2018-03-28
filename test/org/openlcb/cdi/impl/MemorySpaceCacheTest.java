package org.openlcb.cdi.impl;

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
public class MemorySpaceCacheTest {

    private OlcbInterface oi;
    private NodeID nodeID;
 
    @Test
    public void testCTor() {
        MemorySpaceCache t = new MemorySpaceCache(oi,nodeID,42);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        nodeID = new NodeID(new byte[]{1,2,3,4,5,6});
        Connection testConnection = new AbstractConnection(){
            public void put(Message msg, Connection node) {
            }
        };
        oi = new OlcbInterface(nodeID,testConnection);
    }

    @After
    public void tearDown() {
        oi.dispose();
        oi = null;
        nodeID = null;
    }

}
