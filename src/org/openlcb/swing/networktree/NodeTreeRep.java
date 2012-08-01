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
    
    SelectionKeyLoader loader;
        
    public NodeTreeRep(MimicNodeStore.NodeMemo memo, MimicNodeStore store, DefaultTreeModel treeModel, SelectionKeyLoader loader) {
	    super("Node");
	    this.memo = memo;
	    this.store = store;
	    this.treeModel = treeModel;
	    this.loader = loader;
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
                    getTreeModel().insertNodeInto(newNode("Supported Consumers"), getThis(),
                                 getThis().getChildCount());
                
                }
                if (e.getPropertyName().equals("updateProducers")) {
                    getTreeModel().insertNodeInto(newNode("Supported Producers"), getThis(),
                                 getThis().getChildCount());
                
                }
            }
        });
        
        // see if protocol info already present
        ProtocolIdentification pip = store.getProtocolIdentification(memo.getNodeID());
        if (pip != null) updateProtocolIdent(pip);  // otherwise, will be notified later

        // see if simple ID info already present
        SimpleNodeIdent snii = store.getSimpleNodeIdent(memo.getNodeID());
        if (snii != null) updateSimpleNodeIdent(snii);  // otherwise, will be notified later

    }
    
    DefaultMutableTreeNode newNode(String name, SelectionKey key) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
        node.setUserObject(key);
        return node;
    }
    
    DefaultMutableTreeNode newNode(String name) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
        node.setUserObject(new SelectionKey(name, memo.getNodeID()));
        return node;
    }
    
    void updateSimpleNodeIdent(SimpleNodeIdent e) {
        if (simpleInfoMfgNode == null) {
            if (e.getMfgName().replace(" ","").length()>0) {
                simpleInfoMfgNode = newNode("Mfg: "+e.getMfgName());
                getTreeModel().insertNodeInto(simpleInfoMfgNode, 
                            getThis(),
                            getThis().getChildCount());
            }
        } else {
            simpleInfoMfgNode.setUserObject("Mfg: "+e.getMfgName());
        }
        if (simpleInfoModelNode == null) {
            if (e.getModelName().replace(" ","").length()>0) {
                simpleInfoModelNode = newNode("Mod: "+e.getModelName());
                getTreeModel().insertNodeInto(simpleInfoModelNode, 
                            getThis(),
                            getThis().getChildCount());
            }       
        } else {
            simpleInfoModelNode.setUserObject("Mod: "+e.getModelName());
        }
        if (simpleInfoHardwareVersionNode == null) {
            if (e.getHardwareVersion().replace(" ","").length()>0) {
                simpleInfoHardwareVersionNode = newNode("Hardware: "+e.getHardwareVersion());
                getTreeModel().insertNodeInto(simpleInfoHardwareVersionNode, 
                            getThis(),
                            getThis().getChildCount());
            }
        } else {
            simpleInfoHardwareVersionNode.setUserObject("Hardware: "+e.getHardwareVersion());
        }
        if (simpleInfoSoftwareVersionNode == null) {
            if (e.getSoftwareVersion().replace(" ","").length()>0) {
                simpleInfoSoftwareVersionNode = newNode("Software: "+e.getSoftwareVersion());
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
                simpleInfoUserDescNode = newNode("Desc: "+e.getUserDesc());
                getTreeModel().insertNodeInto(simpleInfoUserDescNode, 
                            getThis(),
                            getThis().getChildCount());
            }
        } else {
            simpleInfoUserDescNode.setUserObject("Desc: "+e.getUserDesc());
        }
    }
    
    DefaultMutableTreeNode pipNode;
    
    void updateProtocolIdent(ProtocolIdentification pi) {
        if (pi.getValue() != 0) {

            if (pipNode == null) {
                pipNode = newNode("Supported Protocols");
                getTreeModel().insertNodeInto(pipNode, getThis(),
                         getThis().getChildCount());
            }
                
            List<ProtocolIdentification.Protocol> protocols = pi.getProtocols();
    
            for (ProtocolIdentification.Protocol p : protocols) {
                DefaultMutableTreeNode node = null;
                
                // try to figure out type
                switch (p) {
                    case ConfigurationDescription:
                        node = newNode(p.getName(), loader.cdiKey(p.getName(), memo.getNodeID()));
                        break;
                    case ProtocolIdentification:
                    case Datagram:
                    case Configuration:
                    case SimpleNodeID:
                    default:
                        node = newNode(p.getName());
                        break;
                }
                
                getTreeModel().insertNodeInto(node, pipNode,
                             pipNode.getChildCount());
            }
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
	
	
	/**
	 * When a JTree node is selected, it's user object
	 * (of this class) is pulled and invoked.
	 *
	 * Inherit from this to modify.
	 */
	static public class SelectionKey {
	    public SelectionKey(String name, NodeID node) { this.name = name; this.node = node; }
	    protected String name;
	    protected NodeID node;
	    public void select(DefaultMutableTreeNode rep) {
	        System.out.println("Selected: "+rep+" for "+name+" on "+node);
	    }
	    public String toString() {
	        return name;
	    }
	}
	
	/**
	* Invoked for various protocols to load the
	* selection key object
	*/
	static public class SelectionKeyLoader {
	    public SelectionKey pipKey(String name, NodeID node) {
	        return new SelectionKey(name, node);
	    }
	    public SelectionKey sniiKey(String name, NodeID node) {
	        return new SelectionKey(name, node);
	    }
	    public SelectionKey datagramKey(String name, NodeID node) {
	        return new SelectionKey(name, node);
	    }
	    public SelectionKey configurationKey(String name, NodeID node) {
	        return new SelectionKey(name, node);
	    }
	    public SelectionKey cdiKey(String name, NodeID node) {
	        return new SelectionKey(name, node);
	    }
	}

}
