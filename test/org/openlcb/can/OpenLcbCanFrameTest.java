
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
        
    public void testSetTypeFieldBasic(){
        Assert.assertEquals("0x00, CheckID ", 0x00, 
                OpenLcbCanFrame.setTypeField(0,OpenLcbCanFrame.TypeField.CHECKIDMESSAGE));
        Assert.assertEquals("0x01, CheckID ", 0x01, 
                OpenLcbCanFrame.setTypeField(1,OpenLcbCanFrame.TypeField.CHECKIDMESSAGE));

        Assert.assertEquals("0x00, ReservedID ", 0x04000000, 
                OpenLcbCanFrame.setTypeField(0,OpenLcbCanFrame.TypeField.RESERVEDIDMESSAGE));
        Assert.assertEquals("0x01, ReservedID ", 0x04000001, 
                OpenLcbCanFrame.setTypeField(1,OpenLcbCanFrame.TypeField.RESERVEDIDMESSAGE));

        Assert.assertEquals("0x00, OpenLCB message ", 0x0C000000, 
                OpenLcbCanFrame.setTypeField(0,OpenLcbCanFrame.TypeField.NMRANETCOMMONMESSAGE));
        Assert.assertEquals("0x01, OpenLCB message ", 0x0C000001, 
                OpenLcbCanFrame.setTypeField(1,OpenLcbCanFrame.TypeField.NMRANETCOMMONMESSAGE));

        Assert.assertEquals("0x00, CAN msg ", 0x08000000, 
                OpenLcbCanFrame.setTypeField(0,OpenLcbCanFrame.TypeField.CANMESSAGE));
        Assert.assertEquals("0x01, CAN msg ", 0x08000001, 
                OpenLcbCanFrame.setTypeField(1,OpenLcbCanFrame.TypeField.CANMESSAGE));

    }
    
    public void testGetTypeField(){
        OpenLcbCanFrame c;
        c = new OpenLcbCanFrame(
                                OpenLcbCanFrame.setTypeField(1,
                                    OpenLcbCanFrame.TypeField.CHECKIDMESSAGE));
        Assert.assertEquals("CHECKIDMESSAGE", OpenLcbCanFrame.TypeField.CHECKIDMESSAGE, c.getTypeField());
              
        c = new OpenLcbCanFrame(
                                OpenLcbCanFrame.setTypeField(1,
                                    OpenLcbCanFrame.TypeField.RESERVEDIDMESSAGE));
        Assert.assertEquals("RESERVEDIDMESSAGE", OpenLcbCanFrame.TypeField.RESERVEDIDMESSAGE, c.getTypeField());
              
        c = new OpenLcbCanFrame(
                                OpenLcbCanFrame.setTypeField(1,
                                    OpenLcbCanFrame.TypeField.NMRANETCOMMONMESSAGE));
        Assert.assertEquals("NMRANETCOMMONMESSAGE", OpenLcbCanFrame.TypeField.NMRANETCOMMONMESSAGE, c.getTypeField());
              
        c = new OpenLcbCanFrame(
                                OpenLcbCanFrame.setTypeField(1,
                                    OpenLcbCanFrame.TypeField.CANMESSAGE));
        Assert.assertEquals("CANMESSAGE", OpenLcbCanFrame.TypeField.CANMESSAGE, c.getTypeField());
              
    }

    public void testTypeFieldCoding() {
        Assert.assertEquals("count",4,  OpenLcbCanFrame.TypeField.values().length);
        Assert.assertEquals("CHECKIDMESSAGE", 0, OpenLcbCanFrame.TypeField.CHECKIDMESSAGE.ordinal());
        Assert.assertEquals("RESERVEDIDMESSAGE", 1, OpenLcbCanFrame.TypeField.RESERVEDIDMESSAGE.ordinal());
        Assert.assertEquals("CANMESSAGE", 2, OpenLcbCanFrame.TypeField.CANMESSAGE.ordinal());
        Assert.assertEquals("NMRANETCOMMONMESSAGE", 3, OpenLcbCanFrame.TypeField.NMRANETCOMMONMESSAGE.ordinal());
    }
    
    public void testMakeCim() {
        OpenLcbCanFrame f = OpenLcbCanFrame.makeCimFrame(0, 0, 0);
        Assert.assertTrue(f.isCIM());
        Assert.assertTrue(!f.isRIM());
    }
    
    public void testMakeRim() {
        OpenLcbCanFrame f = OpenLcbCanFrame.makeRimFrame(0, 
                                        new NodeID(new byte[]{10,11,12,13,14,15}));
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
