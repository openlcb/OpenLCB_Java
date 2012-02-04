package org.openlcb.swing.networktree;

import org.openlcb.*;
import org.openlcb.implementations.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;
/**
 * Simulate nine nodes interacting on a single gather/scatter
 * "link", and feed them to monitor.
 * <ul>
 * <li>Nodes 1,2,3 send Event A to 8,9
 * <li>Node 4 sends Event B to node 7
 * <li>Node 5 sends Event C to node 6
 * </ul>
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision: 34 $
 */
public class TreePaneTest extends TestCase {

    NodeID nid1 = new NodeID(new byte[]{0,0,0,0,0,1});
    NodeID nid2 = new NodeID(new byte[]{0,0,0,0,0,2});
    NodeID nid3 = new NodeID(new byte[]{0,0,0,0,0,3});
    NodeID nid4 = new NodeID(new byte[]{0,0,0,0,0,4});
    NodeID nid5 = new NodeID(new byte[]{0,0,0,0,0,5});
    NodeID nid6 = new NodeID(new byte[]{0,0,0,0,0,6});
    NodeID nid7 = new NodeID(new byte[]{0,0,0,0,0,7});
    NodeID nid8 = new NodeID(new byte[]{0,0,0,0,0,8});
    NodeID nid9 = new NodeID(new byte[]{0,0,0,0,0,9});

    EventID eventA = new EventID(new byte[]{1,0,0,0,0,0,1,0});
    EventID eventB = new EventID(new byte[]{1,0,0,0,0,0,2,0});
    EventID eventC = new EventID(new byte[]{1,0,0,0,0,0,3,0});
    
    JFrame frame;
    TreePane pane;
    MimicNodeStore store;
    
    public void setUp() throws Exception {
        store = new MimicNodeStore();
        Message msg = new ProducerIdentifiedMessage(nid1, eventA);
        store.put(msg, null);
        
        // Test is really popping a window before doing all else
        frame = new JFrame();
        frame.setTitle("TreePane Test");
        TreePane pane = new TreePane();
        frame.add( pane );
        pane.initComponents(store);
        frame.pack();
        frame.setMinimumSize(new java.awt.Dimension(200,200));
        frame.setVisible(true);
        
        
    }
    
    public void tearDown() {
        //frame.setVisible(false);
    }
            
    public void testPriorMessage() {
        frame.setTitle("Prior Message");
    }

    public void testAfterMessage() {
        frame.setTitle("After Message");
        Message msg = new ProducerIdentifiedMessage(nid2, eventA);
        store.put(msg, null);
    }
        
    public void testWithProtocolID() {
        frame.setTitle("2nd has protocol id");
        Message msg;
        msg = new ProducerIdentifiedMessage(nid2, eventA);
        store.put(msg, null);
        msg = new ProtocolIdentificationReplyMessage(nid2, 0x03);
        store.put(msg, null);
    }
        
   
    // from here down is testing infrastructure
    
    public TreePaneTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {TreePaneTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TreePaneTest.class);
        return suite;
    }
}
