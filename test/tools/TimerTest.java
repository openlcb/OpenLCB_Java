package tools;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class TimerTest  {

    @Test
    @Ignore("no test here")
    public void testStart() {
    }

    @Test
    public void testSteps() {
        Timer t = new Timer();
        t.run(4);
    }

    long count;
    @Test
    public void testStepOneTimed() {
        Timer t = new Timer();
        Timed d = new Timed(){
            public void tick(long time) { count = time; }
        };
        t.add(d);
        count = -1;
        
        t.run(1);
        
        Assert.assertEquals("one step at t=0", 0, count);
    }

    @Test
    public void testStepTimedSeveral() {
        Timer t = new Timer();
        Timed d = new Timed(){
            public void tick(long time) { count = time; }
        };
        t.add(d);
        count = -1;
        
        t.run(3);
        
        Assert.assertEquals("three steps to t=2", 2, count);
    }
}
