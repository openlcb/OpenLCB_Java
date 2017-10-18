package org.openlcb.can.impl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlcb.*;
import org.openlcb.can.CanFrameListenerScaffold;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class GridConnectInputTest {

    @Test
    public void testCTor() {
        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.StringReader(""));
        GridConnectInput t = new GridConnectInput(br, new CanFrameListenerScaffold(), new Runnable(){
    public void run(){
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
