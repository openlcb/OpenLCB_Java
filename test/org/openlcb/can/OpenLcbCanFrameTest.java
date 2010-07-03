
package org.openlcb.can;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.openlcb.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class OpenLcbCanFrameTest extends TestCase {
    public void testStart() {
    }

    public void testSimpleEquals() {
        CanFrame cf12a = new OpenLcbCanFrame(12);
        CanFrame cf12b = new OpenLcbCanFrame(12);
        CanFrame cf13 = new OpenLcbCanFrame(13);
        
        Assert.assertTrue("12a equals 12a", cf12a.equals(cf12a));
        Assert.assertTrue("12a equals 12b", cf12a.equals(cf12b));
        Assert.assertTrue("12a not equals 13", !cf12a.equals(cf13));
    }
    
    public void testSimpleEqualObject() {
        Object cf12a = new OpenLcbCanFrame(12);
        Object cf12b = new OpenLcbCanFrame(12);
        Object cf13 = new OpenLcbCanFrame(13);
        
        Assert.assertTrue("12a equals 12a", cf12a.equals(cf12a));
        Assert.assertTrue("12a equals 12b", cf12a.equals(cf12b));
        Assert.assertTrue("12a not equals 13", !cf12a.equals(cf13));
    }
        
    public void testBasicFieldMaps(){
        OpenLcbCanFrame f;
        f = new OpenLcbCanFrame(0xFFF);
        Assert.assertEquals("sourceAlias 1s", 0xFFF, f.getSourceAlias());
        
    }
    
    public void testGetTypeField(){
        OpenLcbCanFrame c;
              
    }
    
    public void testInitializationComplete(){
        OpenLcbCanFrame f = new OpenLcbCanFrame(0x123);
        f.setInitializationComplete(0x123, new NodeID(new byte[]{0,1,2,3,4,5}));
        Assert.assertTrue("isIC", f.isInitializationComplete());         
    }

    public void testMakeCim() {
        OpenLcbCanFrame f = new OpenLcbCanFrame(123);
        f.setCIM(1, 2, 123);
        Assert.assertTrue(f.isCIM());
        Assert.assertTrue(!f.isRIM());
    }
    
    public void testMakeRim() {
        OpenLcbCanFrame f = new OpenLcbCanFrame(123);
        f.setRIM(123);
        Assert.assertTrue(!f.isCIM());
        Assert.assertTrue(f.isRIM());
    }
    
    // from here down is testing infrastructure
    
    public OpenLcbCanFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {OpenLcbCanFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OpenLcbCanFrameTest.class);
        return suite;
    }
}
