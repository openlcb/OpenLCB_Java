package org.openlcb.can;

import org.openlcb.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class NIDaTest extends TestCase {
    
    public void testPRNGbuild() {
        na.nextAlias();
    }
    
    // not really checking the sequence, just checking for differences.
    public void testPRNGdiffers() {
        int first = na.getNIDa();
        na.nextAlias();
        int last = na.getNIDa();
        Assert.assertTrue("1", first!=last);
        first = last; 
        na.nextAlias();
        last = na.getNIDa();
        Assert.assertTrue("2", first!=last);
        first = last; 
        na.nextAlias();
        last = na.getNIDa();
        Assert.assertTrue("3", first!=last);
        first = last; 
        na.nextAlias();
        last = na.getNIDa();
        Assert.assertTrue("4", first!=last);
        first = last; 
        na.nextAlias();
        last = na.getNIDa();
        Assert.assertTrue("5", first!=last);
    }
    
    // print the first few from a zero seed, not normally done
    public void XtestListValues() {
        for (int i = 0; i< 200; i++) {
            System.out.println("0x"+Integer.toHexString(na.getNIDa()));
            na.nextAlias();
        }
    }
    
    // test takes a couple minutes, not normally done
    public void XtestAltPRNG() {
      // http://en.wikipedia.org/wiki/Linear_feedback_shift_register
      long lfsr = 1;
      long period = 0; 
      do 
      {
        /* taps: 32 31 29 1; characteristic polynomial: x^32 + x^31 + x^29 + x + 1 */
        // have to mask upper bits, as long is 64 bits and we don't have "unsigned" in java
        lfsr = ((lfsr >> 1) ^ ( (-(lfsr & 1)) & 0xd0000001 )) & 0xFFFFFFFFl; 
        ++period;
      } while(lfsr != 1);  
      
      Assert.assertEquals("full length sequence", (1l<<32)-1, period);  
    }
    
    NodeID node;
    NIDa na;

    public void setUp() {
        node = new NodeID(new byte[]{0,0,0,1,0,2}); 
        na = new NIDa(node);
    }

    // from here down is testing infrastructure
    
    public NIDaTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NIDaTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NIDaTest.class);
        return suite;
    }
}
