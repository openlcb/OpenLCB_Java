package org.openlcb.implementations;

import org.openlcb.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class EventFilterGatewayTest extends GatewayTest {
    
    // create a filtering gateway for parent tests
    protected Gateway getGateway() {
        return new EventFilterGateway();
    }

    NodeID node1 = new NodeID(new byte[]{1,0,0,0,0,1});
    NodeID node2 = new NodeID(new byte[]{1,0,0,0,0,2});
    
    EventID eventA = new EventID(new byte[]{1,0,0,0,0,0,1,0});

    // first will be specific tests of filtering
    // followed by all the inherited tests of a regular Gateway

    public void testEventNotPassByDefaultEtoW() {
        buildGateway();
        Message m = new ProducerConsumerEventReportMessage(node1, eventA);

        cE.put(m, tE);
        
        checkMovedNeitherWay();
    }
    
    protected void checkMovedNeitherWay() {
        Assert.assertTrue(!resultE);
        Assert.assertTrue(!resultW);
        resultE = false;
        resultW = false;
    }

    public void testEventNotPassByDefaultWtoE() {
        buildGateway();
        Message m = new ProducerConsumerEventReportMessage(node1, eventA);

        cW.put(m, tE);
        
        checkMovedNeitherWay();
    }
    
    public void testReqEventPassesEtoW() {
        buildGateway();

        Message m1 = new ConsumerIdentifiedMessage(node2, eventA, EventState.Unknown);
        cW.put(m1, tW);
        checkMovedWestToEastOnly();
        
        Message m2 = new ProducerConsumerEventReportMessage(node1, eventA);
        cE.put(m2, tE);
        checkMovedEastToWestOnly();
    }
    
    public void testReqEventPassesWtoE() {
        buildGateway();

        Message m1 = new ConsumerIdentifiedMessage(node2, eventA, EventState.Unknown);
        cE.put(m1, tE);
        checkMovedEastToWestOnly();
        
        Message m2 = new ProducerConsumerEventReportMessage(node1, eventA);
        cW.put(m2, tW);
        checkMovedWestToEastOnly();
    }
    
    // from here down is testing infrastructure
    
    public EventFilterGatewayTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {EventFilterGatewayTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EventFilterGatewayTest.class);
        return suite;
    }
}
