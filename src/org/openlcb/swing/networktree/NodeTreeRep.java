// NodeTreeRep.java

package org.openlcb.swing.networktree;

import org.openlcb.MimicNodeStore;
import org.openlcb.NodeID;
import org.openlcb.ProtocolIdentification;
import org.openlcb.SimpleNodeIdent;

import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

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
    String nodeDescription = "";

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

    enum InfoNodes {
        MFG("Mfg"),
        MODEL("Mod"),
        HARDWARE_VERSION("Hardware"),
        SOFTWARE_VERSION("Software"),
        USER_NAME("Name"),
        USER_DESC("Desc"),
        NUM_NODES("");

        public final String name;
        InfoNodes(String n) { name = n; }
    }
    DefaultMutableTreeNode simpleInfoNodes[] = new DefaultMutableTreeNode[InfoNodes.NUM_NODES.ordinal()];

    void updateSimpleInfoNode(InfoNodes node, String value) {
        int num = node.ordinal();
        if (value == null || value.trim().length() == 0) return;
        String name = node.name + ": " + value;
        if (simpleInfoNodes[num] == null) {
            simpleInfoNodes[num] = newNode(name);
            getTreeModel().insertNodeInto(simpleInfoNodes[num],
                    getThis(),
                    getThis().getChildCount());
        } else {
            simpleInfoNodes[num].setUserObject(name);
            treeModel.nodeChanged(simpleInfoNodes[num]);
        }
    }

    synchronized void updateSimpleNodeIdent(SimpleNodeIdent e) {
        updateSimpleInfoNode(InfoNodes.MFG, e.getMfgName());
        updateSimpleInfoNode(InfoNodes.MODEL, e.getModelName());
        updateSimpleInfoNode(InfoNodes.HARDWARE_VERSION, e.getHardwareVersion());
        updateSimpleInfoNode(InfoNodes.SOFTWARE_VERSION, e.getSoftwareVersion());
        updateSimpleInfoNode(InfoNodes.USER_NAME, e.getUserName());
        updateSimpleInfoNode(InfoNodes.USER_DESC, e.getUserDesc());

        StringBuilder b = new StringBuilder();
        String n = e.getUserName().trim();
        if (!n.isEmpty()) {
            b.append(n);
        }
        n = e.getUserDesc().trim();
        if (!n.isEmpty()) {
            if (b.length() > 0) b.append(" - ");
            b.append(n);
        }
        String newDesc = b.toString();
        if (!nodeDescription.equals(newDesc)) {
            nodeDescription = newDesc;
            treeModel.nodeChanged(this);
        }
    }
    
    DefaultMutableTreeNode pipNode;
    DefaultMutableTreeNode openConfigNode = null;

    synchronized void updateProtocolIdent(ProtocolIdentification pi) {
        if (pi.getValue() != 0) {

            if (pipNode == null) {
                pipNode = newNode("Supported Protocols");
                getTreeModel().insertNodeInto(pipNode, getThis(),
                         getThis().getChildCount());
            }

            pipNode.removeAllChildren();

            List<ProtocolIdentification.Protocol> protocols = pi.getProtocols();
    
            for (ProtocolIdentification.Protocol p : protocols) {
                DefaultMutableTreeNode node = null;
                
                // try to figure out type
                switch (p) {
                    case ConfigurationDescription:
                        node = newNode(p.getName(), loader.cdiKey(p.getName(), memo.getNodeID()));
                        if (openConfigNode == null) {
                            openConfigNode = newNode(null, loader.cdiKey("Open Configuration dialog", memo.getNodeID()));
                            getTreeModel().insertNodeInto(openConfigNode, getThis(), 0);
                        }
                        break;
                    case ProtocolIdentification:
                        node = newNode(p.getName(), loader.pipKey(p.getName(), memo.getNodeID()));
                        break;
                    case Datagram:
                        node = newNode(p.getName(), loader.datagramKey(p.getName(), memo.getNodeID()));
                        break;
                    case Configuration:
                        node = newNode(p.getName(), loader.configurationKey(p.getName(), memo.getNodeID()));
                        break;
                    case SimpleNodeID:
                        node = newNode(p.getName(), loader.sniiKey(p.getName(), memo.getNodeID()));
                        break;
                    default:
                        node = newNode(p.getName());
                        break;
                }
                
                getTreeModel().insertNodeInto(node, pipNode,
                             pipNode.getChildCount());
            }

            getTreeModel().nodeStructureChanged(pipNode);
        }
    }
    
    /**
     * Provides the node label in the tree.
     * Currently implemented as toString name of underling nodeID.
     */
    public String toString() {
        if (nodeDescription.isEmpty()) {
            return memo.getNodeID().toString();
        } else {
            return memo.getNodeID().toString() + " - " + nodeDescription;
        }
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
	    
	    /**
	     * Override here to change behavior when 
	     * treenode is selected.
         * @param rep    the node selected by the user
         */
	    public void select(DefaultMutableTreeNode rep) {
	        // System.out.println("Selected: "+rep+" for "+name+" on "+node);
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
