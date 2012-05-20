// NodeSelector.java

package org.openlcb.swing;

import javax.swing.*;
import javax.swing.text.*;
import java.beans.PropertyChangeListener;

import org.openlcb.*;
import org.openlcb.implementations.*;

/**
 * Java Swing component to select a node, populated
 * from a MimicNodeStore
 *
 * @author	Bob Jacobsen   Copyright (C) 2012
 * @version	$Revision$
 */
public class NodeSelector extends JPanel  {
    
    MimicNodeStore store;
    JComboBox box;
    public NodeSelector(MimicNodeStore store) {
        this.store = store;
        
        box = new JComboBox();
        add(box);

        // listen for newly arrived nodes
        store.addPropertyChangeListener(
            new PropertyChangeListener(){
            public void propertyChange(java.beans.PropertyChangeEvent e) { 

                if (e.getPropertyName().equals("AddNode")) {
                    MimicNodeStore.NodeMemo memo = (MimicNodeStore.NodeMemo) e.getNewValue();
                    box.addItem(memo.getNodeID());
                }
            }
        });

        // add existing nodes
        for (MimicNodeStore.NodeMemo memo : store.getNodeMemos() ) {
            box.addItem(memo.getNodeID());
        }
    
        // set up to add more as they become available
        
    }
    
    public Object getSelectedItem() {
        return box.getSelectedItem();
    }
    
}
