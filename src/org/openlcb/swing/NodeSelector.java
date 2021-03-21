// NodeSelector.java

package org.openlcb.swing;

import static org.openlcb.MimicNodeStore.ADD_PROP_NODE;
import static org.openlcb.MimicNodeStore.CLEAR_ALL_NODES;
import static org.openlcb.MimicNodeStore.NodeMemo.UPDATE_PROP_SIMPLE_NODE_IDENT;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.openlcb.MimicNodeStore;
import org.openlcb.NodeID;
import org.openlcb.SimpleNodeIdent;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Java Swing component to select a node, populated
 * from a MimicNodeStore
 *
 * @author	Bob Jacobsen   Copyright (C) 2012
 * @version	$Revision$
 */
public class NodeSelector extends JPanel  {
    /** Comment for <code>serialVersionUID</code>. */
    private static final long serialVersionUID = 1714640844679691939L;
    
    private final PropertyChangeListener propertyChangeListener;
    MimicNodeStore store;
    JComboBox<ModelEntry> box;
    private DefaultComboBoxModel<ModelEntry> model = new DefaultComboBoxModel<ModelEntry>();
    private boolean seenLight = false;

    public NodeSelector(MimicNodeStore store) {
        this.store = store;

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        box = new JComboBox<ModelEntry>(model);
        add(box);
        box.setPrototypeDisplayValue(new ModelEntry("01.02.03.04.05.06"
                + " - East Pershing Tower Node"
                + " - Some Description Here"));

        // listen for newly arrived nodes
        propertyChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(ADD_PROP_NODE)) {
                    MimicNodeStore.NodeMemo memo = (MimicNodeStore.NodeMemo) e
                            .getNewValue();
                    newNodeInList(memo);
                } else if (e.getPropertyName().equals(CLEAR_ALL_NODES)) {
                    clearList();
                }
            }
        };
        store.addPropertyChangeListener(propertyChangeListener);

        // add existing nodes
        for (MimicNodeStore.NodeMemo memo : store.getNodeMemos() ) {
            newNodeInList(memo);
        }

        addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent hierarchyEvent) {
                if (isDisplayable()) {
                    seenLight = true;
                } else if (seenLight) {
                    log.finest("NodeSelector disposing due to hierarchy changes.");
                    removeHierarchyListener(this);
                    dispose();
                }
            }
        });
    }

    protected class ModelEntry implements Comparable<ModelEntry>, PropertyChangeListener {
        final MimicNodeStore.NodeMemo nodeMemo;
        String description = "";

        ModelEntry(MimicNodeStore.NodeMemo memo) {
            this.nodeMemo = memo;
            //log.finest("registering listener for " + memo.getNodeID() + " " + memo.toString());
            memo.addPropertyChangeListener(this);
            updateDescription();
        }

        /**
         * Constructor for prototype display value
         * 
         * @param description prototype display value
         */
        private ModelEntry(String description) {
            this.nodeMemo = null;
            this.description = description;
        }

        public NodeID getNodeID() {
            return nodeMemo.getNodeID();
        }

        private void updateDescription() {
            SimpleNodeIdent ident = nodeMemo.getSimpleNodeIdent();
            StringBuilder sb = new StringBuilder();
            sb.append(nodeMemo.getNodeID().toString());
            int count = 0;
            if (count < 2) {
                count += addToDescription(ident.getUserName(), sb);
            }
            if (count < 2) {
                count += addToDescription(ident.getUserDesc(), sb);
            }
            if (count < 2) {
                count += addToDescription(ident.getMfgName() + ident.getModelName(),
                        sb);
            }
            if (count < 2) {
                count += addToDescription(ident.getSoftwareVersion(), sb);
            }
            String newDescription = sb.toString();
            if (!description.equals(newDescription)) {
                description = newDescription;
                // update combo box model.
                updateComboBoxModelEntry(this);
            }
        }

        private int addToDescription(String s, StringBuilder sb) {
            if (s.isEmpty()) {
                return 0;
            }
            sb.append(" - ");
            sb.append(s);
            return 1;
        }

        private long reorder(long n) {
            return (n < 0) ? Long.MAX_VALUE - n : Long.MIN_VALUE + n;
        }

        @Override
        public int compareTo(ModelEntry otherEntry) {
            long l1 = reorder(getNodeID().toLong());
            long l2 = reorder(otherEntry.getNodeID().toLong());
            return Long.compare(l1, l2);
        }

        @Override
        public String toString() {
            return description;
        }

        @Override
        @SuppressFBWarnings(value = "EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS",
                justification = "Purposefully attempting lookup using NodeID argument in model " +
                        "vector.")
        public boolean equals(Object o) {
            if (o instanceof ModelEntry) {
                return getNodeID().equals(((ModelEntry) o).getNodeID());
            }
            if (o instanceof NodeID) {
                return getNodeID().equals(o);
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return getNodeID().hashCode();
        }

        @Override
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            //log.warning("Received model entry update for " + nodeMemo.getNodeID());
            if (propertyChangeEvent.getPropertyName().equals(UPDATE_PROP_SIMPLE_NODE_IDENT)) {
                updateDescription();
            }
        }

        public void dispose() {
            //log.warning("dispose of " + nodeMemo.getNodeID().toString());
            nodeMemo.removePropertyChangeListener(this);
        }
    }

    // Notifies that the contents ofa given entry have changed. This will delete and re-add the
    // entry to the model, forcing a refresh of the box.
    private void updateComboBoxModelEntry(ModelEntry modelEntry) {
        int idx = model.getIndexOf(modelEntry.getNodeID());
        if (idx < 0) {
            return;
        }
        ModelEntry last = model.getElementAt(idx);
        if (last != modelEntry) {
            // not the same object -- we're talking about an abandoned entry.
            modelEntry.dispose();
            return;
        }
        ModelEntry sel = (ModelEntry) model.getSelectedItem();
        model.removeElementAt(idx);
        model.insertElementAt(modelEntry, idx);
        model.setSelectedItem(sel);
    }

    // Adds a new item to the model, maintaining sort order.
    private void newNodeInList(MimicNodeStore.NodeMemo memo) {
        int i = 0;
        if (model.getIndexOf(memo.getNodeID()) >= 0) {
            // already exists. Do nothing.
            return;
        }
        ModelEntry e = new ModelEntry(memo);
        while ((i < model.getSize()) && (model.getElementAt(i).compareTo(e) < 0)) {
            ++i;
        }
        model.insertElementAt(e, i);
    }

    // Removes all entries from the model list, disposing them in the process.
    private void clearList() {
        for (int i = 0; i < model.getSize(); ++i) {
            model.getElementAt(i).dispose();
        }
        model.removeAllElements();
    }

    public void dispose() {
        clearList();
        store.removePropertyChangeListener(propertyChangeListener);
    }

    public NodeID getSelectedItem() {
        return ((ModelEntry) box.getSelectedItem()).getNodeID();
    }

    private static final Logger log = Logger.getLogger(NodeSelector.class.getName());
}
