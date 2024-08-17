package org.openlcb.swing;

import org.openlcb.*;

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
    NodeID idf = new NodeID(new byte[]{(byte)0xFF,0,0,0,0,6});
    NodeID idfe = new NodeID(new byte[]{(byte)0xFE,0,0,0,0,6});

    NodeID thisNode = new NodeID(new byte[]{1,2,3,4,5,6});
    MimicNodeStore store;
    Connection connection = new AbstractConnection() {
        @Override
        public void put(Message msg, Connection sender) {
        }
    };
    
    JFrame frame;
    private NodeSelector nodeSelector;

    @Before    
    public void setUp() throws Exception {
        store = new MimicNodeStore(connection, thisNode);
        store.addNode(id7);
        store.addNode(id2);
        store.addNode(id1);

        // Test is really popping a window before doing all else
        frame = new JFrame();
        frame.setTitle("NodeSelector: expect 3");
        nodeSelector = new NodeSelector(store);
        frame.add(nodeSelector);
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

    String getAllItems() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodeSelector.getItemCount(); ++i) {
            sb.append(nodeSelector.getItemAt(i).toString());
            sb.append(';');
        }
        return sb.toString();
    }

    @Test    
    public void testCtor() {
        // test is really in setUp()
        Assert.assertNotNull("store exists", store);
        Assert.assertEquals("00.00.00.00.00.01;00.00.00.00.00.02;00.00.00.00.00.07;",
                getAllItems());
    }

    @Test
    public void testIdentifyUpdates() {
        Assert.assertNotNull("store exists", store);
        Assert.assertEquals("00.00.00.00.00.01;00.00.00.00.00.02;00.00.00.00.00.07;",
                getAllItems());
        store.put(new SimpleNodeIdentInfoReplyMessage(
                id2, thisNode,
                new byte[]{1,'a','b',0,'1',0,'2',0,'A',0,1,'u','s',0,'3','4',0}), connection);
        Assert.assertEquals("00.00.00.00.00.01;00.00.00.00.00.02 - us - 34;00.00.00.00.00.07;",
                getAllItems());
    }

    @Test    
    public void testNodesArrivingLaterKeepSorted() {
        frame.setTitle("NodeSelector: expect 6");
        frame.setLocation(0,100);
        store.addNode(id6);
        Assert.assertEquals("00.00.00.00.00.01;00.00.00.00.00.02;" +
                        "00.00.00.00.00.06;00.00.00.00.00.07;",
                getAllItems());

        store.addNode(idf);
        Assert.assertEquals("00.00.00.00.00.01;00.00.00.00.00.02;" +
                        "00.00.00.00.00.06;00.00.00.00.00.07;FF.00.00.00.00.06;",
                getAllItems());

        store.addNode(idfe);
        Assert.assertEquals("00.00.00.00.00.01;00.00.00.00.00.02;" +
                        "00.00.00.00.00.06;00.00.00.00.00.07;FE.00.00.00.00.06;FF.00.00.00.00.06;",
                getAllItems());
    }
   
}
