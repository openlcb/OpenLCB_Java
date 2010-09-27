package org.openlcb.can;

import org.openlcb.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2010
 * @version $Revision$
 */
public class AliasMapTest extends TestCase {
    
    public void testEmptyStart() {
        AliasMap map = new AliasMap();
        
        Assert.assertEquals("get Alias", -1, map.getAlias(new NodeID(new byte[]{0,1,2,3,4,5})));

        Assert.assertEquals("get NodeID", null, map.getNodeID(0));
    }
    
    public void testAfterFrame() {
        AliasMap map = new AliasMap();
        
        OpenLcbCanFrame f = new OpenLcbCanFrame(0x123);
        f.setInitializationComplete(0x123, new NodeID(new byte[]{0,1,2,3,4,5}));
        map.processFrame(f);
        Assert.assertEquals("check NodeID", new NodeID(new byte[]{0,1,2,3,4,5}), map.getNodeID(0x123));
    }
    
    public void testAfterInsert() {
        AliasMap map = new AliasMap();
        
        map.insert(0x123, new NodeID(new byte[]{0,1,2,3,4,5}));
        Assert.assertEquals("check NodeID", new NodeID(new byte[]{0,1,2,3,4,5}), map.getNodeID(0x123));
    }
    
    // from here down is testing infrastructure
    
    public AliasMapTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {AliasMapTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AliasMapTest.class);
        return suite;
    }
}
