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
public class FakeMemoryConfigurationServiceTest {

    @Test
    public void testCTor() {
        OlcbInterface oi = new FakeOlcbInterface();
        FakeMemoryConfigurationService t = new FakeMemoryConfigurationService(oi);
        Assert.assertNotNull("exists",t);
        t.dispose();
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
