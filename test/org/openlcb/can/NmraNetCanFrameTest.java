package org.nmra.net.can;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.nmra.net.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class NmraNetCanFrameTest extends TestCase {
    public void testStart() {
    }

    public void testSimpleEquals() {
        CanFrame cf12a = new NmraNetCanFrame(12);
        CanFrame cf12b = new NmraNetCanFrame(12);
        CanFrame cf13 = new NmraNetCanFrame(13);
        
        Assert.assertTrue("12a equals 12a", cf12a.equals(cf12a));
        Assert.assertTrue("12a equals 12b", cf12a.equals(cf12b));
        Assert.assertTrue("12a not equals 13", !cf12a.equals(cf13));
    }
    
    public void testSimpleEqualObject() {
        Object cf12a = new NmraNetCanFrame(12);
        Object cf12b = new NmraNetCanFrame(12);
        Object cf13 = new NmraNetCanFrame(13);
        
        Assert.assertTrue("12a equals 12a", cf12a.equals(cf12a));
        Assert.assertTrue("12a equals 12b", cf12a.equals(cf12b));
        Assert.assertTrue("12a not equals 13", !cf12a.equals(cf13));
    }
        
    public void testSetTypeFieldBasic(){
        Assert.assertEquals("0x00, CheckID ", 0x00, 
                NmraNetCanFrame.setTypeField(0,NmraNetCanFrame.TypeField.CHECKIDMESSAGE));
        Assert.assertEquals("0x01, CheckID ", 0x01, 
                NmraNetCanFrame.setTypeField(1,NmraNetCanFrame.TypeField.CHECKIDMESSAGE));

        Assert.assertEquals("0x00, ReservedID ", 0x04000000, 
                NmraNetCanFrame.setTypeField(0,NmraNetCanFrame.TypeField.RESERVEDIDMESSAGE));
        Assert.assertEquals("0x01, ReservedID ", 0x04000001, 
                NmraNetCanFrame.setTypeField(1,NmraNetCanFrame.TypeField.RESERVEDIDMESSAGE));

        Assert.assertEquals("0x00, NMRAnet message ", 0x0C000000, 
                NmraNetCanFrame.setTypeField(0,NmraNetCanFrame.TypeField.NMRANETCOMMONMESSAGE));
        Assert.assertEquals("0x01, NMRAnet message ", 0x0C000001, 
                NmraNetCanFrame.setTypeField(1,NmraNetCanFrame.TypeField.NMRANETCOMMONMESSAGE));

        Assert.assertEquals("0x00, CAN msg ", 0x08000000, 
                NmraNetCanFrame.setTypeField(0,NmraNetCanFrame.TypeField.CANMESSAGE));
        Assert.assertEquals("0x01, CAN msg ", 0x08000001, 
                NmraNetCanFrame.setTypeField(1,NmraNetCanFrame.TypeField.CANMESSAGE));

    }
    
    public void testGetTypeField(){
        NmraNetCanFrame c;
        c = new NmraNetCanFrame(
                                NmraNetCanFrame.setTypeField(1,
                                    NmraNetCanFrame.TypeField.CHECKIDMESSAGE));
        Assert.assertEquals("CHECKIDMESSAGE", NmraNetCanFrame.TypeField.CHECKIDMESSAGE, c.getTypeField());
              
        c = new NmraNetCanFrame(
                                NmraNetCanFrame.setTypeField(1,
                                    NmraNetCanFrame.TypeField.RESERVEDIDMESSAGE));
        Assert.assertEquals("RESERVEDIDMESSAGE", NmraNetCanFrame.TypeField.RESERVEDIDMESSAGE, c.getTypeField());
              
        c = new NmraNetCanFrame(
                                NmraNetCanFrame.setTypeField(1,
                                    NmraNetCanFrame.TypeField.NMRANETCOMMONMESSAGE));
        Assert.assertEquals("NMRANETCOMMONMESSAGE", NmraNetCanFrame.TypeField.NMRANETCOMMONMESSAGE, c.getTypeField());
              
        c = new NmraNetCanFrame(
                                NmraNetCanFrame.setTypeField(1,
                                    NmraNetCanFrame.TypeField.CANMESSAGE));
        Assert.assertEquals("CANMESSAGE", NmraNetCanFrame.TypeField.CANMESSAGE, c.getTypeField());
              
    }

    public void testTypeFieldCoding() {
        Assert.assertEquals("count",4,  NmraNetCanFrame.TypeField.values().length);
        Assert.assertEquals("CHECKIDMESSAGE", 0, NmraNetCanFrame.TypeField.CHECKIDMESSAGE.ordinal());
        Assert.assertEquals("RESERVEDIDMESSAGE", 1, NmraNetCanFrame.TypeField.RESERVEDIDMESSAGE.ordinal());
        Assert.assertEquals("CANMESSAGE", 2, NmraNetCanFrame.TypeField.CANMESSAGE.ordinal());
        Assert.assertEquals("NMRANETCOMMONMESSAGE", 3, NmraNetCanFrame.TypeField.NMRANETCOMMONMESSAGE.ordinal());
    }
    
    public void testMakeCim() {
        NmraNetCanFrame f = NmraNetCanFrame.makeCimFrame(0, 0, 0);
        Assert.assertTrue(f.isCIM());
        Assert.assertTrue(!f.isRIM());
    }
    
    public void testMakeRim() {
        NmraNetCanFrame f = NmraNetCanFrame.makeRimFrame(0, 
                                        new NodeID(new byte[]{10,11,12,13,14,15}));
        Assert.assertTrue(!f.isCIM());
        Assert.assertTrue(f.isRIM());
    }
    
    // from here down is testing infrastructure
    
    public NmraNetCanFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NmraNetCanFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NmraNetCanFrameTest.class);
        return suite;
    }
}
