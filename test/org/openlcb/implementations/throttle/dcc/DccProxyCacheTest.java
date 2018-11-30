package org.openlcb.implementations.throttle.dcc;

import org.openlcb.*;

import org.junit.*;

import org.openlcb.implementations.throttle.*;

/**
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class DccProxyCacheTest {
    DccProxyCache cache;
   
    @Test 
    public void testSetup() {
        Assert.assertTrue(cache != null);
    }
    
    @Test 
    public void testEmpty() {
        Assert.assertTrue(cache.getList() != null);
        Assert.assertEquals(0, cache.getList().size());
    }
    
    @Test 
    public void testIgnoresMessage() {
    
        Message m = new OptionalIntRejectedMessage(null, null, 0, 0);
        
        cache.put(m, null);
        Assert.assertTrue(cache.getList() != null);
        Assert.assertEquals(0, cache.getList().size());
    }
    
    @Test 
    public void testIgnoresEvent() {
    
        Message m = new ProducerConsumerEventReportMessage(null, new EventID("01.02.03.04.05.06.07.08"));
        
        cache.put(m, null);
        Assert.assertTrue(cache.getList() != null);
        Assert.assertEquals(0, cache.getList().size());
    }
    
    @Test 
    public void testSeesIsTrainEvent() {
    
        Message m = new ProducerConsumerEventReportMessage(new NodeID(new byte[]{1,1,0,0,4,4}), new EventID("01.01.00.00.00.00.04.01"));
        
        cache.put(m, null);
        Assert.assertTrue(cache.getList() != null);
        Assert.assertEquals(1, cache.getList().size());
        
        RemoteDccProxy tn = cache.getList().get(0);
        
        Assert.assertTrue(tn.getNodeId().equals(new NodeID(new byte[]{1,1,0,0,4,4})));
        
    }

    @Before    
    public void setUp() {
        cache = new DccProxyCache();
    }

    @After
    public void tearDown() {
        cache = null;
    }
    
    
}
