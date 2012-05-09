// TreePane.java

package org.openlcb.swing.networktree;

import javax.swing.*;
import javax.swing.tree.*;
import java.beans.PropertyChangeListener;

import org.openlcb.*;

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
    
    public void initComponents(MimicNodeStore store, final Connection connection, final NodeID node) {

        this.store = store;
        
        nodes = new DefaultMutableTreeNode("OpenLCB Network");

        // build GUI
        treeModel = new DefaultTreeModel(nodes);
        JTree tree = new JTree(treeModel);
        tree.setEditable(true);
        JScrollPane treeView = new JScrollPane(tree);
        add(treeView);

        // listen for newly arrived nodes
        store.addPropertyChangeListener(
            new PropertyChangeListener(){
            public void propertyChange(java.beans.PropertyChangeEvent e) { 

                if (e.getPropertyName().equals("AddNode")) {
                    MimicNodeStore.NodeMemo memo = (MimicNodeStore.NodeMemo) e.getNewValue();
    
                    NodeTreeRep n = new NodeTreeRep(memo, getStore(), getTreeModel());
                    treeModel.insertNodeInto(n, nodes,
                                 nodes.getChildCount());
                    n.initConnections();
                }
            }
        });

        // add nodes that exist now
        for (MimicNodeStore.NodeMemo memo : store.getNodeMemos() ) {
            NodeTreeRep n = new NodeTreeRep(memo, store, treeModel);
            nodes.add(n);
            n.initConnections();
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
	
}
