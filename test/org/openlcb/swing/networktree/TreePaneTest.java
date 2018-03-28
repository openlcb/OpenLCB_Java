package org.openlcb.swing.networktree;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openlcb.AbstractConnection;
import org.openlcb.Connection;
import org.openlcb.EventID;
import org.openlcb.EventState;
import org.openlcb.Message;
import org.openlcb.MimicNodeStore;
import org.openlcb.NodeID;
import org.openlcb.ProducerIdentifiedMessage;
import org.openlcb.ProtocolIdentificationReplyMessage;
import org.openlcb.SimpleNodeIdentInfoReplyMessage;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

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
    
    Message pipmsg = new ProtocolIdentificationReplyMessage(nid2, nid2, 0xF01800000000L);
    
    JFrame frame;
    TreePane pane;
    Connection connection = new AbstractConnection() {
        public void put(Message msg, Connection sender) {}
    };
    
    MimicNodeStore store;
    
    public void setUp() throws Exception {
        store = new MimicNodeStore(connection, nid1);
        Message msg = new ProducerIdentifiedMessage(nid1, eventA, EventState.Unknown);
        store.put(msg, null);
        
        // Test is really popping a window before doing all else
        frame = new JFrame();
        frame.setTitle("TreePane Test");
        pane = new TreePane();
        frame.add( pane );
        pane.initComponents(store, null, null, 
                new NodeTreeRep.SelectionKeyLoader() {
                    public NodeTreeRep.SelectionKey cdiKey(String name, NodeID node) {
                        return new NodeTreeRep.SelectionKey(name, node) {
                            public void select(DefaultMutableTreeNode rep) {
                                System.out.println("Making special fuss over: "+rep+" for "+name+" on "+node);
                            }
                        };
                    }
                });
        frame.pack();
        frame.setMinimumSize(new java.awt.Dimension(200,200));
        frame.setVisible(true);
    }
    
    public void tearDown() {
        frame.setVisible(false);
        pane.release();
        store.dispose();
        store = null;
        pane = null;
        frame = null;
    }
            
    public void testPriorMessage() {
        frame.setTitle("Prior Message");
    }

    public void testAfterMessage() {
        frame.setTitle("After Message");
        Message msg = new ProducerIdentifiedMessage(nid2, eventA, EventState.Unknown);
        store.put(msg, null);
    }
        
    public void testWithProtocolID() {
        frame.setTitle("2nd has protocol id");
        Message msg;
        msg = new ProducerIdentifiedMessage(nid2, eventA, EventState.Unknown);
        store.put(msg, null);
        store.put(pipmsg, null);
    }
        
    public void testWith1stSNII() {
        frame.setTitle("3rd has PIP && 1st SNII");
        Message msg;
        msg = new ProducerIdentifiedMessage(nid2, eventA, EventState.Unknown);
        store.put(msg, null);
        store.put(pipmsg, null);

        msg = new SimpleNodeIdentInfoReplyMessage(nid2, nid2,
                new byte[]{0x01, 0x31, 0x32, 0x33, 0x41, 0x42, (byte) 0xC2, (byte) 0xA2, 0x44,
                        0x00, 0, 0, 0, 1, 'h', 'e', 'l', 'l', 'o', 0, 'd', 'e', 's', 'c', 0}
                );
        store.put(msg, null);
        assertEquals("00.00.00.00.00.02 - hello - desc", pane.nodes.getChildAt(1).toString());
    }

    private void addNodeWithSnii(NodeID node, String manufacturer, String model, String userName, String userDesc) {
        List<Byte> dt = new ArrayList<>();
        dt.add((byte)1);
        for (Byte b : manufacturer.getBytes()) {
            dt.add(b);
        }
        dt.add((byte)0);
        for (Byte b : model.getBytes()) {
            dt.add(b);
        }
        dt.add((byte)0);
        dt.add((byte)0);
        dt.add((byte)0);
        dt.add((byte)1);
        for (Byte b : userName.getBytes()) {
            dt.add(b);
        }
        dt.add((byte)0);
        for (Byte b : userDesc.getBytes()) {
            dt.add(b);
        }
        dt.add((byte)0);

        store.put(new ProducerIdentifiedMessage(node, eventA, EventState.Unknown), null);
        store.put(new ProtocolIdentificationReplyMessage(node, nid1, 0xF01800000000L), null);

        byte[] ba = new byte[dt.size()];
        for (int i = 0; i < dt.size(); ++i) ba[i] = dt.get(i);
        Message msg = new SimpleNodeIdentInfoReplyMessage(node, nid1, ba);
        store.put(msg, null);
    }

    public void testNodeOrder() {
        frame.setTitle("test node order");
        store.put(new ProtocolIdentificationReplyMessage(nid2, nid1, 0xF01800000000L), null);
        assertEquals(2, pane.nodes.getChildCount());
        store.put(new ProtocolIdentificationReplyMessage(nid6, nid1, 0xF01800000000L), null);
        assertEquals(3, pane.nodes.getChildCount());
        store.put(new ProtocolIdentificationReplyMessage(nid3, nid1, 0xF01800000000L), null);
        assertEquals(4, pane.nodes.getChildCount());
        store.put(new ProtocolIdentificationReplyMessage(nid4, nid1, 0xF01800000000L), null);
        assertEquals(5, pane.nodes.getChildCount());
        assertEquals("00.00.00.00.00.01", pane.nodes.getChildAt(0).toString().substring(0, 17));
        assertEquals("00.00.00.00.00.02", pane.nodes.getChildAt(1).toString().substring(0, 17));
        assertEquals("00.00.00.00.00.03", pane.nodes.getChildAt(2).toString().substring(0, 17));
        assertEquals("00.00.00.00.00.04", pane.nodes.getChildAt(3).toString().substring(0, 17));
        assertEquals("00.00.00.00.00.06", pane.nodes.getChildAt(4).toString().substring(0, 17));
    }

    public void testAddNodeWithSnii() {
        addNodeWithSnii(nid5, "manuf42", "model55", "username92", "userdesc93");
        MimicNodeStore.NodeMemo memo = store.findNode(nid5);
        assertNotNull(memo);
        assertEquals("manuf42", memo.getSimpleNodeIdent().getMfgName());
        assertEquals("model55", memo.getSimpleNodeIdent().getModelName());
        assertEquals("username92", memo.getSimpleNodeIdent().getUserName());
        assertEquals("userdesc93", memo.getSimpleNodeIdent().getUserDesc());
    }

    public void testSortOrder() {
        addNodeWithSnii(nid2, "3", "2", "2", "4");
        addNodeWithSnii(nid5, "1", "4", "3", "2");
        MimicNodeStore.NodeMemo memo5 = store.findNode(nid5);
        MimicNodeStore.NodeMemo memo2 = store.findNode(nid2);
        assertTrue(new TreePane.Sorter(TreePane.SortOrder.BY_NODE_ID).compare(memo2, memo5) < 0);
        assertTrue(new TreePane.Sorter(TreePane.SortOrder.BY_NAME).compare(memo2, memo5) < 0);
        assertTrue(new TreePane.Sorter(TreePane.SortOrder.BY_DESCRIPTION).compare(memo2, memo5) > 0);
        assertTrue(new TreePane.Sorter(TreePane.SortOrder.BY_MODEL).compare(memo2, memo5) > 0);
    }

    public void testSortOrder2() {
        addNodeWithSnii(nid4, "xxx", "ppp", "bbb", "ccc");
        addNodeWithSnii(nid5, "xxx", "pqq", "bbb", "ccc");
        MimicNodeStore.NodeMemo memo5 = store.findNode(nid5);
        MimicNodeStore.NodeMemo memo4 = store.findNode(nid4);
        assertTrue(new TreePane.Sorter(TreePane.SortOrder.BY_NODE_ID).compare(memo4, memo5) < 0);
        assertTrue(new TreePane.Sorter(TreePane.SortOrder.BY_NAME).compare(memo4, memo5) < 0);
        assertTrue(new TreePane.Sorter(TreePane.SortOrder.BY_DESCRIPTION).compare(memo4, memo5) < 0);
        assertTrue(new TreePane.Sorter(TreePane.SortOrder.BY_MODEL).compare(memo4, memo5) < 0);
    }

    public void testSortOrder3() {
        addNodeWithSnii(nid4, "xxx", "qqq", "bbb", "ccd");
        addNodeWithSnii(nid5, "xxx", "pqq", "bbb", "ccc");
        MimicNodeStore.NodeMemo memo5 = store.findNode(nid5);
        MimicNodeStore.NodeMemo memo4 = store.findNode(nid4);
        assertTrue(new TreePane.Sorter(TreePane.SortOrder.BY_NODE_ID).compare(memo4, memo5) < 0);
        assertTrue(new TreePane.Sorter(TreePane.SortOrder.BY_NAME).compare(memo4, memo5) > 0);
        assertTrue(new TreePane.Sorter(TreePane.SortOrder.BY_DESCRIPTION).compare(memo4, memo5) > 0);
        assertTrue(new TreePane.Sorter(TreePane.SortOrder.BY_MODEL).compare(memo4, memo5) > 0);
    }

    public void testSortOrder4() {
        addNodeWithSnii(nid3, "xxx", "qqq", "bbb", "ccd");
        addNodeWithSnii(nid4, "xxx", "qqq", "bbb", "ccd");
        addNodeWithSnii(nid5, "xxx", "qqq", "bbb", "ccc");
        MimicNodeStore.NodeMemo memo5 = store.findNode(nid5);
        MimicNodeStore.NodeMemo memo4 = store.findNode(nid4);
        MimicNodeStore.NodeMemo memo3 = store.findNode(nid3);
        assertTrue(new TreePane.Sorter(TreePane.SortOrder.BY_MODEL).compare(memo4, memo5) < 0);
        assertTrue(new TreePane.Sorter(TreePane.SortOrder.BY_MODEL).compare(memo3, memo5) < 0);
        assertTrue(new TreePane.Sorter(TreePane.SortOrder.BY_MODEL).compare(memo3, memo4) < 0);
    }

    public void testSortOrder5() {
        addNodeWithSnii(nid3, "", "", "", "");
        addNodeWithSnii(nid4, "", "", "", "");
        MimicNodeStore.NodeMemo memo4 = store.findNode(nid4);
        MimicNodeStore.NodeMemo memo3 = store.findNode(nid3);
        assertTrue(new TreePane.Sorter(TreePane.SortOrder.BY_MODEL).compare(memo3, memo4) < 0);
    }

    public void testSortOrder6() {
        addNodeWithSnii(nid3, "", "", "", "");
        addNodeWithSnii(nid4, "abcd", "", "", "");
        MimicNodeStore.NodeMemo memo4 = store.findNode(nid4);
        MimicNodeStore.NodeMemo memo3 = store.findNode(nid3);
        assertTrue(new TreePane.Sorter(TreePane.SortOrder.BY_MODEL).compare(memo3, memo4) > 0);
    }

    public void testSort() throws InvocationTargetException, InterruptedException {
        store.refresh(); // clears nid1.
        addNodeWithSnii(nid2, "xxx", "qqq", "aaa", "bbb");
        addNodeWithSnii(nid3, "yyy", "ppp", "ccc", "aaa");
        addNodeWithSnii(nid4, "xxx", "ppp", "bbb", "ccc");

        assertEquals(nid2.toString(), pane.nodes.getChildAt(0).toString().substring(0, 17));
        assertEquals(nid3.toString(), pane.nodes.getChildAt(1).toString().substring(0, 17));
        assertEquals(nid4.toString(), pane.nodes.getChildAt(2).toString().substring(0, 17));

        pane.setSortOrder(TreePane.SortOrder.BY_NAME);
        SwingUtilities.invokeAndWait(()->{});
        assertEquals(nid2.toString(), pane.nodes.getChildAt(0).toString().substring(0, 17));
        assertEquals(nid4.toString(), pane.nodes.getChildAt(1).toString().substring(0, 17));
        assertEquals(nid3.toString(), pane.nodes.getChildAt(2).toString().substring(0, 17));

        pane.setSortOrder(TreePane.SortOrder.BY_DESCRIPTION);
        SwingUtilities.invokeAndWait(()->{});
        assertEquals(nid3.toString(), pane.nodes.getChildAt(0).toString().substring(0, 17));
        assertEquals(nid2.toString(), pane.nodes.getChildAt(1).toString().substring(0, 17));
        assertEquals(nid4.toString(), pane.nodes.getChildAt(2).toString().substring(0, 17));

        pane.setSortOrder(TreePane.SortOrder.BY_MODEL);
        SwingUtilities.invokeAndWait(()->{});
        assertEquals(nid4.toString(), pane.nodes.getChildAt(0).toString().substring(0, 17));
        assertEquals(nid2.toString(), pane.nodes.getChildAt(1).toString().substring(0, 17));
        assertEquals(nid3.toString(), pane.nodes.getChildAt(2).toString().substring(0, 17));

        addNodeWithSnii(nid5, "xxx", "pqq", "bbb", "ccc");
        Thread.sleep(200);

        assertEquals(nid4.toString(), pane.nodes.getChildAt(0).toString().substring(0, 17));
        assertEquals(nid5.toString(), pane.nodes.getChildAt(1).toString().substring(0, 17));
        assertEquals(nid2.toString(), pane.nodes.getChildAt(2).toString().substring(0, 17));
        assertEquals(nid3.toString(), pane.nodes.getChildAt(3).toString().substring(0, 17));
    }

    public void testSortFallback() throws InvocationTargetException, InterruptedException {
        store.refresh(); // clears nid1.
        store.put(new ProtocolIdentificationReplyMessage(nid4, nid1, 0xF01800000000L), null);
        store.put(new ProtocolIdentificationReplyMessage(nid3, nid1, 0xF01800000000L), null);
        pane.setSortOrder(TreePane.SortOrder.BY_MODEL);
        SwingUtilities.invokeAndWait(()->{});

        assertEquals(nid3.toString(), pane.nodes.getChildAt(0).toString().substring(0, 17));
        assertEquals(nid4.toString(), pane.nodes.getChildAt(1).toString().substring(0, 17));
    }

    public void testWithSelect() {
        frame.setTitle("listener test");
        
        pane.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                JTree tree = (JTree) e.getSource();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                   tree.getLastSelectedPathComponent();
            
                if (node == null) return;
                System.out.print("Test prints selected treenode "+node);
                if (node.getUserObject() instanceof NodeTreeRep.SelectionKey) {
                    System.out.println(" and invokes");
                    ((NodeTreeRep.SelectionKey)node.getUserObject()).select((DefaultMutableTreeNode)node);
                } else {
                    System.out.println();
                }
            }
        });
        Message msg;
        msg = new ProducerIdentifiedMessage(nid2, eventA, EventState.Unknown);
        store.put(msg, null);
        store.put(pipmsg, null);

        msg = new SimpleNodeIdentInfoReplyMessage(nid2, nid2, 
                    new byte[]{0x01, 0x31, 0x32, 0x33, 0x41, 0x42, (byte)0xC2, (byte)0xA2, 0x44, 0x00}
                );
        store.put(msg, null);
    }

    public void testRefresh() {
        // fill up with nodes
        testNodeOrder();
        assertEquals(5, pane.nodes.getChildCount());
        store.refresh();
        assertEquals(0, pane.nodes.getChildCount());
    }
   
    // from here down is testing infrastructure
    
    public TreePaneTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {TreePaneTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TreePaneTest.class);
        return suite;
    }
}
