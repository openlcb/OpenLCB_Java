package tools.cansim;

import org.junit.*;
import tools.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class CanSegmentTest  {
    @Test
    @Ignore("no test here")
    public void testStart() {
    }

    @Test
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
    @Test
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
}
