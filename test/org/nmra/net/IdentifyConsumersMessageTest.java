package org.nmra.net;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class IdentifyConsumersMessageTest extends TestCase {
    boolean result;
    
    public void testEqualsSame() {
        Message m1 = new IdentifyConsumersMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));

        Message m2 = new IdentifyConsumersMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
    
        Assert.assertTrue(m1.equals(m2));
    }

    public void testNotEqualsDifferentNode() {
        Message m1 = new IdentifyConsumersMessage(
                                            new NodeID(new byte[]{99,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));

        Message m2 = new IdentifyConsumersMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    public void testNotEqualsDifferentEvent() {
        Message m1 = new IdentifyConsumersMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{99,2,3,4,5,6,7,8}));

        Message m2 = new IdentifyConsumersMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleIdentifyConsumers(IdentifyConsumersMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new IdentifyConsumersMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            new EventID(new byte[]{1,2,3,4,5,6,7,8}));
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }
    
    // from here down is testing infrastructure
    
    public IdentifyConsumersMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {IdentifyConsumersMessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(IdentifyConsumersMessageTest.class);
        return suite;
    }
}
