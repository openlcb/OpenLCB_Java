package org.nmra.net;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class ProducerIdentifiedMessageTest extends TestCase {
    boolean result;
    
    EventID eventID1 = new EventID(new byte[]{1,0,0,0,0,0,1,0});
    EventID eventID2 = new EventID(new byte[]{1,0,0,0,0,0,2,0});

    NodeID nodeID1 = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID nodeID2 = new NodeID(new byte[]{0,0,0,0,0,0});

    public void testEqualsSame() {
        Message m1 = new ProducerIdentifiedMessage(
                               nodeID1, eventID1 );
        Message m2 = new ProducerIdentifiedMessage(
                               nodeID1, eventID1 );
    
        Assert.assertTrue(m1.equals(m2));
    }

    public void testNotEqualsDifferentNode() {
        Message m1 = new ProducerIdentifiedMessage(
                                nodeID1, eventID1 );
        Message m2 = new ProducerIdentifiedMessage(
                                nodeID2, eventID1 );
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    public void testNotEqualsDifferentEvent() {
        Message m1 = new ProducerIdentifiedMessage(
                                nodeID1, eventID1 );
        Message m2 = new ProducerIdentifiedMessage(
                                nodeID1, eventID2 );
    
        Assert.assertTrue( ! m1.equals(m2));
    }


    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleProducerIdentified(ProducerIdentifiedMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new ProducerIdentifiedMessage(nodeID1, eventID1);
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }
    
    // from here down is testing infrastructure
    
    public ProducerIdentifiedMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ProducerIdentifiedMessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ProducerIdentifiedMessageTest.class);
        return suite;
    }
}
