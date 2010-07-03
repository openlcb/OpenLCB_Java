package org.openlcb.implementations;

import org.openlcb.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class ScatterGatherTest extends TestCase {

    NodeID nodeID = new NodeID(new byte[]{1,2,3,4,5,6});
    
    abstract class TestListener implements Connection {
        public void put(Message m, Connection n) {
            setResult();
        }
        abstract void setResult();
    }
    
    boolean result1;
    boolean result2;
    boolean result3;
    
    public void testCreate() {
        new ScatterGather();
    }
    
    public void testGetConnection() {
        ScatterGather sg = new ScatterGather();
        Connection c = sg.getConnection();
    }
    
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
    
    // from here down is testing infrastructure
    
    public ScatterGatherTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ScatterGatherTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ScatterGatherTest.class);
        return suite;
    }
}
