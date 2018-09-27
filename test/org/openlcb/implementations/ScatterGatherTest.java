package org.openlcb.implementations;

import org.openlcb.*;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class ScatterGatherTest {

    NodeID nodeID = new NodeID(new byte[]{1,2,3,4,5,6});
    
    abstract class TestListener extends AbstractConnection {
        public void put(Message m, Connection n) {
            setResult();
        }
        abstract void setResult();
    }
    
    boolean result1;
    boolean result2;
    boolean result3;

    @Test    
    public void testCreate() {
        new ScatterGather();
    }
    
    @Test 
    public void testGetConnection() {
        ScatterGather sg = new ScatterGather();
        Connection c = sg.getConnection();
    }
    
    @Test 
    public void testPassThrough() {
        ScatterGather sg = new ScatterGather();
        result1 = false;
        TestListener t1 = new TestListener() {
            void setResult() { result1 = true;}
        };
        Connection c1 = sg.getConnection();      
        result2 = false;
        TestListener t2 = new TestListener() {
            void setResult() { result2 = true;}
        };
        Connection c2 = sg.getConnection();      
        result3 = false;
        TestListener t3 = new TestListener() {
            void setResult() { result3 = true;}
        };
        Connection c3 = sg.getConnection();      
        Message m = new Message(nodeID)
            {public int getMTI() {return 0; }};

        sg.register(t1);
        sg.register(t2);
        sg.register(t3);
        c1.put(m, t1);
        
        Assert.assertTrue(!result1);
        Assert.assertTrue(result2);
        Assert.assertTrue(result3);
    }
    
    @Test 
    public void testNoEcho() {
        ScatterGather sg = new ScatterGather();
        result1 = false;
        TestListener t = new TestListener() {
            void setResult() { result1 = true; }
        };
        Connection c1 = sg.getConnection();      
        Message m = new Message(nodeID)
            {public int getMTI() {return 0; }};

        sg.register(t);
        c1.put(m, t);
        
        Assert.assertTrue(!result1);
    }
    
}
