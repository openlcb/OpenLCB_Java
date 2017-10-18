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
public class GridConnectOutputTest {

    @Test
    public void testCTor() {
        GridConnectOutput t = new GridConnectOutput(new java.io.ByteArrayOutputStream(), new Runnable(){
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
