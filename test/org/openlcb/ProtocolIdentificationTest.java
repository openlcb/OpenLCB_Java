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
        java.util.List result = ProtocolIdentification.Protocols.decode(0x000000000000L);
        
        Assert.assertEquals("length", 0, result.size());
    }
    
    public void testDecode1() {
        java.util.List result = ProtocolIdentification.Protocols.decode(0x800000000000L);
        
        Assert.assertEquals("length", 1, result.size());
        Assert.assertEquals("result 1", "ProtocolIdentification", result.get(0));
    }
    
    public void testDecode2() {
        java.util.List result = ProtocolIdentification.Protocols.decode(0x880000000000L);
        
        Assert.assertEquals("length", 2, result.size());
        Assert.assertEquals("result 1", "ProtocolIdentification", result.get(0));
        Assert.assertEquals("result 2", "Reservation", result.get(1));
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
