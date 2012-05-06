package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class IdentifyEventsMessageTest extends TestCase {
    boolean result;
    
    public void testEqualsSame() {
        Message m1 = new IdentifyEventsMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
        Message m2 = new IdentifyEventsMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
    
        Assert.assertTrue(m1.equals(m2));
    }

    public void testEqualsSelf() {
        Message m1 = new IdentifyEventsMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
    
        Assert.assertTrue(m1.equals(m1));
    }

    public void testNotEqualsDifferentSrc() {
        Message m1 = new IdentifyEventsMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
        Message m2 = new IdentifyEventsMessage(
                                            new NodeID(new byte[]{1,3,3,4,5,6}), 
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
    
        Assert.assertTrue( ! m1.equals(m2));
    }
    public void testNotEqualsDifferentDest() {
        Message m1 = new IdentifyEventsMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
        Message m2 = new IdentifyEventsMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}), 
                                            new NodeID(new byte[]{7,10,9,10,11,12}) );
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleIdentifyEvents(IdentifyEventsMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new IdentifyEventsMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new NodeID(new byte[]{7,8,9,10,11,12}) );
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }
    
    // from here down is testing infrastructure
    
    public IdentifyEventsMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {IdentifyEventsMessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(IdentifyEventsMessageTest.class);
        return suite;
    }
}
