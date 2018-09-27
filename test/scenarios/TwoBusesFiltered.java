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
 * The connection between them is an event-filtering gateway, so this
 * is a check of the filtering algorithm
 *
 * @author  Bob Jacobsen   Copyright 2009
 */
public class TwoBusesFiltered extends TwoBuses {

    // Use a filtering gateway
    @Override 
    protected void createGateway() {
        gate = new org.openlcb.implementations.EventFilterGateway();
    }
    
    // first will be specific tests of filtering
    // followed by all the inherited tests of TwoBusTest (which doesn't test filtering)
   
    @Test 
    public void testNoExtraMessages() {

        // similar to testMessagesInOrder() in parent,
        // except "west" consumer nodes not initialized, 
        // so don't get messages from East
        
        nodeWP1.initialize();
        nodeWP2.initialize();
        nodeWP3.initialize();

        // three west comsumers not initialized
                
        nodeEP1.initialize();
        nodeEP2.initialize();
        nodeEP3.initialize();
        nodeEC1.initialize();
        nodeEC2.initialize();
        nodeEC3.initialize();
        
        nodeWP1.send();  
        Assert.assertTrue(nodeWC1.getReceived()); 
        Assert.assertTrue(nodeEC1.getReceived()); 

        checkAllReset();

        nodeEP1.send();  
        Assert.assertTrue(!nodeWC1.getReceived()); 
        Assert.assertTrue(nodeEC1.getReceived()); 

        checkAllReset();
        
        nodeWP2.send();  
        Assert.assertTrue(nodeWC2.getReceived()); 
        Assert.assertTrue(nodeEC2.getReceived()); 

        checkAllReset();

        nodeEP2.send();  
        Assert.assertTrue(!nodeWC2.getReceived()); 
        Assert.assertTrue(nodeEC2.getReceived()); 

        checkAllReset();
        
        nodeWP3.send();  
        Assert.assertTrue(nodeWC3.getReceived()); 
        Assert.assertTrue(nodeEC3.getReceived()); 

        checkAllReset();

        nodeEP3.send();  
        Assert.assertTrue(!nodeWC3.getReceived()); 
        Assert.assertTrue(nodeEC3.getReceived()); 

        checkAllReset();

    }
}
