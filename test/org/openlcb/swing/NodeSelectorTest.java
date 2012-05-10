package org.openlcb.swing;

import org.openlcb.*;
import org.openlcb.implementations.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;
/**
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision: 34 $
 */
public class NodeSelectorTest extends TestCase {

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
    
    public void tearDown() {}
            
    public void testCtor() {
        // test is really in setUp()
    }
        
    public void testNodesArrivingLater() {
        frame.setTitle("NodeSelector: expect 6");
        frame.setLocation(0,100);
        store.addNode(id4);
        store.addNode(id5);
        store.addNode(id6);
    }
   
    // from here down is testing infrastructure
    
    public NodeSelectorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NodeSelectorTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NodeSelectorTest.class);
        return suite;
    }
}
