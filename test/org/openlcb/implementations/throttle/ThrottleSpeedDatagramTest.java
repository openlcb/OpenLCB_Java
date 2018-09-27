package org.openlcb.implementations.throttle;

import org.openlcb.*;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2012
 */
public class ThrottleSpeedDatagramTest {
   
    @Test	
    public void testZeroSpeed() {
        ThrottleSpeedDatagram t = new ThrottleSpeedDatagram(0.0, true);
        
        int[] content = t.getData();
        
        Assert.assertEquals(4, content.length);
        Assert.assertEquals(0x30, content[0]);
        Assert.assertEquals(0x01, content[1]);
        Assert.assertEquals(0x00, content[2]);
        Assert.assertEquals(0x00, content[3]);
        
    }
    
    @Test	
    public void testNegZeroSpeed() {
        ThrottleSpeedDatagram t = new ThrottleSpeedDatagram(0.0f, false);
        
        int[] content = t.getData();
        
        Assert.assertEquals(4, content.length);
        Assert.assertEquals(0x30, content[0]);
        Assert.assertEquals(0x01, content[1]);
        Assert.assertEquals(0x80, content[2]);
        Assert.assertEquals(0x00, content[3]);
        
    }
    
    @Test	
    public void test100Speed() {
        ThrottleSpeedDatagram t = new ThrottleSpeedDatagram(100.0, true);
        
        int[] content = t.getData();
        
        Assert.assertEquals(4, content.length);
        Assert.assertEquals(0x30, content[0]);
        Assert.assertEquals(0x01, content[1]);
        Assert.assertEquals(0x56, content[2]);
        Assert.assertEquals(0x40, content[3]);
        
    }

    @Test	
    public void testNeg100Speed() {
        ThrottleSpeedDatagram t = new ThrottleSpeedDatagram(100.0, false);
        
        int[] content = t.getData();
        
        Assert.assertEquals(4, content.length);
        Assert.assertEquals(0x30, content[0]);
        Assert.assertEquals(0x01, content[1]);
        Assert.assertEquals(0xD6, content[2]);
        Assert.assertEquals(0x40, content[3]);
        
    }

    @Test	
    public void testEStop() {
        ThrottleSpeedDatagram t = new ThrottleSpeedDatagram();
        
        int[] content = t.getData();
        
        Assert.assertEquals(2, content.length);
        Assert.assertEquals(0x30, content[0]);
        Assert.assertEquals(0x00, content[1]);        
    }
    
}
