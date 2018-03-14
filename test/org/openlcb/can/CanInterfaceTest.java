package org.openlcb.can;

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
public class CanInterfaceTest {

    @Test
    public void testCTor() {
        NodeID nodeID = new NodeID(new byte[]{1,2,3,4,5,6});
        CanInterface t = new CanInterface(nodeID, new CanFrameListenerScaffold() );
        Assert.assertNotNull("exists",t);
        t.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

}
