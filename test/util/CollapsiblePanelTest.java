package util;

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
public class CollapsiblePanelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CollapsiblePanel t = new CollapsiblePanel("test",new javax.swing.JPanel());
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
