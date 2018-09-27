package scenarios;

import org.openlcb.*;
import org.openlcb.implementations.*;

import org.junit.*;

/**
 * Simulate two buses East and West, 
 * each with three producers and three consumers.
 * <ul>
 * <li>Producer West 1 and East 1 send Event A to Consumer West 1 and East 1
 * <li>(same for nodes 2 and Event B, nodes 3 and Event C)
 * </ul>
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class TwoBuses {

    NodeID idWP1 = new NodeID(new byte[]{0,0,0,0,0,1});
    NodeID idWP2 = new NodeID(new byte[]{0,0,0,0,0,2});
    NodeID idWP3 = new NodeID(new byte[]{0,0,0,0,1,3});
    NodeID idWC1 = new NodeID(new byte[]{0,0,0,0,1,1});
    NodeID idWC2 = new NodeID(new byte[]{0,0,0,0,1,2});
    NodeID idWC3 = new NodeID(new byte[]{0,0,0,0,0,3});
    NodeID idEP1 = new NodeID(new byte[]{0,0,0,1,0,1});
    NodeID idEP2 = new NodeID(new byte[]{0,0,0,1,0,2});
    NodeID idEP3 = new NodeID(new byte[]{0,0,0,1,0,3});
    NodeID idEC1 = new NodeID(new byte[]{0,0,0,1,1,1});
    NodeID idEC2 = new NodeID(new byte[]{0,0,0,1,1,2});
    NodeID idEC3 = new NodeID(new byte[]{0,0,0,1,1,3});

    EventID eventA = new EventID(new byte[]{1,0,0,0,0,0,1,0});
    EventID eventB = new EventID(new byte[]{1,0,0,0,0,0,2,0});
    EventID eventC = new EventID(new byte[]{1,0,0,0,0,0,3,0});
    
    SingleProducerNode nodeWP1;
    SingleProducerNode nodeWP2;
    SingleProducerNode nodeWP3;
    SingleProducerNode nodeEP1;
    SingleProducerNode nodeEP2;
    SingleProducerNode nodeEP3;

    SingleConsumerNode nodeWC1;
    SingleConsumerNode nodeWC2;
    SingleConsumerNode nodeWC3;
    SingleConsumerNode nodeEC1;
    SingleConsumerNode nodeEC2;
    SingleConsumerNode nodeEC3;
        
    ScatterGather sgW;
    ScatterGather sgE;
    
    Gateway gate;
   
    @Before 
    public void setUp() {
        sgE = new ScatterGather();
        sgW = new ScatterGather();

        nodeWP1 = new SingleProducerNode(idWP1, sgW.getConnection(), eventA);
        sgW.register(nodeWP1);
        
        nodeEP1 = new SingleProducerNode(idEP1, sgE.getConnection(), eventA);
        sgE.register(nodeEP1);
        
        nodeWC1 = new SingleConsumerNode(idWC1, sgW.getConnection(), eventA);
        sgW.register(nodeWC1);
        
        nodeEC1 = new SingleConsumerNode(idEC1, sgE.getConnection(), eventA);
        sgE.register(nodeEC1);
        

        nodeWP2 = new SingleProducerNode(idWP2, sgW.getConnection(), eventB);
        sgW.register(nodeWP2);
        
        nodeEP2 = new SingleProducerNode(idEP2, sgE.getConnection(), eventB);
        sgE.register(nodeEP2);
        
        nodeWC2 = new SingleConsumerNode(idWC2, sgW.getConnection(), eventB);
        sgW.register(nodeWC2);
        
        nodeEC2 = new SingleConsumerNode(idEC2, sgE.getConnection(), eventB);
        sgE.register(nodeEC2);
        

        nodeWP3 = new SingleProducerNode(idWP3, sgW.getConnection(), eventC);
        sgW.register(nodeWP3);
        
        nodeEP3 = new SingleProducerNode(idEP3, sgE.getConnection(), eventC);
        sgE.register(nodeEP3);
        
        nodeWC3 = new SingleConsumerNode(idWC3, sgW.getConnection(), eventC);
        sgW.register(nodeWC3);
        
        nodeEC3 = new SingleConsumerNode(idEC3, sgE.getConnection(), eventC);
        sgE.register(nodeEC3);    
        
        // Link the two buses
        createGateway();
        
        sgE.register(gate.getEastConnection());
        gate.registerEast(sgE.getConnection());
        sgW.register(gate.getWestConnection());
        gate.registerWest(sgW.getConnection());
    }
    
    protected void createGateway() {
        gate = new Gateway();
    }

    @After
    public void tearDown() {
        sgE = null;
        sgW = null;
        nodeWP1 = null;
        nodeEP1 = null;
        nodeWC1 = null;
        nodeEC1 = null;
        nodeWP2 = null;
        nodeEP2 = null;
        nodeWC2 = null;
        nodeEC2 = null;
        nodeWP3 = null;
        nodeEP3 = null;
        nodeWC3 = null;
        nodeEC3 = null;
        gate = null;
    }
    
    @Test
    public void testSetup() {
        // just run the setup to make sure it works
    }
    
    @Test
    public void testInitAll() {
        initAll();
    }
    
    void initAll() {
        nodeWP1.initialize();
        nodeWP2.initialize();
        nodeWP3.initialize();
        nodeWC1.initialize();
        nodeWC2.initialize();
        nodeWC3.initialize();
        
        nodeEP1.initialize();
        nodeEP2.initialize();
        nodeEP3.initialize();
        nodeEC1.initialize();
        nodeEC2.initialize();
        nodeEC3.initialize();
    }
    
    @Test
    public void testMessagesInOrder() {
        initAll();
        
        nodeWP1.send();  
        Assert.assertTrue(nodeWC1.getReceived()); 
        Assert.assertTrue(nodeEC1.getReceived()); 

        checkAllReset();

        nodeEP1.send();  
        Assert.assertTrue(nodeWC1.getReceived()); 
        Assert.assertTrue(nodeEC1.getReceived()); 

        checkAllReset();
        
        nodeWP2.send();  
        Assert.assertTrue(nodeWC2.getReceived()); 
        Assert.assertTrue(nodeEC2.getReceived()); 

        checkAllReset();

        nodeEP2.send();  
        Assert.assertTrue(nodeWC2.getReceived()); 
        Assert.assertTrue(nodeEC2.getReceived()); 

        checkAllReset();
        
        nodeWP3.send();  
        Assert.assertTrue(nodeWC3.getReceived()); 
        Assert.assertTrue(nodeEC3.getReceived()); 

        checkAllReset();

        nodeEP3.send();  
        Assert.assertTrue(nodeWC3.getReceived()); 
        Assert.assertTrue(nodeEC3.getReceived()); 

        checkAllReset();
    }
    
    void checkAllReset() {
        Assert.assertTrue(!nodeEC1.getReceived()); 
        Assert.assertTrue(!nodeEC2.getReceived()); 
        Assert.assertTrue(!nodeEC3.getReceived()); 
        Assert.assertTrue(!nodeWC1.getReceived()); 
        Assert.assertTrue(!nodeWC2.getReceived()); 
        Assert.assertTrue(!nodeWC3.getReceived()); 
    }
}
