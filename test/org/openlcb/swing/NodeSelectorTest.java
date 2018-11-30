package org.openlcb.swing;

import org.openlcb.*;
import org.openlcb.implementations.*;

import org.junit.*;

import javax.swing.*;
/**
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision: 34 $
 */
public class NodeSelectorTest  {

    NodeID id1 = new NodeID(new byte[]{0,0,0,0,0,1});
    NodeID id2 = new NodeID(new byte[]{0,0,0,0,0,2});
    NodeID id3 = new NodeID(new byte[]{0,0,0,0,0,3});
    NodeID id4 = new NodeID(new byte[]{0,0,0,0,0,4});
    NodeID id5 = new NodeID(new byte[]{0,0,0,0,0,5});
    NodeID id6 = new NodeID(new byte[]{0,0,0,0,0,6});
    NodeID id7 = new NodeID(new byte[]{0,0,0,0,0,7});
    NodeID id8 = new NodeID(new byte[]{0,0,0,0,0,8});
    NodeID id9 = new NodeID(new byte[]{0,0,0,0,0,9});

    NodeID thisNode = new NodeID(new byte[]{1,2,3,4,5,6});
    MimicNodeStore store;
    
    JFrame frame;

    @Before    
    public void setUp() throws Exception {
        store = new MimicNodeStore(null, thisNode);
        store.addNode(id1);
        store.addNode(id2);
        store.addNode(id3);
        
        // Test is really popping a window before doing all else
        frame = new JFrame();
        frame.setTitle("NodeSelector: expect 3");
        NodeSelector m = new NodeSelector(store);
        frame.add( m );
        frame.pack();
        frame.setVisible(true);
        
    }
   
    @After 
    public void tearDown() {
       frame.setVisible(false);
       frame.dispose();
       frame = null;
       store.dispose();
       store = null;
    }

    @Test    
    public void testCtor() {
        // test is really in setUp()
	Assert.assertNotNull("store exists",store);
    }
        
    @Test    
    public void testNodesArrivingLater() {
        frame.setTitle("NodeSelector: expect 6");
        frame.setLocation(0,100);
        store.addNode(id4);
        store.addNode(id5);
        store.addNode(id6);
    }
   
}
