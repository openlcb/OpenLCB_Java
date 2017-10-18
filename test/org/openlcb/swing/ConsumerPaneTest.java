package org.openlcb.swing;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import java.awt.GraphicsEnvironment;
import org.openlcb.*;
import org.openlcb.implementations.SingleConsumerNode;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ConsumerPaneTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeID nodeID = new NodeID(new byte[]{1,2,3,4,5,6});
        EventID eventID = new EventID(new byte[]{1,0,0,0,0,0,1,0});
        Connection testConnection = new AbstractConnection(){
            public void put(Message msg, Connection sender) {
            }
        };
        SingleConsumerNode node = new SingleConsumerNode(
                                            nodeID,
                                            testConnection,
                                            eventID);
                                            
        node.initialize();
        ConsumerPane t = new ConsumerPane("test",node);
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
