package org.openlcb.implementations.throttle;

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
public class ThrottleFunctionDatagramTest {

    @Test
    public void testCTor() {
        ThrottleFunctionDatagram t = new ThrottleFunctionDatagram(0,0);
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
