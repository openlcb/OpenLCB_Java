package org.openlcb.swing.memconfig;

import org.openlcb.*;
import org.openlcb.implementations.*;

import org.junit.*;

import javax.swing.*;

import static org.mockito.Mockito.mock;

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
public class MemConfigReadWritePaneTest  {

    NodeID nidHere = new NodeID(new byte[]{0,0,0,0,0,1});
    NodeID nidThere = new NodeID(new byte[]{0,0,0,0,0,2});
    
    JFrame frame;
    Connection connection = new AbstractConnection() {
        public void put(Message msg, Connection sender) {}
    };
    
    MimicNodeStore store;
    MemoryConfigurationService service;
    DatagramService dgs;

    MemConfigReadWritePane pane;

    @Before
    public void setUp() throws Exception {
        store = new MimicNodeStore(connection, nidHere);
        store.addNode(nidThere);
        dgs = new DatagramService(null, null);
        
        service = mock(MemoryConfigurationService.class);

        // Test is really popping a window before doing all else
        frame = new JFrame();
        frame.setTitle("MemConfigReadWritePane Test");

        pane = new MemConfigReadWritePane(nidThere, store, service);
        pane.initComponents();
        frame.add(pane);
        
        frame.pack();
        frame.pack();
        frame.setMinimumSize(new java.awt.Dimension(200,200));
        frame.setVisible(true);
        
        
    }
    
    @After
    public void tearDown() {
        frame.setVisible(false);
	frame.dispose();
        store.dispose();
        service.dispose();
	frame = null;
	store = null;
	service = null;
    }
           
    @Test 
    public void testSetup() {
	Assert.assertNotNull("frame created",frame);
    }
}
