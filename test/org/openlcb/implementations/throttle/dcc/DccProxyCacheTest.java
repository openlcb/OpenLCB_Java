package org.openlcb.implementations.throttle.dcc;

import org.openlcb.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openlcb.implementations.throttle.*;

/**
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class DccProxyCacheTest extends TestCase {
    DccProxyCache cache;
    
    public void testSetup() {
        Assert.assertTrue(cache != null);
    }
    
    public void testEmpty() {
        Assert.assertTrue(cache.getList() != null);
        Assert.assertEquals(0, cache.getList().size());
    }
    
    public void testIgnoresMessage() {
    
        Message m = new OptionalIntRejectedMessage(null, null, 0, 0);
        
        cache.put(m, null);
        Assert.assertTrue(cache.getList() != null);
        Assert.assertEquals(0, cache.getList().size());
    }
    
    public void testIgnoresEvent() {
    
        Message m = new ProducerConsumerEventReportMessage(null, new EventID("01.02.03.04.05.06.07.08"));
        
        cache.put(m, null);
        Assert.assertTrue(cache.getList() != null);
        Assert.assertEquals(0, cache.getList().size());
    }
    
    public void testSeesIsTrainEvent() {
    
        Message m = new ProducerConsumerEventReportMessage(new NodeID(new byte[]{1,1,0,0,4,4}), new EventID("01.01.00.00.00.00.04.01"));
        
        cache.put(m, null);
        Assert.assertTrue(cache.getList() != null);
        Assert.assertEquals(1, cache.getList().size());
        
        TrainNode tn = cache.getList().get(0);
        
        Assert.assertTrue(tn.getNode().equals(new NodeID(new byte[]{1,1,0,0,4,4})));
        
    }
    
    public void setUp() {
        cache = new DccProxyCache();
    }
    
    // from here down is testing infrastructure
    
    public DccProxyCacheTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DccProxyCacheTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DccProxyCacheTest.class);
        return suite;
    }
}
