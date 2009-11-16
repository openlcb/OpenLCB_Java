package scenarios;

import org.openlcb.*;
import org.openlcb.implementations.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Simulate nine nodes interacting on a single gather/scatter
 * "link".
 * <ul>
 * <li>Nodes 1,2,3 send Event A to 8,9
 * <li>Node 4 sends Event B to node 7
 * <li>Node 5 sends Event C to node 6
 * </ul>
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class NineOnALink extends TestCase {

    NodeID id1 = new NodeID(new byte[]{0,0,0,0,0,1});
    NodeID id2 = new NodeID(new byte[]{0,0,0,0,0,2});
    NodeID id3 = new NodeID(new byte[]{0,0,0,0,0,3});
    NodeID id4 = new NodeID(new byte[]{0,0,0,0,0,4});
    NodeID id5 = new NodeID(new byte[]{0,0,0,0,0,5});
    NodeID id6 = new NodeID(new byte[]{0,0,0,0,0,6});
    NodeID id7 = new NodeID(new byte[]{0,0,0,0,0,7});
    NodeID id8 = new NodeID(new byte[]{0,0,0,0,0,8});
    NodeID id9 = new NodeID(new byte[]{0,0,0,0,0,9});

    EventID eventA = new EventID(new byte[]{1,0,0,0,0,0,1,0});
    EventID eventB = new EventID(new byte[]{1,0,0,0,0,0,2,0});
    EventID eventC = new EventID(new byte[]{1,0,0,0,0,0,3,0});
    
    SingleProducerNode node1;
    SingleProducerNode node2;
    SingleProducerNode node3;
    SingleProducerNode node4;
    SingleProducerNode node5;
    SingleConsumerNode node6;
    SingleConsumerNode node7;
    SingleConsumerNode node8;
    SingleConsumerNode node9;
    
    ScatterGather sg;
    
    public void setUp() {
        sg = new ScatterGather();

        node1 = new SingleProducerNode(id1, sg.getConnection(), eventA);
        sg.register(node1);
        
        node2 = new SingleProducerNode(id2, sg.getConnection(), eventA);
        sg.register(node2);
        
        node3 = new SingleProducerNode(id3, sg.getConnection(), eventA);
        sg.register(node3);
        
        node4 = new SingleProducerNode(id4, sg.getConnection(), eventB);
        sg.register(node4);
        
        node5 = new SingleProducerNode(id5, sg.getConnection(), eventC);
        sg.register(node5);
        
        node6 = new SingleConsumerNode(id6, sg.getConnection(), eventC);
        sg.register(node6);
        
        node7 = new SingleConsumerNode(id7, sg.getConnection(), eventB);
        sg.register(node7);
        
        node8 = new SingleConsumerNode(id8, sg.getConnection(), eventA);
        sg.register(node8);
        
        node9 = new SingleConsumerNode(id9, sg.getConnection(), eventA);
        sg.register(node9);
        
    }
    
    public void tearDown() {}
    
    public void testSetup() {
        // just run the setup to make sure it works
    }
    
    public void testInitAll() {
        initAll();
    }
    
    void initAll() {
        node1.initialize();
        node2.initialize();
        node3.initialize();
        node4.initialize();
        node5.initialize();
        node6.initialize();
        node7.initialize();
        node8.initialize();
        node9.initialize();
    }
    
    public void testMessagesInOrder() {
        initAll();
        
        node1.send();  
        Assert.assertTrue(node8.getReceived()); 
        Assert.assertTrue(node9.getReceived()); 

        checkAllReset();

        node4.send();  
        Assert.assertTrue(node7.getReceived()); 

        checkAllReset();

        node5.send(); 
        Assert.assertTrue(node6.getReceived()); 

        checkAllReset();
    }
    
    void checkAllReset() {
        Assert.assertTrue(!node6.getReceived()); 
        Assert.assertTrue(!node7.getReceived()); 
        Assert.assertTrue(!node8.getReceived()); 
        Assert.assertTrue(!node9.getReceived()); 
    }
    
    public void testMessages2ndOrder() {
        initAll();
        
        node1.send();  
        node4.send();  
        node5.send(); 
        
        Assert.assertTrue(node6.getReceived()); 
        Assert.assertTrue(node7.getReceived()); 
        Assert.assertTrue(node8.getReceived()); 
        Assert.assertTrue(node9.getReceived()); 

        checkAllReset();
    }
        
    public void testMessagesResent() {
        initAll();
        
        node1.send();  
        node4.send();  
        node5.send(); 
        
        Assert.assertTrue(node6.getReceived()); 
        Assert.assertTrue(node7.getReceived()); 
        Assert.assertTrue(node8.getReceived()); 
        Assert.assertTrue(node9.getReceived()); 

        checkAllReset();

        node2.send();  
        Assert.assertTrue(node8.getReceived()); 
        Assert.assertTrue(node9.getReceived()); 

        checkAllReset();

        node3.send();  
        Assert.assertTrue(node8.getReceived()); 
        Assert.assertTrue(node9.getReceived()); 

        checkAllReset();
    }
    
    
    // from here down is testing infrastructure
    
    public NineOnALink(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NineOnALink.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NineOnALink.class);
        return suite;
    }
}
