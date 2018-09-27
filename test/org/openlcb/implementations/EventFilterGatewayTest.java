package org.openlcb.implementations;

import org.openlcb.*;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class EventFilterGatewayTest extends GatewayTest {
    
    // create a filtering gateway for parent tests
    @Override
    protected Gateway getGateway() {
        return new EventFilterGateway();
    }

    NodeID node1 = new NodeID(new byte[]{1,0,0,0,0,1});
    NodeID node2 = new NodeID(new byte[]{1,0,0,0,0,2});
    
    EventID eventA = new EventID(new byte[]{1,0,0,0,0,0,1,0});

    // first will be specific tests of filtering
    // followed by all the inherited tests of a regular Gateway

    @Test
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

    @Test
    public void testEventNotPassByDefaultWtoE() {
        buildGateway();
        Message m = new ProducerConsumerEventReportMessage(node1, eventA);

        cW.put(m, tE);
        
        checkMovedNeitherWay();
    }
    
    @Test
    public void testReqEventPassesEtoW() {
        buildGateway();

        Message m1 = new ConsumerIdentifiedMessage(node2, eventA, EventState.Unknown);
        cW.put(m1, tW);
        checkMovedWestToEastOnly();
        
        Message m2 = new ProducerConsumerEventReportMessage(node1, eventA);
        cE.put(m2, tE);
        checkMovedEastToWestOnly();
    }
    
    @Test
    public void testReqEventPassesWtoE() {
        buildGateway();

        Message m1 = new ConsumerIdentifiedMessage(node2, eventA, EventState.Unknown);
        cE.put(m1, tE);
        checkMovedEastToWestOnly();
        
        Message m2 = new ProducerConsumerEventReportMessage(node1, eventA);
        cW.put(m2, tW);
        checkMovedWestToEastOnly();
    }
    
}
