// TreePane.java

package org.openlcb.swing.networktree;

import org.openlcb.Connection;
import org.openlcb.MimicNodeStore;
import org.openlcb.NodeID;
import org.openlcb.SimpleNodeIdent;
import org.openlcb.VerifyNodeIDNumberMessage;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Pane for monitoring an entire OpenLCB network as a logical tree
 *<p>
 *
 * @author	Bob Jacobsen   Copyright (C) 2010, 2012
 * @version	$Revision$
 */
public class TreePane extends JPanel  {

    public TreePane() {
	    super();
            timer = new Timer("OpenLCB Tree Pane Timer");
    }

    public enum SortOrder {
        BY_NODE_ID,
        BY_NAME,
        BY_DESCRIPTION,
        BY_MODEL,
    }

    MimicNodeStore store;
    DefaultMutableTreeNode nodes;
    DefaultTreeModel treeModel;
    
    MimicNodeStore getStore() { return store; }
    DefaultTreeModel getTreeModel() { return treeModel; }
    JTree tree;
    SortOrder sortOrder = SortOrder.BY_NODE_ID;

    NodeID nullNode = new NodeID(new byte[]{0,0,0,0,0,0});
    private Timer timer;
    private boolean needResortTree = false;

    // This listener ensures that if any node's SNIP data changes we resort the visible tree.
    PropertyChangeListener resortListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals(MimicNodeStore.NodeMemo.UPDATE_PROP_SIMPLE_NODE_IDENT)) {
                // This code will delay the updating of the display by a bit and coalesces update
                // commands. This way we can ensure we don't spend too much CPU repeatedly
                // updating the display.
                synchronized (timer) {
                    needResortTree = true;
                }
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        synchronized (timer) {
                            if (needResortTree) {
                                SwingUtilities.invokeLater(() -> resortTree());
                                needResortTree = false;
                            }
                        }
                    }
                }, 100);
            }
        }
    };

    public void initComponents(MimicNodeStore store, final Connection connection, 
                                final NodeID node, final NodeTreeRep.SelectionKeyLoader loader) {
        this.store = store;

        setPreferredSize(new Dimension(500, 700));
        nodes = new DefaultMutableTreeNode("OpenLCB Network");
    
        // build GUI
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        treeModel = new DefaultTreeModel(nodes);
        tree = new JTree(treeModel);
        tree.setEditable(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);  
        JScrollPane treeView = new JScrollPane(tree);
        add(treeView);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setToolTipText("Reloads network view including the status of all nodes.");
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                store.refresh();
            }
        });
        bottomPanel.add(btnRefresh);

        JButton btnSetSort = new JButton("Sort by...");
        btnSetSort.setToolTipText("Changes the sort order of the nodes.");
        btnSetSort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JPopupMenu sortMenu = new JPopupMenu();
                sortMenu.add(createSortMenuEntry("Node ID", SortOrder.BY_NODE_ID));
                sortMenu.add(createSortMenuEntry("Name", SortOrder.BY_NAME));
                sortMenu.add(createSortMenuEntry("Description", SortOrder.BY_DESCRIPTION));
                sortMenu.add(createSortMenuEntry("Mfg/Model", SortOrder.BY_MODEL));
                sortMenu.show(btnSetSort, 0, btnSetSort.getHeight());
            }
        });
        bottomPanel.add(btnSetSort);

        bottomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int)bottomPanel.getPreferredSize().getHeight()));
        add(bottomPanel);

        // listen for newly arrived nodes
        store.addPropertyChangeListener(
            new PropertyChangeListener(){
            public void propertyChange(java.beans.PropertyChangeEvent e) { 
                if (e.getPropertyName().equals(MimicNodeStore.ADD_PROP_NODE)) {
                    MimicNodeStore.NodeMemo memo = (MimicNodeStore.NodeMemo) e.getNewValue();
                    if (!memo.getNodeID().equals(nullNode)) {
                        NodeTreeRep n = new NodeTreeRep(memo, getStore(), getTreeModel(), loader);
                        addNewHardwareNode(n);
                        n.initConnections();
                        memo.addPropertyChangeListener(resortListener);
                    }
                } else if (e.getPropertyName().equals(MimicNodeStore.CLEAR_ALL_NODES)) {
                    synchronized (nodes) {
                        nodes.removeAllChildren();
                        treeModel.nodeStructureChanged(nodes);
                        SwingUtilities.invokeLater(()->tree.expandPath(new TreePath(nodes.getPath())));
                    }
                }
            }
        });

        // add nodes that exist now
        for (MimicNodeStore.NodeMemo memo : store.getNodeMemos() ) {
            if (!memo.getNodeID().equals(nullNode)) {
                NodeTreeRep n = new NodeTreeRep(memo, store, treeModel, loader);
                addNewHardwareNode(n);
                n.initConnections();
            }
        }
        
        // start with top level expanded
        tree.expandPath(new TreePath(nodes.getPath()));

        // kick off a listen when connection ready
        Connection.ConnectionListener cl = new Connection.ConnectionListener(){
            public void connectionActive(Connection c) {
                // load the alias field
                connection.put(new VerifyNodeIDNumberMessage(node), null);
            }
        };
        if (connection != null) connection.registerStartNotification(cl);

        SwingUtilities.invokeLater(() -> {
            Window win = SwingUtilities.getWindowAncestor(this);
            if (win == null) {
                System.out.println("Could not add close window listener");
                return;
            }
            win.addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent windowEvent) {
                }

                @Override
                public void windowClosing(WindowEvent windowEvent) {
                    release();
                }

                @Override
                public void windowClosed(WindowEvent windowEvent) {
                }

                @Override
                public void windowIconified(WindowEvent windowEvent) {

                }

                @Override
                public void windowDeiconified(WindowEvent windowEvent) {

                }

                @Override
                public void windowActivated(WindowEvent windowEvent) {

                }

                @Override
                public void windowDeactivated(WindowEvent windowEvent) {

                }
            });
        });
    }

    private JMenuItem createSortMenuEntry(String text, final SortOrder order) {
        AbstractAction action = new AbstractAction(text) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setSortOrder(order);
            }
        };
        JCheckBoxMenuItem it = new JCheckBoxMenuItem(action);
        it.setState(order == sortOrder);
        return it;
    }

    /**
     * Adds an OpenLCB node into the tree of nodes shown.
     *
     * @param n the new node
     */
    private void addNewHardwareNode(NodeTreeRep n) {
        synchronized (nodes) {
            Comparator<NodeTreeRep> s = getSorter();
            int i = 0;
            while (i < nodes.getChildCount() &&
                    s.compare((NodeTreeRep) nodes.getChildAt(i), n) < 0) ++i;
            treeModel.insertNodeInto(n, nodes, i);
        }
    }

    /**
     * Sets the sort order to be used in the tree;
     *
     * @param order new order.
     */
    public void setSortOrder(SortOrder order) {
        if (sortOrder == order) return;
        sortOrder = order;
        SwingUtilities.invokeLater(() -> resortTree());
    }

    public void addTreeSelectionListener(final TreeSelectionListener listener) {
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
                listener.valueChanged(treeSelectionEvent);
                tree.getSelectionModel().clearSelection();
            }
        });
    }

    private void resortTree() {
        synchronized (nodes) {
            ArrayList<NodeTreeRep> list = new ArrayList<>(nodes.getChildCount());
            for (int i = 0; i < nodes.getChildCount(); ++i) {
                list.add((NodeTreeRep) nodes.getChildAt(i));
            }
            list.sort(getSorter());
            nodes.removeAllChildren();
            for (NodeTreeRep ch : list) {
                nodes.add(ch);
            }
            treeModel.nodeStructureChanged(nodes);
        }
    }

    Comparator<NodeTreeRep> getSorter() {
        return new Sorter(sortOrder);
    }

    public static class Sorter implements Comparator<NodeTreeRep> {
        private final SortOrder sortOrder;

        public Sorter(SortOrder order) {
            sortOrder = order;
        }

        private String findCompareKey(MimicNodeStore.NodeMemo memo) {
            SimpleNodeIdent ident = memo.getSimpleNodeIdent();
            if (ident == null) return null;
            String s = null, t = null;
            switch (sortOrder) {
                case BY_NODE_ID:
                    return null;
                case BY_NAME:
                    s = ident.getUserName();
                    t = ident.getUserDesc();
                    break;
                case BY_DESCRIPTION:
                    s = ident.getUserDesc();
                    t = ident.getUserName();
                    break;
                case BY_MODEL:
                    s = ident.getMfgName();
                    t = ident.getModelName();
                    break;
            }
            if (s == null) s = "";
            if (t == null) t = "";
            if (s.isEmpty() && t.isEmpty()) return null;
            return s + "\0" + t;
        }

        public int compare(MimicNodeStore.NodeMemo m1, MimicNodeStore.NodeMemo m2) {
            String entry1 = findCompareKey(m1);
            String entry2 = findCompareKey(m2);
            if (entry1 == null && entry2 == null) {
                return m1.getNodeID().toString().compareTo(m2.getNodeID().toString());
            } else if (entry1 == null) {
                return 1;
            } else if (entry2 == null) {
                return -1;
            } else {
                int cc = entry1.compareTo(entry2);
                if (cc != 0) {
                    return cc;
                }
                return m1.getNodeID().toString().compareTo(m2.getNodeID().toString());
            }
        }

        @Override
        public int compare(NodeTreeRep n1, NodeTreeRep n2) {
            MimicNodeStore.NodeMemo m1 = n1.memo;
            MimicNodeStore.NodeMemo m2 = n2.memo;
            return compare(m1, m2);
        }
    }

    /**
     * Cleans up all property change listeners etc in preparation when closing the window.
     */
    public void release() {
        timer.cancel(); 
    }

}
