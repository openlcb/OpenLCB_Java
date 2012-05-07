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
                if (e.getPropertyName().equals("updateSimpleNodeIdent")) {
                    updateSimpleNodeIdent((SimpleNodeIdent)e.getNewValue());
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
        
        // see if simple ID info already present
        SimpleNodeIdent id = store.getSimpleNodeIdent(memo.getNodeID());
        if (id != null) updateSimpleNodeIdent(id);  // otherwise, will be notified later
    }
    
    void updateSimpleNodeIdent(SimpleNodeIdent e) {
        if (simpleInfoMfgNode == null) {
            simpleInfoMfgNode = new DefaultMutableTreeNode("Mfg: "+e.getMfgName());
            getTreeModel().insertNodeInto(simpleInfoMfgNode, getThis(),
                         getThis().getChildCount());
        } else {
            simpleInfoMfgNode.setUserObject("Mfg: "+e.getMfgName());
        }
        if (simpleInfoModelNode == null) {
            simpleInfoModelNode = new DefaultMutableTreeNode("Mod: "+e.getModelName());
            getTreeModel().insertNodeInto(simpleInfoModelNode, getThis(),
                         getThis().getChildCount());
        } else {
            simpleInfoModelNode.setUserObject("Mod: "+e.getModelName());
        }
        if (simpleInfoVersionNode == null) {
            simpleInfoVersionNode = new DefaultMutableTreeNode("Ver: "+e.getVersion());
            getTreeModel().insertNodeInto(simpleInfoVersionNode, getThis(),
                         getThis().getChildCount());
        } else {
            simpleInfoVersionNode.setUserObject("Ver: "+e.getVersion());
        }
    }
    
    DefaultMutableTreeNode simpleInfoMfgNode;
    DefaultMutableTreeNode simpleInfoModelNode;
    DefaultMutableTreeNode simpleInfoVersionNode;
    
    /**
     * Provides the node label in the tree
     */
    public String toString() {
        return memo.getNodeID().toString();
    }
	
}
