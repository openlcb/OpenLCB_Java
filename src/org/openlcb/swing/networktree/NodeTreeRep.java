// NodeTreeRep.java

package org.openlcb.swing.networktree;

import javax.swing.*;
import javax.swing.tree.*;
import java.beans.PropertyChangeListener;

import org.openlcb.*;

/**
 * Represent a single node for the tree display
 *<p>
 *
 * @author	Bob Jacobsen   Copyright (C) 2010, 2012
 * @version	$Revision$
 */
public class NodeTreeRep extends DefaultMutableTreeNode  {

    MimicNodeStore.NodeMemo memo;
    MimicNodeStore store;
    DefaultTreeModel treeModel;
    
    DefaultMutableTreeNode getThis() { return this; }
    DefaultTreeModel getTreeModel() { return treeModel; }
    
    public NodeTreeRep(MimicNodeStore.NodeMemo memo, MimicNodeStore store, DefaultTreeModel treeModel) {
	    super("Node");
	    this.memo = memo;
	    this.store = store;
	    this.treeModel = treeModel;
	    
        // listen for more info arriving
        memo.addPropertyChangeListener(
            new PropertyChangeListener(){
            public void propertyChange(java.beans.PropertyChangeEvent e) { 

                if (e.getPropertyName().equals("updateProtocol")) {
                    getTreeModel().insertNodeInto(new DefaultMutableTreeNode("Supported Protocols"), getThis(),
                                 getThis().getChildCount());
                
                }
                if (e.getPropertyName().equals("updateConsumers")) {
                    getTreeModel().insertNodeInto(new DefaultMutableTreeNode("Supported Consumers"), getThis(),
                                 getThis().getChildCount());
                
                }
                if (e.getPropertyName().equals("updateProducers")) {
                    getTreeModel().insertNodeInto(new DefaultMutableTreeNode("Supported Producers"), getThis(),
                                 getThis().getChildCount());
                
                }
            }
        });
    }
    
    /**
     * Provides the node label in the tree
     */
    public String toString() {
        return memo.getNodeID().toString();
    }
	
}
