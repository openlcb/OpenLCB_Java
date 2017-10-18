package org.openlcb.implementations;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import java.awt.GraphicsEnvironment;
import org.openlcb.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class BlueGoldExtendedEngineTest {

    @Test
    public void testCTor() {
        NodeID nodeID = new NodeID(new byte[]{1,2,3,4,5,6});
        ScatterGather sg = new ScatterGather();
        EventID eid = new EventID(new byte[]{1,2,3,4,5,6,7,8});
        Connection testConnection = new AbstractConnection(){
            public void put(Message msg, Connection node) {
            }
        };
        java.util.ArrayList<SingleProducer> spl = new java.util.ArrayList<SingleProducer>();
        java.util.ArrayList<SingleConsumer> scl = new java.util.ArrayList<SingleConsumer>();

        spl.add(new SingleProducer(nodeID,testConnection,eid));
        scl.add(new SingleConsumer(nodeID,testConnection,eid));
        BlueGoldExtendedEngine t = new BlueGoldExtendedEngine(nodeID,sg,spl,scl);
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
