package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class ProtocolIdentificationTest extends TestCase {
    public void testCtor() {
        new ProtocolIdentification();
    }
    
    public void testDecode0() {
        java.util.List result = ProtocolIdentification.Protocol.decode(0x000000000000L);
        
        Assert.assertEquals("length", 0, result.size());
    }
    
    public void testDecode1() {
        java.util.List result = ProtocolIdentification.Protocol.decodeNames(0x800000000000L);
        
        Assert.assertEquals("length", 1, result.size());
        Assert.assertEquals("result 1", "ProtocolIdentification", result.get(0));
    }
    
    public void testDecode2() {
        java.util.List result = ProtocolIdentification.Protocol.decodeNames(0x880000000000L);
        
        Assert.assertEquals("length", 2, result.size());
        Assert.assertEquals("result 1", "ProtocolIdentification", result.get(0));
        Assert.assertEquals("result 2", "Reservation", result.get(1));
    }

    public void testDecode3() {
        java.util.List result = ProtocolIdentification.Protocol.decodeNames(0xF01800000000L);
        
        Assert.assertEquals("length", 6, result.size());
        Assert.assertEquals("result 1", "ProtocolIdentification", result.get(0));
        Assert.assertEquals("result 2", "Datagram", result.get(1));
        Assert.assertEquals("result 3", "Stream", result.get(2));
        Assert.assertEquals("result 4", "Configuration", result.get(3));
        Assert.assertEquals("result 5", "SNII", result.get(4));
        Assert.assertEquals("result 6", "CDI", result.get(5));
    }

    public void testDecode4() {
        java.util.List result = ProtocolIdentification.Protocol.decodeNames(0x000F00000000L);
        
        Assert.assertEquals("length", 4, result.size());
        Assert.assertEquals("result 1", "CDI", result.get(0));
        Assert.assertEquals("result 2", "Train", result.get(1));
        Assert.assertEquals("result 3", "FDI", result.get(2));
        Assert.assertEquals("result 4", "DccCS", result.get(3));
    }

    public void testSupports1() {
        ProtocolIdentification.Protocol p = ProtocolIdentification.Protocol.Datagram;
        
        Assert.assertTrue("supports", p.supports(~0));
    }
    
    public void testSupports2() {
        ProtocolIdentification.Protocol p = ProtocolIdentification.Protocol.Datagram;
        
        Assert.assertTrue("supports", !p.supports(0));
    }
    
    public void testCreationFromMessage() {
        ProtocolIdentification pi = new ProtocolIdentification(
            new ProtocolIdentificationReplyMessage(
                new NodeID(new byte[]{1,3,3,4,5,6}), 
                0x03));
        Assert.assertTrue((long)0x03 == pi.getValue());
    }
    
    // from here down is testing infrastructure
    
    public ProtocolIdentificationTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ProtocolIdentificationTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ProtocolIdentificationTest.class);
        return suite;
    }
}
