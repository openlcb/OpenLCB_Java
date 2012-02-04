package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision: 155 $
 */
public class DatagramMessageTest extends TestCase {
    boolean result;
    
    NodeID nodeID1 = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID nodeID2 = new NodeID(new byte[]{0,0,0,0,0,0});
    NodeID nodeID3 = new NodeID(new byte[]{0,0,0,0,0,3});

    int data1[] = new int[]{1,1};
    int data2[] = new int[]{1,2};
    int data3[] = new int[]{1};
    
    public void testEqualsSame() {
        Message m1 = new DatagramMessage(nodeID1, nodeID2, data1);
        Message m2 = new DatagramMessage(nodeID1, nodeID2, data1);
    
        Assert.assertTrue(m1.equals(m2));
    }

    public void testNotEqualsDifferentNodeFrom() {
        Message m1 = new DatagramMessage(nodeID1, nodeID2, data1);
        Message m2 = new DatagramMessage(nodeID3, nodeID2, data1);
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    public void testNotEqualsDifferentNodeTo() {
        Message m1 = new DatagramMessage(nodeID1, nodeID2, data1);
        Message m2 = new DatagramMessage(nodeID1, nodeID3, data1);
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    public void testNotEqualsDifferentDataLength1() {
        Message m1 = new DatagramMessage(nodeID1, nodeID2, data1);
        Message m2 = new DatagramMessage(nodeID1, nodeID2, data3);
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    public void testNotEqualsDifferentDataLength2() {
        Message m1 = new DatagramMessage(nodeID1, nodeID2, data3);
        Message m2 = new DatagramMessage(nodeID1, nodeID2, data1);
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    public void testNotEqualsDifferentDataContent() {
        Message m1 = new DatagramMessage(nodeID1, nodeID2, data1);
        Message m2 = new DatagramMessage(nodeID1, nodeID2, data2);
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    public void testMessageImmutable() {
        int data[] = new int[]{1,2,3};
        Message m1 = new DatagramMessage(nodeID1, nodeID2, data);
        data[1]=12;
        Message m2 = new DatagramMessage(nodeID1, nodeID2, data);
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleDatagram(DatagramMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new DatagramMessage(nodeID1, nodeID2, data1);
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }
    
    // from here down is testing infrastructure
    
    public DatagramMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DatagramMessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DatagramMessageTest.class);
        return suite;
    }
}
