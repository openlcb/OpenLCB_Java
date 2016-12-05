package org.openlcb.cdi.swing;

import org.openlcb.EventID;
import org.openlcb.cdi.CdiRep;
import org.openlcb.cdi.impl.ConfigRepresentation;
import org.openlcb.implementations.MemoryConfigurationService;
import org.openlcb.swing.EventIdTextField;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import static org.openlcb.cdi.impl.ConfigRepresentation.UPDATE_ENTRY_DATA;
import static org.openlcb.cdi.impl.ConfigRepresentation.UPDATE_REP;
import static org.openlcb.cdi.impl.ConfigRepresentation.UPDATE_STATE;
import static org.openlcb.cdi.impl.ConfigRepresentation.UPDATE_WRITE_COMPLETE;

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
    private static final String TAG = "CdiPanel";
    private static final Logger log = Logger.getLogger(TAG);

    private static final Color COLOR_EDITED = new Color(0xffa500); // orange
    private static final Color COLOR_UNFILLED = new Color(0xffff00); // yellow
    private static final Color COLOR_WRITTEN = new Color(0xffffff); // white
    private static final Color COLOR_ERROR = new Color(0xff0000); // red


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
    private JButton reloadButton;

    boolean loadingIsPacked = false;

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
                        if (!loadingIsPacked && win != null) {
                            win.pack();
                            loadingIsPacked = true;
                        }
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
        displayLoadingProgress();
        loadingText.setText("Creating display...");
        if (rep.getCdiRep().getIdentification() != null) {
            add(createIdentificationPane(rep.getCdiRep()));
        }
        repack();
        new Thread(new Runnable() {
            @Override
            public void run() {
                rep.visit(new RendererVisitor());
                EventQueue.invokeLater(() -> displayComplete());
            }
        }).start();
    }

    private void displayComplete() {
        hideLoadingProgress();
        // add glue at bottom
        add(Box.createVerticalGlue());
        repack();
    }

    private void repack() {
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win != null) win.pack();
    }

    /**
     * This class descends into a CDI group (usually a group repeat) and tries to find a string
     * field. If a string field is found, and only one such, then foundUnique will be set to true
     * and foundEntry will be the field's representation.
     * <p>
     * The iteration does not look inside repeated groups (since anything there would never be
     * unique).
     */
    private class FindDescriptorVisitor extends ConfigRepresentation.Visitor {
        public boolean foundUnique = false;
        public ConfigRepresentation.StringEntry foundEntry = null;

        @Override
        public void visitString(ConfigRepresentation.StringEntry e) {
            if (foundUnique) {
                foundUnique = false;
            } else {
                foundUnique = true;
                foundEntry = e;
            }
        }

        @Override
        public void visitGroupRep(ConfigRepresentation.GroupRep e) {
            // Stops descending into repeated subgroups.
            return;
        }
    }

    /**
     * This class renders the user interface for a config. All configuration components are
     * handled here.
     */
    private class RendererVisitor extends ConfigRepresentation.Visitor {
        private JPanel currentPane;
        private JPanel currentLeaf;
        private JTabbedPane currentTabbedPane;
        @Override
        public void visitSegment(ConfigRepresentation.SegmentEntry e) {
            currentPane = new SegmentPane(e);
            super.visitSegment(e);

            String name = "Segment" + (e.getName() != null ? (": " + e.getName()) : "");
            JPanel ret = new util.CollapsiblePanel(name, currentPane);
            // ret.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED)); //debugging
            ret.setAlignmentY(Component.TOP_ALIGNMENT);
            ret.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(ret);
            EventQueue.invokeLater(() -> repack());
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

            if (oldPane instanceof SegmentPane) {
                // we make toplevel groups collapsible.
                groupPane.setBorder(null);
                JPanel ret = new util.CollapsiblePanel(groupPane.getName(), groupPane);
                // ret.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED)); //debugging
                ret.setAlignmentY(Component.TOP_ALIGNMENT);
                ret.setAlignmentX(Component.LEFT_ALIGNMENT);
                oldPane.add(ret);
            } else {
                oldPane.add(groupPane);
            }

            // restore stack
            currentPane = oldPane;
            currentTabbedPane = oldTabbed;
        }

        @Override
        public void visitGroupRep(final ConfigRepresentation.GroupRep e) {
            currentPane = new JPanel();
            currentPane.setLayout(new BoxLayout(currentPane, BoxLayout.Y_AXIS));
            currentPane.setAlignmentX(Component.LEFT_ALIGNMENT);
            CdiRep.Group item = e.group;
            final String name = (item.getRepName() != null ? (item.getRepName()) : "Group") + " "
                    + (e.index);
            //currentPane.setBorder(BorderFactory.createTitledBorder(name));
            currentPane.setName(name);

            // Finds a string field that could be used as a caption.
            FindDescriptorVisitor vv = new FindDescriptorVisitor();
            vv.visitContainer(e);

            if (vv.foundUnique) {
                final JPanel tabPanel = currentPane;
                final ConfigRepresentation.StringEntry source = vv.foundEntry;
                final JTabbedPane parentTabs = currentTabbedPane;
                // Creates a binder for listening to the name field changes.
                source.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent event) {
                        if (event.getPropertyName().equals(UPDATE_ENTRY_DATA)) {
                            if (source.lastVisibleValue != null && !source.lastVisibleValue
                                    .isEmpty()) {
                                String newName = (name + " (" + source.lastVisibleValue + ")");
                                tabPanel.setName(newName);
                                if (parentTabs.getTabCount() >= e.index) {
                                    parentTabs.setTitleAt(e.index - 1, newName);
                                }
                            } else {
                                if (parentTabs.getTabCount() >= e.index) {
                                    parentTabs.setTitleAt(e.index - 1, name);
                                }
                            }
                        }
                    }
                });
            }

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
        loadingText.setPreferredSize(new Dimension(400, 20));
        loadingText.setMinimumSize(new Dimension(400, 20));
        loadingText.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(loadingText);
        reloadButton = new JButton("Re-try");
        reloadButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                rep.restartIfNeeded();
            }
        });
        reloadButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        reloadButton.setAlignmentY(Component.TOP_ALIGNMENT);
        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(reloadButton);
        p.add(p1);
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

    public class SegmentPane extends JPanel {
        SegmentPane(ConfigRepresentation.SegmentEntry item) {
            JPanel p = this;
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.setAlignmentY(Component.TOP_ALIGNMENT);
            //p.setBorder(BorderFactory.createTitledBorder(name));

            String d = item.getDescription();
            if (d != null) p.add(createDescriptionPane(d));

            // include map if present
            JPanel p2 = createPropertyPane(item.getMap());
            if (p2 != null) p.add(p2);
        }
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

    public abstract class EntryPane extends JPanel {
        protected final CdiRep.Item item;
        protected JComponent textComponent;
        private ConfigRepresentation.CdiEntry entry;
        JPanel p3;

        EntryPane(ConfigRepresentation.CdiEntry e, String defaultName) {
            item = e.getCdiItem();
            entry = e;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            String name = (item.getName() != null ? item.getName() : defaultName);
            setBorder(BorderFactory.createTitledBorder(name));

            String d = item.getDescription();
            if (d != null) {
                add(createDescriptionPane(d));
            }

            p3 = new JPanel();
            p3.setAlignmentX(Component.LEFT_ALIGNMENT);
            p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
            add(p3);
        }

        protected void init() {
            p3.add(textComponent);
            textComponent.setMaximumSize(textComponent.getPreferredSize());
            if (textComponent instanceof JTextComponent) {
                ((JTextComponent) textComponent).getDocument().addDocumentListener(
                        new DocumentListener() {
                            @Override
                            public void insertUpdate(DocumentEvent documentEvent) {
                                drawRed();
                            }

                            @Override
                            public void removeUpdate(DocumentEvent documentEvent) {
                                drawRed();
                            }

                            @Override
                            public void changedUpdate(DocumentEvent documentEvent) {
                                drawRed();
                            }

                            private void drawRed() {
                                updateColor();
                            }
                        }
                );
            } else if (textComponent instanceof JComboBox) {
                ((JComboBox) textComponent).addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        updateColor();
                    }
                });
            }
            entry.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    if (propertyChangeEvent.getPropertyName().equals(UPDATE_ENTRY_DATA)) {
                        String v = entry.lastVisibleValue;
                        if (v == null) v = "";
                        updateDisplayText(v);
                        updateColor();
                    } else if (propertyChangeEvent.getPropertyName().equals
                            (UPDATE_WRITE_COMPLETE)) {
                        updateColor();
                        //textComponent.setBackground(COLOR_WRITTEN);
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
                    writeDisplayTextToNode();
                }
            });
            p3.add(b);
            p3.add(Box.createHorizontalGlue());
        }

        void updateColor() {
            if (entry.lastVisibleValue == null) {
                textComponent.setBackground(COLOR_UNFILLED);
                return;
            }
            String v = getDisplayText();
            if (v.equals(entry.lastVisibleValue)) {
                textComponent.setBackground(COLOR_WRITTEN);
            } else {
                textComponent.setBackground(COLOR_EDITED);
            }
        }

        // Take the value from the text box and write it to the Cdi entry.
        protected abstract void writeDisplayTextToNode();

        // Take the latest entry (or "") from the Cdi entry and write it to the text box.
        protected abstract void updateDisplayText(@Nonnull String value);

        // returns the currently displayed value ("" if none).
        protected abstract
        @Nonnull
        String getDisplayText();
    }

    public class EventIdPane extends EntryPane {
        private final ConfigRepresentation.EventEntry entry;
        JFormattedTextField textField;

        EventIdPane(ConfigRepresentation.EventEntry e) {
            super(e, "EventID");
            entry = e;

            textField = factory.handleEventIdTextField(EventIdTextField.getEventIdTextField());
            textComponent = textField;

            init();
        }


        @Override
        protected void writeDisplayTextToNode() {
            byte[] contents = org.openlcb.Utilities.bytesFromHexString((String) textField
                    .getText());
            entry.setValue(new EventID(contents));
        }

        @Override
        protected void updateDisplayText(@Nonnull String value) {
            textField.setText(value);
        }

        @Nonnull
        @Override
        protected String getDisplayText() {
            String s = textField.getText();
            return s == null ? "" : s;
        }
    }


    public class IntPane extends EntryPane {
        JTextField textField = null;
        JComboBox box = null;
        CdiRep.Map map = null;
        private final ConfigRepresentation.IntegerEntry entry;


        IntPane(ConfigRepresentation.IntegerEntry e) {
            super(e, "Integer");
            this.entry = e;

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
                textComponent = box;
            } else {
                // map not present, just an entry box
                textField = new JTextField(24) {
                    public java.awt.Dimension getMaximumSize() {
                        return getPreferredSize();
                    }
                };
                textComponent = textField;
                textField.setToolTipText("Signed integer value of up to "+entry.size+" bytes");
            }

            init();
        }

        @Override
        protected void writeDisplayTextToNode() {
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

        @Override
        protected void updateDisplayText(@Nonnull String value) {
            if (textField != null) textField.setText(value);
            if (box != null) box.setSelectedItem(value);
        }

        @Nonnull
        @Override
        protected String getDisplayText() {
            String s = (box == null) ? (String) textField.getText()
                    : (String) box.getSelectedItem();
            return s == null ? "" : s;
        }
    }

    public class StringPane extends EntryPane {
        JTextField textField;
        private final ConfigRepresentation.StringEntry entry;

        StringPane(ConfigRepresentation.StringEntry e) {
            super(e, "String");
            this.entry = e;

            textField = new JTextField(entry.size) {
                public java.awt.Dimension getMaximumSize() {
                    return getPreferredSize();
                }
            };
            textField = factory.handleStringValue(textField);
            textComponent = textField;
            textField.setToolTipText("String of up to "+entry.size+" characters");

            init();
        }

        @Override
        protected void writeDisplayTextToNode() {
            entry.setValue(textField.getText());
        }

        @Override
        protected void updateDisplayText(@Nonnull String value) {
            textField.setText(value);
        }

        @Nonnull
        @Override
        protected String getDisplayText() {
            String s = textField.getText();
            return s == null ? "" : s;
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
