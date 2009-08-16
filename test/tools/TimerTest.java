package tools;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class TimerTest extends TestCase {
    public void testStart() {
    }

    public void testSteps() {
        Timer t = new Timer();
        t.run(4);
    }

    long count;
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

    
    // from here down is testing infrastructure
    
    public TimerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {TimerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TimerTest.class);
        return suite;
    }
}
