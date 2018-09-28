package tools.cansim;

import org.junit.*;
import tools.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class CanFrameTest  {

    @Test
    @Ignore("no test here")
    public void testStart() {
    }

    @Test
    public void testSimpleEquals() {
        CanFrame cf12a = new CanFrame(12);
        CanFrame cf12b = new CanFrame(12);
        CanFrame cf13 = new CanFrame(13);
        
        Assert.assertTrue("12a equals 12a", cf12a.equals(cf12a));
        Assert.assertTrue("12a equals 12b", cf12a.equals(cf12b));
        Assert.assertTrue("12a not equals 13", !cf12a.equals(cf13));
    }
    
    @Test
    public void testSimpleEqualObject() {
        Object cf12a = new CanFrame(12);
        Object cf12b = new CanFrame(12);
        Object cf13 = new CanFrame(13);
        
        Assert.assertTrue("12a equals 12a", cf12a.equals(cf12a));
        Assert.assertTrue("12a equals 12b", cf12a.equals(cf12b));
        Assert.assertTrue("12a not equals 13", !cf12a.equals(cf13));
    }
}
