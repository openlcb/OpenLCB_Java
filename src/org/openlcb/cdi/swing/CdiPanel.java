package org.openlcb.cdi.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.charset.Charset;
import javax.swing.*;

import org.openlcb.EventID;
import org.openlcb.cdi.CdiRep;
import org.openlcb.cdi.impl.ConfigRepresentation;
import org.openlcb.implementations.MemoryConfigurationService;
import org.openlcb.swing.EventIdTextField;

import static org.openlcb.cdi.impl.ConfigRepresentation.UPDATE_ENTRY_DATA;
import static org.openlcb.cdi.impl.ConfigRepresentation.UPDATE_REP;
import static org.openlcb.cdi.impl.ConfigRepresentation.UPDATE_STATE;

/**
 * Simple example CDI display.
 *
 * Works with a CDI reader.
 *
 * @author  Bob Jacobsen   Copyright 2011
 * @author  Paul Bender Copyright 2016
 * @author  Balazs Racz Copyright 2016
 */
public class CdiPanel extends JPanel {

    private ConfigRepresentation rep;

    public CdiPanel () { super(); }
    
    /**
     * @param rep Representation of the config to be loaded
     * @param factory Implements hooks for optional interface elements
     */
    public void initComponents(ConfigRepresentation rep, GuiItemFactory factory) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        this.rep = rep;
        this.factory = factory;
        synchronized(rep) {
            if (rep.getRoot() != null) {
                displayCdi();
            } else {
                displayLoadingProgress();
            }
        }
        // TODO: add update listener to display when load is complete.
    }

    /**
     * @param rep Representation of the config to be loaded
     */
    public void initComponents(ConfigRepresentation rep) {
        initComponents(rep, new GuiItemFactory()); // default with no behavior
    }
    
    GuiItemFactory factory;
    JPanel loadingPanel;
    JLabel loadingText;
    PropertyChangeListener loadingListener;

    private void removeLoadingListener() {
        synchronized (rep) {
            if (loadingListener != null) rep.removePropertyChangeListener(loadingListener);
            loadingListener = null;
        }
    }

    private void addLoadingListener() {
        synchronized(rep) {
            loadingListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    if (event.getPropertyName().equals(UPDATE_REP)) {
                        displayCdi();
                    } else if (event.getPropertyName().equals(UPDATE_STATE)) {
                        loadingText.setText(rep.getStatus());
                        Window win = SwingUtilities.getWindowAncestor(CdiPanel.this);
                        if (win != null) win.pack();
                    }
                }
            };
            rep.addPropertyChangeListener(loadingListener);
        }
    }

    private void hideLoadingProgress() {
        if (loadingPanel == null) return;
        removeLoadingListener();
        loadingPanel.setVisible(false);
    }

    private void displayLoadingProgress() {
        if (loadingPanel == null) createLoadingPane();
        add(loadingPanel);
        addLoadingListener();
    }

    private void displayCdi() {
        hideLoadingProgress();
        if (rep.getCdiRep().getIdentification() != null) {
            add(createIdentificationPane(rep.getCdiRep()));
        }
        rep.visit(new RendererVisitor());
        // add glue at bottom
        add(Box.createVerticalGlue());
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win != null) win.pack();
    }

    private class RendererVisitor extends ConfigRepresentation.Visitor {
        private JPanel currentPane;
        private JPanel currentLeaf;
        private JTabbedPane currentTabbedPane;
        @Override
        public void visitSegment(ConfigRepresentation.SegmentEntry e) {
            currentPane = createSegmentPane(e);
            super.visitSegment(e);

            String name = "Segment" + (e.getName() != null ? (": " + e.getName()) : "");
            JPanel ret = new util.CollapsiblePanel(name, currentPane);
            // ret.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED)); //debugging
            ret.setAlignmentY(Component.TOP_ALIGNMENT);
            ret.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(ret);
        }

        @Override
        public void visitGroup(ConfigRepresentation.GroupEntry e) {
            // stack these variables
            JPanel oldPane = currentPane;
            JTabbedPane oldTabbed = currentTabbedPane;

            GroupPane groupPane = new GroupPane(e);
            currentPane = groupPane;
            if (e.group.getReplication() > 1) {
                currentTabbedPane = new JTabbedPane();
                currentTabbedPane.setAlignmentX(Component.LEFT_ALIGNMENT);
                currentPane.add(currentTabbedPane);
            }

            factory.handleGroupPaneStart(groupPane);
            super.visitGroup(e);
            factory.handleGroupPaneEnd(groupPane);

            oldPane.add(groupPane);

            // restore stack
            currentPane = oldPane;
            currentTabbedPane = oldTabbed;
        }

        @Override
        public void visitGroupRep(ConfigRepresentation.GroupRep e) {
            currentPane = new JPanel();
            currentPane.setLayout(new BoxLayout(currentPane, BoxLayout.Y_AXIS));
            currentPane.setAlignmentX(Component.LEFT_ALIGNMENT);
            CdiRep.Group item = e.group;
            String name = (item.getRepName() != null ? (item.getRepName()) : "Group")+" "+(e.index);
            //currentPane.setBorder(BorderFactory.createTitledBorder(name));
            currentPane.setName(name);

            factory.handleGroupPaneStart(currentPane);
            super.visitGroupRep(e);
            factory.handleGroupPaneEnd(currentPane);

            currentTabbedPane.add(currentPane);
        }

        @Override
        public void visitString(ConfigRepresentation.StringEntry e) {
            currentLeaf = new StringPane(e);
            super.visitString(e);
        }

        @Override
        public void visitInt(ConfigRepresentation.IntegerEntry e) {
            currentLeaf = new IntPane(e);
            super.visitInt(e);
        }

        @Override
        public void visitEvent(ConfigRepresentation.EventEntry e) {
            currentLeaf = new EventIdPane(e);
            super.visitEvent(e);
        }

        @Override
        public void visitLeaf(ConfigRepresentation.CdiEntry e) {
            currentLeaf.setAlignmentX(Component.LEFT_ALIGNMENT);
            currentPane.add(currentLeaf);
            currentLeaf = null;
        }


    }


    void createLoadingPane() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setAlignmentY(Component.TOP_ALIGNMENT);
        p.setBorder(BorderFactory.createTitledBorder("Loading"));
        loadingText = new JLabel(rep.getStatus());
        loadingText.setPreferredSize(new Dimension(200, 20));
        loadingText.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(loadingText);
        loadingPanel = p;
    }

    JPanel createIdentificationPane(CdiRep c) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setAlignmentY(Component.TOP_ALIGNMENT);
        //p.setBorder(BorderFactory.createTitledBorder("Identification"));

        CdiRep.Identification id = c.getIdentification();
        
        JPanel p1 = new JPanel();
        p.add(p1);
        p1.setLayout(new util.javaworld.GridLayout2(4,2));
        p1.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        p1.add(new JLabel("Manufacturer: "));
        p1.add(new JLabel(id.getManufacturer()));
        
        p1.add(new JLabel("Model: "));
        p1.add(new JLabel(id.getModel()));
        
        p1.add(new JLabel("Hardware Version: "));
        p1.add(new JLabel(id.getHardwareVersion()));
        
        p1.add(new JLabel("Software Version: "));
        p1.add(new JLabel(id.getSoftwareVersion()));
        
        p1.setMaximumSize(p1.getPreferredSize());
        
        // include map if present
        JPanel p2 = createPropertyPane(id.getMap());
        if (p2!=null) {
            p2.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(p2);
        }
        
        JPanel ret = new util.CollapsiblePanel("Identification", p);
        ret.setAlignmentY(Component.TOP_ALIGNMENT);
        ret.setAlignmentX(Component.LEFT_ALIGNMENT);
        return ret;
    }

    /**
     * Creates UI for a properties Map (for segments and groups).
     * @param map the properties to display
     * @return panel with UI
     */
    JPanel createPropertyPane(CdiRep.Map map) {
        if (map != null) {
            JPanel p2 = new JPanel();
            p2.setAlignmentX(Component.LEFT_ALIGNMENT);
            p2.setBorder(BorderFactory.createTitledBorder("Properties"));
            
            java.util.List keys = map.getKeys();
            if (keys.isEmpty()) return null;

            p2.setLayout(new util.javaworld.GridLayout2(keys.size(),2));

            for (int i = 0; i<keys.size(); i++) {
                String key = (String)keys.get(i);

                p2.add(new JLabel(key+": "));
                p2.add(new JLabel(map.getEntry(key)));
                
            }
            p2.setMaximumSize(p2.getPreferredSize());
            return p2;
        } else 
            return null;
    }

    JPanel createSegmentPane(ConfigRepresentation.SegmentEntry item) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setAlignmentY(Component.TOP_ALIGNMENT);
        //p.setBorder(BorderFactory.createTitledBorder(name));

        String d = item.getDescription();
        if (d != null) p.add(createDescriptionPane(d));

        // include map if present
        JPanel p2 = createPropertyPane(item.getMap());
        if (p2 != null) p.add(p2);
        return p;
    }

    JPanel createDescriptionPane(String d) {
        if (d == null) return null;
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextArea area = new JTextArea(d);
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        area.setFont(UIManager.getFont("Label.font"));
        area.setEditable(false);
        area.setOpaque(false);
        area.setWrapStyleWord(true); 
        area.setLineWrap(true);
        p.add(area);
        return p;
    }

    public class GroupPane extends JPanel {
        private final ConfigRepresentation.GroupEntry entry;
        private final CdiRep.Item item;
        GroupPane(ConfigRepresentation.GroupEntry e) {
            entry = e;
            item = e.getCdiItem();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            String name = (item.getName() != null ? (item.getName()) : "Group");
            setBorder(BorderFactory.createTitledBorder(name));
            setName(name);

            String d = item.getDescription();
            if (d != null) {
                add(createDescriptionPane(d));
            }

            // include map if present
            JPanel p2 = createPropertyPane(item.getMap());
            if (p2 != null) {
                add(p2);
            }
        }
    }

    public class EventIdPane extends JPanel {
        private final ConfigRepresentation.EventEntry entry;
        private final CdiRep.Item item;
        JFormattedTextField textField;
        
        EventIdPane(ConfigRepresentation.EventEntry e) {
            entry = e;
            item = entry.getCdiItem();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            String name = (item.getName() != null ? item.getName() : "EventID");
            setBorder(BorderFactory.createTitledBorder(name));

            String d = item.getDescription();
            if (d != null) {
                add(createDescriptionPane(d));
            }

            JPanel p3 = new JPanel();
            p3.setAlignmentX(Component.LEFT_ALIGNMENT);
            p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
            add(p3);

            textField = factory.handleEventIdTextField(EventIdTextField.getEventIdTextField());
            textField.setMaximumSize(textField.getPreferredSize());
            p3.add(textField);

            entry.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    if (propertyChangeEvent.getPropertyName() == UPDATE_ENTRY_DATA) {
                        if (e.lastVisibleValue == null) {
                            textField.setText("");
                        } else {
                            textField.setText(e.lastVisibleValue);
                        }
                    }
                }
            });
            entry.fireUpdate();

            JButton b;
            b = factory.handleReadButton(new JButton("Refresh")); // was: read
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    entry.reload();
                }
            });
            p3.add(b);
            b = factory.handleWriteButton(new JButton("Write"));
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    byte[] contents = org.openlcb.Utilities.bytesFromHexString((String)textField.getValue());
                    entry.setValue(new EventID(contents));
                 }
            });
            p3.add(b);
            p3.add(Box.createHorizontalGlue());
        }
    }
    

    public class IntPane extends JPanel {
        JTextField textField = null;
        JComboBox box = null;
        CdiRep.Map map = null;
        private final ConfigRepresentation.IntegerEntry entry;
        private final CdiRep.Item item;


        IntPane(ConfigRepresentation.IntegerEntry e) {
            this.entry = e;
            this.item = entry.getCdiItem();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            String name = (item.getName() != null ? item.getName() : "Integer");
            setBorder(BorderFactory.createTitledBorder(name));

            String d = item.getDescription();
            if (d != null) {
                add(createDescriptionPane(d));
            }

            JPanel p3 = new JPanel();
            p3.setAlignmentX(Component.LEFT_ALIGNMENT);
            p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
            add(p3);

            // see if map is present
            String[] labels;
            map = item.getMap();
            if ((map != null) && (map.getKeys().size() > 0)) {
                // map present, make selection box
                box = new JComboBox(map.getValues().toArray(new String[]{""})) {
                    public java.awt.Dimension getMaximumSize() {
                        return getPreferredSize();
                    }
                };
                p3.add(box);
            } else {
                // map not present, just an entry box
                textField = new JTextField(24) {
                    public java.awt.Dimension getMaximumSize() {
                        return getPreferredSize();
                    }
                };
                p3.add(textField);
                textField.setToolTipText("Signed integer value of up to "+entry.size+" bytes");
            }

            entry.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    if (propertyChangeEvent.getPropertyName() == UPDATE_ENTRY_DATA) {
                        if (e.lastVisibleValue == null) return;
                        if (textField != null) {
                            textField.setText(entry.lastVisibleValue);
                        } else {
                            box.setSelectedItem(entry.lastVisibleValue);
                        }
                    }
                }
            });
            entry.fireUpdate();

            JButton b;
            b = factory.handleReadButton(new JButton("Refresh")); // was: read
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    entry.reload();
                }
            });
            p3.add(b);
            b = factory.handleWriteButton(new JButton("Write"));
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    long value;
                    if (textField != null) {
                        value = Long.parseLong(textField.getText());
                    } else {
                        // have to get key from stored value
                        String entry = (String) box.getSelectedItem();
                        String key = map.getKey(entry);
                        value = Long.parseLong(key);
                    }
                    entry.setValue(value);
                }
            });
            p3.add(b);
            p3.add(Box.createHorizontalGlue());
        }
    }

    public class StringPane extends JPanel {
        JTextField textField;
        private final ConfigRepresentation.StringEntry entry;
        private final CdiRep.Item item;

        StringPane(ConfigRepresentation.StringEntry e) {
            this.entry = e;
            this.item = entry.getCdiItem();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            String name = (item.getName()!=null? item.getName() : "String");
            setBorder(BorderFactory.createTitledBorder(name));
        
            String d = item.getDescription();
            if (d!=null) add(createDescriptionPane(d));

            JPanel p3 = new JPanel();
            p3.setAlignmentX(Component.LEFT_ALIGNMENT);
            p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
            add(p3);

            textField = new JTextField(entry.size) {
                public java.awt.Dimension getMaximumSize() {
                    return getPreferredSize();
                }
            };
            textField = factory.handleStringValue(textField);
            
            p3.add(textField);
            textField.setToolTipText("String of up to "+entry.size+" characters");

            entry.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    if (propertyChangeEvent.getPropertyName() == UPDATE_ENTRY_DATA) {
                        if (e.lastVisibleValue == null) {
                            textField.setText("");
                        } else {
                            textField.setText(e.lastVisibleValue);
                        }
                    }
                }
            });
            entry.fireUpdate();

            JButton b;
            b = factory.handleReadButton(new JButton("Refresh")); // was: read
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    entry.reload();
                }
            });
            p3.add(b);
            b = factory.handleWriteButton(new JButton("Write"));
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    entry.setValue(textField.getText());
                }
            });
            p3.add(b);
            p3.add(Box.createHorizontalGlue());
        }
     }

     /** 
      * Provide access to e.g. a MemoryConfig service.
      * 
      * Default just writes output for debug
      */
    public static class ReadWriteAccess {
        public void doWrite(long address, int space, byte[] data, final
                            MemoryConfigurationService.McsWriteHandler handler) {
            System.out.println("Write to "+address+" in space "+space);
        }
        public void doRead(long address, int space, int length, final MemoryConfigurationService
                .McsReadHandler handler) {
            System.out.println("Read from "+address+" in space "+space);
        }
    }
     
    /** 
     * Handle GUI hook requests if needed
     * 
     * Default behavior is to do nothing
     */
    public static class GuiItemFactory {
        public JButton handleReadButton(JButton button) {
            return button;
        }
        public JButton handleWriteButton(JButton button) {
            return button;
        }
        public void handleGroupPaneStart(JPanel pane) {
            return;
        }
        public void handleGroupPaneEnd(JPanel pane) {
            return;
        }
        public JFormattedTextField handleEventIdTextField(JFormattedTextField field) {
            return field;
        }
        public JTextField handleStringValue(JTextField value) {
            return value;
        }

    }
}
