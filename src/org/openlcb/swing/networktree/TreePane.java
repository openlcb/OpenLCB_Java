// TreePane.java

package org.openlcb.swing.networktree;

import org.openlcb.Connection;
import org.openlcb.MimicNodeStore;
import org.openlcb.NodeID;
import org.openlcb.VerifyNodeIDNumberMessage;

import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
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
    }

    MimicNodeStore store;
    DefaultMutableTreeNode nodes;
    DefaultTreeModel treeModel;
    
    MimicNodeStore getStore() { return store; }
    DefaultTreeModel getTreeModel() { return treeModel; }
    JTree tree;
    
    NodeID nullNode = new NodeID(new byte[]{0,0,0,0,0,0});
    
    public void initComponents(MimicNodeStore store, final Connection connection, 
                                final NodeID node, final NodeTreeRep.SelectionKeyLoader loader) {
        this.store = store;
        
        nodes = new DefaultMutableTreeNode("OpenLCB Network");
    
        // build GUI
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        treeModel = new DefaultTreeModel(nodes);
        tree = new JTree(treeModel);
        tree.setEditable(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);  
        JScrollPane treeView = new JScrollPane(tree);
        add(treeView);

        // listen for newly arrived nodes
        store.addPropertyChangeListener(
            new PropertyChangeListener(){
            public void propertyChange(java.beans.PropertyChangeEvent e) { 

                if (e.getPropertyName().equals("AddNode")) {
                    MimicNodeStore.NodeMemo memo = (MimicNodeStore.NodeMemo) e.getNewValue();
                    if (!memo.getNodeID().equals(nullNode)) {
                        NodeTreeRep n = new NodeTreeRep(memo, getStore(), getTreeModel(), loader);
                        addNewHardwareNode(n);
                        n.initConnections();
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
        

    }

    /**
     * Adds an OpenLCB node into the tree of nodes shown.
     *
     * @param n the new node
     */
    private void addNewHardwareNode(NodeTreeRep n) {
        synchronized (nodes) {
            String newKey = n.toString();
            int i = 0;
            while (i < nodes.getChildCount() &&
                    nodes.getChildAt(i).toString().compareTo(newKey) < 0) ++i;
            treeModel.insertNodeInto(n, nodes, i);
        }
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
	
}
