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


        // listen for newly arrived nodes
        store.addPropertyChangeListener(
            new PropertyChangeListener(){
            public void propertyChange(java.beans.PropertyChangeEvent e) { 

                if (e.getPropertyName().equals("AddNode")) {
                    MimicNodeStore.NodeMemo memo = (MimicNodeStore.NodeMemo) e.getNewValue();
    
                    treeModel.insertNodeInto(new NodeTreeRep(memo, getStore(), getTreeModel()), nodes,
                                 nodes.getChildCount());
                }
            }
        });

        // add nodes that exist now
        for (MimicNodeStore.NodeMemo memo : store.getNodeMemos() ) {
            nodes.add(new NodeTreeRep(memo, store, treeModel));
        }

        // kick off a listen when connection ready
        Connection.ConnectionListener cl = new Connection.ConnectionListener(){
            public void connectionActive(Connection c) {
                // load the alias field
                connection.put(new VerifyNodeIDNumberMessage(node), null);
            }
        };
        if (connection != null) connection.registerStartNotification(cl);
        
        // build GUI
        treeModel = new DefaultTreeModel(nodes);
        JTree tree = new JTree(treeModel);
        tree.setEditable(true);
        JScrollPane treeView = new JScrollPane(tree);
        add(treeView);

    }
	
}
