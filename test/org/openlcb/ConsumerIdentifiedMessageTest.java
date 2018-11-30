package org.openlcb;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class ConsumerIdentifiedMessageTest  {
    boolean result;
    
    EventID eventID1 = new EventID(new byte[]{1,0,0,0,0,0,1,0});
    EventID eventID2 = new EventID(new byte[]{1,0,0,0,0,0,2,0});

    NodeID nodeID1 = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID nodeID2 = new NodeID(new byte[]{0,0,0,0,0,0});

    @Test
    public void testEqualsSame() {
        Message m1 = new ConsumerIdentifiedMessage(
                               nodeID1, eventID1, EventState.Valid);
        Message m2 = new ConsumerIdentifiedMessage(
                               nodeID1, eventID1, EventState.Valid);
    
        Assert.assertTrue(m1.equals(m2));
    }

    @Test
    public void testNotEqualsDifferentNode() {
        Message m1 = new ConsumerIdentifiedMessage(
                                nodeID1, eventID1, EventState.Valid);
        Message m2 = new ConsumerIdentifiedMessage(
                                nodeID2, eventID1, EventState.Valid);
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test
    public void testNotEqualsDifferentEvent() {
        Message m1 = new ConsumerIdentifiedMessage(
                                nodeID1, eventID1, EventState.Valid);
        Message m2 = new ConsumerIdentifiedMessage(
                                nodeID1, eventID2, EventState.Valid);
    
        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test
    public void testNotEqualsDifferentState() {
        Message m1 = new ConsumerIdentifiedMessage(
                nodeID1, eventID1, EventState.Valid);
        Message m2 = new ConsumerIdentifiedMessage(
                nodeID1, eventID2, EventState.Unknown);

        Assert.assertTrue( ! m1.equals(m2));
    }

    @Test
    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleConsumerIdentified(ConsumerIdentifiedMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new ConsumerIdentifiedMessage(
                                            new NodeID(new byte[]{1,2,3,4,5,6}),
                                            eventID1, EventState.Unknown);
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }
}
