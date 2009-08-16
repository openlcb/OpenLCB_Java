package tools.cansim;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import tools.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class CanSegmentTest extends TestCase {
    public void testStart() {
    }

    public void testSendOne() {
        CanSegment seg = new CanSegment();
        CanInterface sender = new CanInterface(){
            public void tick(long time){Assert.fail("tick() should not be called");}
            public void done(){}
            public void receive(CanFrame f){Assert.fail("receive() should not be called");}
        };
        seg.add(sender);
        
        seg.send(new CanFrame(12), sender);
        
    }
    
    boolean received = false;
    boolean done = false;
    public void testSendAndReceiveOne() {
        CanSegment seg = new CanSegment();
        CanInterface sender = new CanInterface(){
            public void tick(long time){Assert.fail("bd1 tick() should not be called");}
            public void done(){ done = true; }
            public void receive(CanFrame f){Assert.fail("bd1 receive() should not be called");}
        };
        CanInterface receiver = new CanInterface(){
            public void tick(long time){Assert.fail("tick() should not be called");}
            public void done(){Assert.fail("done() should not be called");}
            public void receive(CanFrame f){ received = true; }
        };
        seg.add(sender);
        seg.add(receiver);
        
        seg.send(new CanFrame(12), sender);
        
        Assert.assertTrue("received frame", received);
    }
    
    
    // from here down is testing infrastructure
    
    public CanSegmentTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {CanSegmentTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CanSegmentTest.class);
        return suite;
    }
}
