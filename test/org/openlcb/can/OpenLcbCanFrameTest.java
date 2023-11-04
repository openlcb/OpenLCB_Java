
package org.openlcb.can;

import org.junit.*;
import org.openlcb.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class OpenLcbCanFrameTest  {

    @Test
    @Ignore("no test here")
    public void testStart() {
    }

    @Test
    public void testSimpleEquals() {
        CanFrame cf12a = new OpenLcbCanFrame(12);
        CanFrame cf12b = new OpenLcbCanFrame(12);
        CanFrame cf13 = new OpenLcbCanFrame(13);
        
        Assert.assertTrue("12a equals 12a", cf12a.equals(cf12a));
        Assert.assertTrue("12a equals 12b", cf12a.equals(cf12b));
        Assert.assertTrue("12a not equals 13", !cf12a.equals(cf13));
        Assert.assertEquals("hashcodes equal when equal",cf12a.hashCode(),cf12b.hashCode());
    }
    
    @Test
    public void testArrayGet() {
        OpenLcbCanFrame cf12a = new OpenLcbCanFrame(12);
        cf12a.setData(new byte[]{10,20,30});
        
        byte[] b = cf12a.getData();
        Assert.assertEquals(3, b.length);
        Assert.assertEquals(10, b[0]);
        Assert.assertEquals(20, b[1]);
        Assert.assertEquals(30, b[2]);
    }
    
    @Test
    public void testSimpleEqualObject() {
        Object cf12a = new OpenLcbCanFrame(12);
        Object cf12b = new OpenLcbCanFrame(12);
        Object cf13 = new OpenLcbCanFrame(13);
        
        Assert.assertTrue("12a equals 12a", cf12a.equals(cf12a));
        Assert.assertTrue("12a equals 12b", cf12a.equals(cf12b));
        Assert.assertTrue("12a not equals 13", !cf12a.equals(cf13));
    }
        
    @Test
    public void testBasicFieldMaps(){
        OpenLcbCanFrame f;
        f = new OpenLcbCanFrame(0xFFF);
        Assert.assertEquals("sourceAlias 1s", 0xFFF, f.getSourceAlias());
        
    }
    
    @Test
    @Ignore("only declares a value. Never initializes the value")
    public void testGetTypeField(){
        OpenLcbCanFrame c;
              
    }
    
    @Test
    public void testInitializationComplete(){
        OpenLcbCanFrame f = new OpenLcbCanFrame(0x123);
        f.setInitializationComplete(0x123, new NodeID(new byte[]{0,1,2,3,4,5}));
        Assert.assertTrue("isIC", f.isInitializationComplete());         
    }

    @Test
    public void testMakeCim() {
        OpenLcbCanFrame f = new OpenLcbCanFrame(123);
        f.setCIM(1, 2, 123);
        Assert.assertTrue(f.isCIM());
        Assert.assertTrue(!f.isRIM());
    }
    
    @Test
    public void testMakeRim() {
        OpenLcbCanFrame f = new OpenLcbCanFrame(123);
        f.setRIM(123);
        Assert.assertTrue(!f.isCIM());
        Assert.assertTrue(f.isRIM());
    }
    
    @Test
    public void testPCERforms() {
        OpenLcbCanFrame f = new OpenLcbCanFrame(123);
        f.setPCEventReport(new EventID("1.2.3.4.5.6.7.8"));
        
        Assert.assertTrue(f.isPCEventReport());
        
        f = new OpenLcbCanFrame(123);
        f.setPCEventReport(new EventID("1.2.3.4.5.6.7.8"), MessageTypeIdentifier.ProducerConsumerEventReport);

        Assert.assertTrue(f.isPCEventReport());

        f = new OpenLcbCanFrame(123);
        f.setPCEventReport(new EventID("1.2.3.4.5.6.7.8"), MessageTypeIdentifier.PCERfirst);

        Assert.assertTrue(f.isPCEventReport());

        f = new OpenLcbCanFrame(123);
        f.setPCEventReport(new EventID("1.2.3.4.5.6.7.8"), MessageTypeIdentifier.PCERlast);

        Assert.assertFalse(f.isPCEventReport());
    }
    
}
