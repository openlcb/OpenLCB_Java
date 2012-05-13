package org.openlcb.swing.memconfig;

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
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision: 34 $
 */
public class MemConfigDescriptionPaneTest extends TestCase {

    NodeID nidHere = new NodeID(new byte[]{0,0,0,0,0,1});
    NodeID nidThere = new NodeID(new byte[]{0,0,0,0,0,2});
    
    JFrame frame;
    Connection connection = new AbstractConnection() {
        public void put(Message msg, Connection sender) {}
    };
    
    MimicNodeStore store;
    
    public void setUp() throws Exception {
        store = new MimicNodeStore(connection, nidHere);
        store.addNode(nidThere);
        
        // Test is really popping a window before doing all else
        frame = new JFrame();
        frame.setTitle("MemConfigDescriptionPane Test");

        frame.pack();
        frame.setMinimumSize(new java.awt.Dimension(200,200));
        frame.setVisible(true);
        
        
    }
    
    public void tearDown() {
        //frame.setVisible(false);
    }
            
    public void testSetup() {
    }
           
    // from here down is testing infrastructure
    
    public MemConfigDescriptionPaneTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MemConfigDescriptionPaneTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MemConfigDescriptionPaneTest.class);
        return suite;
    }
}
