// NodeTreeRep.java

package org.openlcb.swing.networktree;

import javax.swing.*;
import javax.swing.tree.*;
import java.beans.PropertyChangeListener;
import java.util.List;

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
    }
    
    void initConnections() {
        // listen for more info arriving
        memo.addPropertyChangeListener(
            new PropertyChangeListener(){
            public void propertyChange(java.beans.PropertyChangeEvent e) { 

                if (e.getPropertyName().equals("updateProtocol")) {
                    updateProtocolIdent((ProtocolIdentification)e.getNewValue());                
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
        SimpleNodeIdent snii = store.getSimpleNodeIdent(memo.getNodeID());
        if (snii != null) updateSimpleNodeIdent(snii);  // otherwise, will be notified later

        // see if protocol info already present
        ProtocolIdentification pip = store.getProtocolIdentification(memo.getNodeID());
        if (pip != null) updateProtocolIdent(pip);  // otherwise, will be notified later
    }
    
    void updateSimpleNodeIdent(SimpleNodeIdent e) {
        if (simpleInfoMfgNode == null) {
            if (e.getMfgName().replace(" ","").length()>0) {
                simpleInfoMfgNode = new DefaultMutableTreeNode("Mfg: "+e.getMfgName());
                getTreeModel().insertNodeInto(simpleInfoMfgNode, 
                            getThis(),
                            getThis().getChildCount());
            }
        } else {
            simpleInfoMfgNode.setUserObject("Mfg: "+e.getMfgName());
        }
        if (simpleInfoModelNode == null) {
            if (e.getModelName().replace(" ","").length()>0) {
                simpleInfoModelNode = new DefaultMutableTreeNode("Mod: "+e.getModelName());
                getTreeModel().insertNodeInto(simpleInfoModelNode, 
                            getThis(),
                            getThis().getChildCount());
            }       
        } else {
            simpleInfoModelNode.setUserObject("Mod: "+e.getModelName());
        }
        if (simpleInfoHardwareVersionNode == null) {
            if (e.getHardwareVersion().replace(" ","").length()>0) {
                simpleInfoHardwareVersionNode = new DefaultMutableTreeNode("Hardware: "+e.getHardwareVersion());
                getTreeModel().insertNodeInto(simpleInfoHardwareVersionNode, 
                            getThis(),
                            getThis().getChildCount());
            }
        } else {
            simpleInfoHardwareVersionNode.setUserObject("Hardware: "+e.getHardwareVersion());
        }
        if (simpleInfoSoftwareVersionNode == null) {
            if (e.getSoftwareVersion().replace(" ","").length()>0) {
                simpleInfoSoftwareVersionNode = new DefaultMutableTreeNode("Software: "+e.getSoftwareVersion());
                getTreeModel().insertNodeInto(simpleInfoSoftwareVersionNode, 
                            getThis(),
                            getThis().getChildCount());
            }
        } else {
            simpleInfoSoftwareVersionNode.setUserObject("Software: "+e.getSoftwareVersion());
        }
        if (simpleInfoUserNameNode == null) {
            if (e.getUserName().replace(" ","").length()>0) {
                simpleInfoUserNameNode = new DefaultMutableTreeNode("Name: "+e.getUserName());
                getTreeModel().insertNodeInto(simpleInfoUserNameNode, 
                            getThis(),
                            getThis().getChildCount());
            }
        } else {
            simpleInfoUserNameNode.setUserObject("Name: "+e.getUserName());
        }
        if (simpleInfoUserDescNode == null ) { 
            if (e.getUserDesc().replace(" ","").length()>0) {
                simpleInfoUserDescNode = new DefaultMutableTreeNode("Desc: "+e.getUserDesc());
                getTreeModel().insertNodeInto(simpleInfoUserDescNode, 
                            getThis(),
                            getThis().getChildCount());
            }
        } else {
            simpleInfoUserDescNode.setUserObject("Desc: "+e.getUserDesc());
        }
    }
    
    void updateProtocolIdent(ProtocolIdentification pi) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Supported Protocols");
        getTreeModel().insertNodeInto(node, getThis(),
                     getThis().getChildCount());

        List<String> protocols = pi.getProtocols();

        for (String s : protocols) {

            getTreeModel().insertNodeInto(new DefaultMutableTreeNode(s), node,
                         node.getChildCount());
        }
    }
    
    DefaultMutableTreeNode simpleInfoMfgNode;
    DefaultMutableTreeNode simpleInfoModelNode;
    DefaultMutableTreeNode simpleInfoHardwareVersionNode;
    DefaultMutableTreeNode simpleInfoSoftwareVersionNode;
    DefaultMutableTreeNode simpleInfoUserNameNode;
    DefaultMutableTreeNode simpleInfoUserDescNode;
    
    /**
     * Provides the node label in the tree
     */
    public String toString() {
        return memo.getNodeID().toString();
    }
	
}
