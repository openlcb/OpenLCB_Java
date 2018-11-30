package org.openlcb.cdi.swing;

import org.openlcb.EventID;
import org.openlcb.cdi.CdiRep;
import org.openlcb.cdi.cmd.BackupConfig;
import org.openlcb.cdi.cmd.RestoreConfig;
import org.openlcb.cdi.impl.ConfigRepresentation;
import org.openlcb.implementations.EventTable;
import org.openlcb.implementations.MemoryConfigurationService;
import org.openlcb.swing.EventIdTextField;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

import util.CollapsiblePanel;

import static org.openlcb.cdi.impl.ConfigRepresentation.UPDATE_ENTRY_DATA;
import static org.openlcb.cdi.impl.ConfigRepresentation.UPDATE_REP;
import static org.openlcb.cdi.impl.ConfigRepresentation.UPDATE_STATE;
import static org.openlcb.cdi.impl.ConfigRepresentation.UPDATE_WRITE_COMPLETE;
import static org.openlcb.implementations.BitProducerConsumer.nullEvent;

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
    private static final Logger logger = Logger.getLogger(CdiPanel.class.getName());

    private static final Color COLOR_EDITED = new Color(0xffa500); // orange
    private static final Color COLOR_UNFILLED = new Color(0xffff00); // yellow
    private static final Color COLOR_WRITTEN = new Color(0xffffff); // white
    private static final Color COLOR_ERROR = new Color(0xff0000); // red
    private static final Pattern segmentPrefixRe = Pattern.compile("^seg[0-9]*[.]");
    private static final Pattern entrySuffixRe = Pattern.compile("[.]child[0-9]*$");
    private static final Color COLOR_COPIED = new Color(0xffa500); // orange

    /**
     * We always use the same file chooser in this class, so that the user's
     * last-accessed directory remains available.
     */
    static JFileChooser fci = new JFileChooser();

    private ConfigRepresentation rep;
    private EventTable eventTable = null;
    private String nodeName = "";

    public CdiPanel () {
        super();
        tabColorTimer = new Timer("OpenLCB CDI Reader Tab Color Timer");
    }

    /**
     * Cleans up all property change listeners etc in preparation when closing the window.
     */
    public void release() {
        logger.log(Level.FINE, "Cleanup of CDI window for {0}",nodeName);
        for (Runnable task : cleanupTasks) {
            task.run();
        }
        cleanupTasks.clear();
        tabColorTimer.cancel(); 
    }

    /**
     * Call this function before initComponents in order to use an event table, both for read and
     * write purposes in the UI.
     * @param t the global event table, coming from the OlcbInterface.
     * @param nodeName is the textual user name of the current node, as represented by SNIP.
     */
    public void setEventTable(String nodeName, EventTable t) {
        eventTable = t;
        this.nodeName = nodeName;
    }

    /**
     * @param rep Representation of the config to be loaded
     * @param factory Implements hooks for optional interface elements
     */
    public void initComponents(ConfigRepresentation rep, GuiItemFactory factory) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        this.rep = rep;
        this.factory = factory;

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);


        scrollPane = new JScrollPane(contentPanel);
        Dimension minScrollerDim = new Dimension(800, 12);
        scrollPane.setMinimumSize(minScrollerDim);
        scrollPane.getVerticalScrollBar().setUnitIncrement(30);

        add(scrollPane);
        //add(contentPanel);

        buttonBar = new JPanel();
        //buttonBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonBar.setLayout(new FlowLayout());
        JButton bb = new JButton("Refresh All");
        bb.setToolTipText("Discards all changes and loads the freshest value from the hardware for all entries.");
        bb.addActionListener(actionEvent -> reloadAll());
        buttonBar.add(bb);

        bb = new JButton("Save changed");
        bb.setToolTipText("Writes every changed value to the hardware.");
        bb.addActionListener(actionEvent -> saveChanged());
        buttonBar.add(bb);

        bb = new JButton("Backup...");
        bb.setToolTipText("Creates a file on your computer with all saved settings from this node. Use the \"Save changed\" button first.");
        bb.addActionListener(actionEvent -> runBackup());
        buttonBar.add(bb);

        bb = new JButton("Restore...");
        bb.setToolTipText("Loads a file with backed-up settings. Does not change the hardware settings, so use \"Save changed\" afterwards.");
        bb.addActionListener(actionEvent -> runRestore());
        buttonBar.add(bb);

        if (rep.getConnection() != null && rep.getRemoteNodeID() != null) {
            bb = new JButton("Reboot");
            bb.setToolTipText("Requests the configured node to restart.");
            bb.addActionListener(actionEvent -> runReboot());
            addButtonToMoreFunctions(bb);
            bb = new JButton("Update Complete");
            bb.setToolTipText("Tells the configured node that the you are done with changing the settings and they should be taking effect now. Might restart the node.");
            bb.addActionListener(actionEvent -> runUpdateComplete());
            addButtonToMoreFunctions(bb);
        }

        createSensorCreateHelper();

        add(buttonBar);

        synchronized(rep) {
            if (rep.getRoot() != null) {
                displayCdi();
            } else {
                displayLoadingProgress();
            }
        }
    }

    private void createSensorCreateHelper() {
        JPanel createHelper = new JPanel();
        factory.handleGroupPaneStart(createHelper);
        createHelper.setAlignmentX(Component.LEFT_ALIGNMENT);
        createHelper.setLayout(new BoxLayout(createHelper, BoxLayout.Y_AXIS));
        JPanel lineHelper = new JPanel();
        lineHelper.setAlignmentX(Component.LEFT_ALIGNMENT);
        lineHelper.setLayout(new BoxLayout(lineHelper, BoxLayout.X_AXIS));
        lineHelper.setBorder(BorderFactory.createTitledBorder("User name"));
        JTextField textField = new JTextField(32) {
            public java.awt.Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        factory.handleStringValue(textField);
        lineHelper.add(textField);
        lineHelper.add(Box.createHorizontalGlue());
        createHelper.add(lineHelper);

        lineHelper = new JPanel();
        lineHelper.setAlignmentX(Component.LEFT_ALIGNMENT);
        lineHelper.setLayout(new BoxLayout(lineHelper, BoxLayout.X_AXIS));
        lineHelper.setBorder(BorderFactory.createTitledBorder("Event Id for Active / Thrown"));
        JFormattedTextField activeTextField = factory.handleEventIdTextField(EventIdTextField
                .getEventIdTextField());
        activeTextField.setMaximumSize(activeTextField.getPreferredSize());
        lineHelper.add(activeTextField);
        addCopyPasteButtons(lineHelper, activeTextField);
        lineHelper.add(Box.createHorizontalGlue());
        createHelper.add(lineHelper);

        lineHelper = new JPanel();
        lineHelper.setAlignmentX(Component.LEFT_ALIGNMENT);
        lineHelper.setLayout(new BoxLayout(lineHelper, BoxLayout.X_AXIS));
        lineHelper.setBorder(BorderFactory.createTitledBorder("Event Id for Inactive / Closed"));
        JFormattedTextField inactiveTextField = factory.handleEventIdTextField(EventIdTextField
                .getEventIdTextField());
        inactiveTextField.setMaximumSize(inactiveTextField.getPreferredSize());
        lineHelper.add(inactiveTextField);
        addCopyPasteButtons(lineHelper, inactiveTextField);
        lineHelper.add(Box.createHorizontalGlue());
        createHelper.add(lineHelper);

        factory.handleGroupPaneEnd(createHelper);
        CollapsiblePanel cp = new CollapsiblePanel("Sensor/Turnout creation", createHelper);
        cp.setExpanded(false);
        cp.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        //cp.setMinimumSize(new Dimension(0, cp.getPreferredSize().height));
        add(cp);
    }

    /**
     * @param rep Representation of the config to be loaded
     */
    public void initComponents(ConfigRepresentation rep) {
        initComponents(rep, new GuiItemFactory()); // default with no behavior
    }

    /** Adds a button to the bar visible on the bottom line, below the scrollbar.
     * @param c component to add (typically a button)
     */
    public void addButtonToFooter(JComponent c) {
        if (c instanceof JButton) {
            addButtonToMoreFunctions((JButton)c);
        } else {
            buttonBar.add(c);
        }
    }

    private void addButtonToMoreFunctions(final JButton b) {
        if (moreButton == null) {
            moreButton = new JButton("More...");
            moreButton.setToolTipText("Shows additional operations you can do here.");
            moreButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    showMoreFunctionsMenu();
                }
            });
            buttonBar.add(moreButton);
        }
        Action a = new AbstractAction(b.getText()) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                b.doClick();
            }
        };
        moreMenu.add(a);
    }

    private void showMoreFunctionsMenu() {
        moreMenu.show(moreButton, 0, moreButton.getHeight());
    }

    /**
     * Refreshes all memory variable entries directly from the hardware node.
     */
    public void reloadAll() {
        rep.reloadAll();
    }

    public void saveChanged() {
        for (EntryPane entry : allEntries) {
            if (entry.isDirty()) {
                entry.writeDisplayTextToNode();
            }
        }
    }

    public void runBackup() {
        // First select a file to save to.
        fci.setDialogTitle("Save configuration backup file");
        fci.rescanCurrentDirectory();
        fci.setSelectedFile(new File("config." + rep.getRemoteNodeAsString() + ".txt"));
        int retVal = fci.showSaveDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION || fci.getSelectedFile() == null) {
            return;
        }
        if (fci.getSelectedFile().exists()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Do you want to overwrite the existing file?",
                    "File already exists", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION)
                return;
        }

        try {
            BackupConfig.writeConfigToFile(fci.getSelectedFile().getPath(), rep);
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("Failed to write variables to file " + fci.getSelectedFile().getPath() + ": " + e.toString());
        }
    }

    public void runRestore() {
        // First select a file to save to.
        fci.setDialogTitle("Open configuration restore file");
        fci.rescanCurrentDirectory();
        fci.setSelectedFile(new File("config." + rep.getRemoteNodeAsString() + ".txt"));
        int retVal = fci.showOpenDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        RestoreConfig.parseConfigFromFile(fci.getSelectedFile().getPath(), new RestoreConfig.ConfigCallback() {
            boolean hasError = false;

            @Override
            public void onConfigEntry(String key, String value) {
                EntryPane pp = entriesByKey.get(key);
                if (pp == null) {
                    onError("Could not find variable for key " + key);
                    return;
                }
                // TODO: The logical value to display value change should not be the
                // responsibility of this code; there is duplication over the
                // ConfigRepresentation.IntegerEntry class. This
                // should probably go via someplace else.
                CdiRep.Map map = pp.entry.getCdiItem().getMap();
                if (map != null && map.getKeys().size() > 0) {
                    String mapvalue = map.getEntry(value);
                    if (mapvalue != null) value = mapvalue;
                }
                pp.updateDisplayText(value);
                pp.updateColor();
            }

            @Override
            public void onError(String error) {
                if (!hasError) {
                    logger.severe("Error(s) encountered during loading configuration backup.");
                    hasError = true;
                }
                logger.severe(error);
            }
        });
        logger.info("Config load done.");
    }

    private void runReboot() {
        rep.getConnection().getDatagramService().sendData(rep.getRemoteNodeID(), new int[] {0x20, 0xA9});
    }

    private void runUpdateComplete() {
        rep.getConnection().getDatagramService().sendData(rep.getRemoteNodeID(), new int[] {0x20, 0xA8});
    }

    GuiItemFactory factory;
    JPanel loadingPanel;
    JLabel loadingText;
    PropertyChangeListener loadingListener;
    private JButton reloadButton;
    private final List<EntryPane> allEntries = new ArrayList<>();
    private final Map<String, EntryPane> entriesByKey = new HashMap<>();
    private final Map<String, JTabbedPane> tabsByKey = new HashMap<>();
    // These need to be executed when closing the window.
    private final ArrayList<Runnable> cleanupTasks = new ArrayList<>();
    // Deferred callbacks that came during the rendering of the window.
    private final ArrayList<Runnable> startupTasks = new ArrayList<>();
    private boolean renderingInProgress = true;

    boolean loadingIsPacked = false;
    JScrollPane scrollPane;
    JPanel contentPanel;
    JPanel buttonBar;
    JPopupMenu moreMenu = new JPopupMenu();
    JButton moreButton;
    SearchPane searchPane = new SearchPane();

    private Timer tabColorTimer;
    long lastColorRefreshNeeded = 0; // guarded by tabColorTimer
    long lastColorRefreshDone = Long.MAX_VALUE; // guarded by tabColorTimer

    private void notifyTabColorRefresh() {
        long currentTick;
        synchronized (tabColorTimer) {
            currentTick = ++lastColorRefreshNeeded;
        }
        final long actualRequest = currentTick;
        tabColorTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                EventQueue.invokeLater(() -> performTabColorRefresh(actualRequest));
            }
        }, 500);
    }

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
        contentPanel.add(loadingPanel);
        addLoadingListener();
    }

    private void displayCdi() {
        displayLoadingProgress();
        loadingText.setText("Creating display...");
        if (rep.getCdiRep().getIdentification() != null) {
            contentPanel.add(createIdentificationPane(rep.getCdiRep()));
        }
        repack();
        new Thread(new Runnable() {
            @Override
            public void run() {
                rep.visit(new RendererVisitor());
                EventQueue.invokeLater(() -> displayComplete());
            }
        }, "openlcb-cdi-render").start();
    }

    private void displayComplete() {
        synchronized (startupTasks) {
            renderingInProgress = false;
        }
        startupTasks.forEach(r -> r.run());

        hideLoadingProgress();
        // add glue at bottom
        contentPanel.add(Box.createVerticalGlue());
        repack();
        synchronized (tabColorTimer) {
            lastColorRefreshDone = 0;
        }
        notifyTabColorRefresh();
        SwingUtilities.invokeLater(() -> {
            Window win = SwingUtilities.getWindowAncestor(this);
            if (win == null) {
                logger.log(Level.FINE, "Could not add close window listener");
                return;
            }
            win.addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent windowEvent) {
                }

                @Override
                public void windowClosing(WindowEvent windowEvent) {
                    release();
                    runUpdateComplete();
                }

                @Override
                public void windowClosed(WindowEvent windowEvent) {
                }

                @Override
                public void windowIconified(WindowEvent windowEvent) {

                }

                @Override
                public void windowDeiconified(WindowEvent windowEvent) {

                }

                @Override
                public void windowActivated(WindowEvent windowEvent) {

                }

                @Override
                public void windowDeactivated(WindowEvent windowEvent) {

                }
            });
        });
    }

    private void repack() {
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win != null) win.pack();
    }

    private void performTabColorRefresh(long requestTick) {
        synchronized (tabColorTimer) {
            if (lastColorRefreshDone >= requestTick) return; // nothing to do
            lastColorRefreshDone = lastColorRefreshNeeded;
        }
        rep.visit(new ConfigRepresentation.Visitor() {
            boolean isDirty = false;

            @Override
            public void visitGroupRep(ConfigRepresentation.GroupRep e) {
                boolean oldDirty = isDirty;
                isDirty = false;
                super.visitGroupRep(e);
                JTabbedPane tabs = tabsByKey.get(e.key);
                if (tabs != null && tabs.getTabCount() >= e.index) {
                    if (isDirty) {

                        tabs.setBackgroundAt(e.index - 1, COLOR_EDITED);
                    } else {
                        tabs.setBackgroundAt(e.index - 1, null);
                    }
                }
                isDirty |= oldDirty;
            }

            @Override
            public void visitLeaf(ConfigRepresentation.CdiEntry e) {
                EntryPane v = entriesByKey.get(e.key);
                isDirty |= v.isDirty();
            }
        });
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
     * This class descends into members of a group recursively and updates the visible names of
     * event fields for their EventTable registration.
     */
    private class UpdateGroupNameVisitor extends ConfigRepresentation.Visitor {
        String baseGroupKey;
        String labelValue;

        /**
         * Constructor.
         *
         * @param baseGroupKey the key of the GroupRep whose group name has changed.
         * @param labelValue   the new user name of the group repeat.
         */
        UpdateGroupNameVisitor(String baseGroupKey, String labelValue) {
            this.baseGroupKey = baseGroupKey;
            this.labelValue = labelValue;
        }

        @Override
        public void visitEvent(ConfigRepresentation.EventEntry e) {
            EventIdPane pane = (EventIdPane) entriesByKey.get(e.key);
            if (pane == null) return;
            pane.parentVisibleKeys.put(baseGroupKey, labelValue);
            pane.updateOwnEventName();
        }
    }

    /**
     * This class renders the user interface for a config. All configuration components are
     * handled here.
     */
    private class RendererVisitor extends ConfigRepresentation.Visitor {
        private JPanel currentPane;
        private EntryPane currentLeaf;
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
            contentPanel.add(ret);
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
                final PropertyChangeListener l = new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent event) {
                        if (event.getPropertyName().equals(UPDATE_ENTRY_DATA)) {
                            runNowOrLater(() -> {
                                String downstreamName = "";
                                if (source.lastVisibleValue != null && !source.lastVisibleValue
                                        .isEmpty()) {
                                    String newName = (name + " (" + source.lastVisibleValue + ")");
                                    tabPanel.setName(newName);
                                    if (parentTabs.getTabCount() >= e.index) {
                                        parentTabs.setTitleAt(e.index - 1, newName);
                                    }
                                    downstreamName = source.lastVisibleValue;
                                } else {
                                    if (parentTabs.getTabCount() >= e.index) {
                                        parentTabs.setTitleAt(e.index - 1, name);
                                    }
                                }
                                new UpdateGroupNameVisitor(e.key, downstreamName).visitContainer(e);
                            });
                        }
                    }
                };
                source.addPropertyChangeListener(l);
                cleanupTasks.add(() -> source.removePropertyChangeListener(l));
            }

            factory.handleGroupPaneStart(currentPane);
            super.visitGroupRep(e);
            factory.handleGroupPaneEnd(currentPane);
            currentPane.add(Box.createVerticalGlue());

            currentTabbedPane.add(currentPane);
            tabsByKey.put(e.key, currentTabbedPane);
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
            allEntries.add(currentLeaf);
            entriesByKey.put(currentLeaf.entry.key, currentLeaf);
            currentLeaf.setAlignmentX(Component.LEFT_ALIGNMENT);
            currentPane.add(currentLeaf);
            currentLeaf = null;
        }
    }

    /**
     * If we are still building up the display, then enqueues the callback to run after the
     * rendering is done. Otherwise runs the callback inline.
     *
     * @param r callback to run
     */
    private void runNowOrLater(Runnable r) {
        synchronized (startupTasks) {
            if (renderingInProgress) {
                startupTasks.add(r);
                return;
            }
        }
        r.run();
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
        } else {
            return null;
        }
    }

    public class SegmentPane extends JPanel {
        SegmentPane(ConfigRepresentation.SegmentEntry item) {
            JPanel p = this;
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.setAlignmentY(Component.TOP_ALIGNMENT);
            //p.setBorder(BorderFactory.createTitledBorder(name));

            createDescriptionPane(this, item.getDescription());

            // include map if present
            JPanel p2 = createPropertyPane(item.getMap());
            if (p2 != null) p.add(p2);
        }
    }

    void createDescriptionPane(JPanel parent, String d) {
        if (d == null) return;
        if (d.trim().length() == 0) return;
        JTextArea area = new JTextArea(d) {
            // Prevents the area from expanding vertically when there is vertical space to acquire.
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        area.setFont(UIManager.getFont("Label.font"));
        area.setEditable(false);
        area.setOpaque(false);
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        parent.add(area);
    }

    private void addCopyPasteButtons(JPanel linePanel, JTextField textField) {
        final JButton b = new JButton("Copy");
        final Color defaultColor = b.getBackground();
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String s = textField.getText();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        textField.selectAll();
                    }
                });
                StringSelection eventToCopy = new StringSelection(s);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(eventToCopy, new ClipboardOwner() {
                    @Override
                    public void lostOwnership(Clipboard clipboard, Transferable transferable) {
                        b.setBackground(defaultColor);
                        b.setText("Copy");
                    }
                });
                b.setBackground(COLOR_COPIED);
                b.setText("Copied");
            }
        });
        linePanel.add(b);

        JButton bb = new JButton("Paste");
        bb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                DataFlavor dataFlavor = DataFlavor.stringFlavor;

                Object text = null;
                try {
                    text = systemClipboard.getData(dataFlavor);
                } catch (UnsupportedFlavorException | IOException e1) {
                    return;
                }
                String pasteValue = (String) text;
                if (pasteValue != null) {
                    textField.setText(pasteValue);
                }
            }
        });
        linePanel.add(bb);
    }

    private class SearchPane extends JPanel {
        JPanel parent = null;
        JTextField textField;
        JTextField outputField;
        JPopupMenu suggestMenu = null;
        SearchPane() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            textField = new JTextField(32);
            textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));

            textField.setToolTipText("Enter the description of an event here to help pasting the event ID.");
            textField.getDocument().addDocumentListener(
                    new DocumentListener() {
                        @Override
                        public void insertUpdate(DocumentEvent documentEvent) {
                            textUpdated();
                        }

                        @Override
                        public void removeUpdate(DocumentEvent documentEvent) {
                            textUpdated();
                        }

                        @Override
                        public void changedUpdate(DocumentEvent documentEvent) {
                            textUpdated();
                        }
                    }
            );
            textField.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent keyEvent) {

                }

                @Override
                public void keyPressed(KeyEvent keyEvent) {
                    if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE ){
                        cancelSearch();
                    } else if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN) {
                        if (suggestMenu != null && suggestMenu.isVisible()) {
                            suggestMenu.setVisible(false);
                            suggestMenu.setFocusable(true);
                            suggestMenu.getSelectionModel().setSelectedIndex(0);
                            suggestMenu.show(textField, 0, textField.getHeight());
                            // Ugly hack to select the first entry in the menu. The menu needs a bit of time to appear.
                            javax.swing.Timer t = new javax.swing.Timer(100, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent actionEvent) {
                                    try {
                                        Robot r = new Robot();
                                        r.keyPress(KeyEvent.VK_DOWN);
                                        r.keyRelease(KeyEvent.VK_DOWN);
                                    } catch (AWTException e) {
                                    }
                                }
                            });
                            t.setRepeats(false);
                            t.start();
                            keyEvent.consume();
                            //suggestMenu.requestFocus();
                        }
                    }
                }

                @Override
                public void keyReleased(KeyEvent keyEvent) {

                }
            });
        }

        private void textUpdated() {
            if (parent == null) return;
            String searchQuery = textField.getText();
            logger.log(Level.FINE, String.format("Search for: %s",searchQuery));
            boolean fresh = false;
            if (suggestMenu == null) {
                suggestMenu = new JPopupMenu();
                fresh = true;
            }
            long startTime = System.nanoTime();
            List<EventTable.EventTableEntry> results = eventTable.searchForEvent(searchQuery, 8);
            long timelen = System.nanoTime() - startTime;
            logger.log(Level.FINE, String.format("Search took %.2f msec", 
                                   timelen * 1.0 / 1e6));
            suggestMenu.removeAll();
            for (EventTable.EventTableEntry result : results) {
                Action a = new AbstractAction(result.getDescription()) {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        String r = org.openlcb.Utilities.toHexDotsString(result.getEvent()
                                .getContents());
                        outputField.setText(r);
                        cancelSearch();
                    }
                };
                suggestMenu.add(a);
            }
            if (results.isEmpty()) {
                suggestMenu.add("No matches.");
            }
            suggestMenu.setFocusable(false);
            if (!fresh) {
                suggestMenu.revalidate();
                suggestMenu.pack();
                suggestMenu.repaint();
            }
            suggestMenu.show(textField, 0, textField.getHeight());
        }

        private void cancelSearch() {
            logger.log(Level.FINE, "Removing search box");
            if (suggestMenu != null) {
                suggestMenu.setVisible(false);
            }
            if (parent != null) {
                parent.remove(textField);
                parent.revalidate();
                parent.repaint();
                parent = null;
            }
        }

        void attachParent(JPanel parentPane, JTextField output) {
            if (parent != null) {
                cancelSearch();
            }
            textField.setText("");
            parentPane.add(textField);
            parent = parentPane;
            outputField = output;
            parentPane.revalidate();
            //setVisible(true);
            textField.requestFocusInWindow();
            //parentPane.repaint();
        }
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

            createDescriptionPane(this, item.getDescription());

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
        PropertyChangeListener entryListener = null;
        boolean dirty = false;
        JPanel p3;

        EntryPane(ConfigRepresentation.CdiEntry e, String defaultName) {
            item = e.getCdiItem();
            entry = e;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            String name = (item.getName() != null ? item.getName() : defaultName);
            setBorder(BorderFactory.createTitledBorder(name));

            createDescriptionPane(this, item.getDescription());

            p3 = new JPanel();
            p3.setAlignmentX(Component.LEFT_ALIGNMENT);
            p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
            add(p3);
        }

        void release() {
            if (entryListener != null) {
                entry.removePropertyChangeListener(entryListener);
            }
        }

        protected void additionalButtons() {}

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
            entryListener = new PropertyChangeListener() {
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
            };
            entry.addPropertyChangeListener(entryListener);
            cleanupTasks.add(new Runnable() {
                @Override
                public void run() {
                    entry.removePropertyChangeListener(entryListener);
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

            additionalButtons();

            p3.add(Box.createHorizontalGlue());
        }

        void updateColor() {
            if (entry.lastVisibleValue == null) {
                textComponent.setBackground(COLOR_UNFILLED);
                return;
            }
            String v = getDisplayText();
            boolean oldDirty = dirty;
            if (v.equals(entry.lastVisibleValue)) {
                textComponent.setBackground(COLOR_WRITTEN);
                dirty = false;
            } else {
                textComponent.setBackground(COLOR_EDITED);
                dirty = true;
            }
            if (oldDirty != dirty) {
                notifyTabColorRefresh();
            }
        }

        boolean isDirty() {
             return dirty;
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
        JLabel eventNamesLabel = null;
        EventTable.EventTableEntryHolder eventTableEntryHolder = null;
        String lastEventText;
        PropertyChangeListener eventListUpdateListener;
        // Stores the user names of known parent repeats. key: a group rep key that's a prefix of
        // the current key. value: the user name coming from the respective string valued field.
        Map<String, String> parentVisibleKeys = new TreeMap<>(Collections.reverseOrder());

        EventIdPane(ConfigRepresentation.EventEntry e) {
            super(e, "EventID");
            entry = e;

            textField = factory.handleEventIdTextField(EventIdTextField.getEventIdTextField());
            textComponent = textField;

            if (eventTable != null) {
                eventNamesLabel = new JLabel();
                eventNamesLabel.setVisible(false);
                eventListUpdateListener = new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                        if (propertyChangeEvent.getPropertyName().equals(EventTable
                                .UPDATED_EVENT_LIST)) {
                            updateEventDescriptionField((EventTable.EventInfo)
                                    propertyChangeEvent.getNewValue());
                        }
                    }
                };
                cleanupTasks.add(() -> {
                    releaseListener();
                });
            }
            init();
            if (eventTable != null) {
                add(eventNamesLabel);
            }
        }

        /**
         * Updates the UI for the list of other uses of the event.
         * 
         * @param eventInfo from the event table.
         */
        private void updateEventDescriptionField(EventTable.EventInfo eventInfo) {
            EventTable.EventTableEntry[] elist = eventInfo.getAllEntries();
            StringBuilder b = new StringBuilder();
            b.append("<html><body>");
            boolean first = true;
            for (EventTable.EventTableEntry ee: elist) {
                if (ee.isOwnedBy(eventTableEntryHolder)) continue;
                if (first) {
                    b.append("Other uses of this Event ID:<br>");
                    first = false;
                } else {
                    b.append("<br>");
                }
                b.append(ee.getDescription());
            }
            b.append("</body></html>");
            if (first)  {
                eventNamesLabel.setVisible(false);
            } else {
                eventNamesLabel.setText(b.toString());
                eventNamesLabel.setVisible(true);
            }
        }

        /**
         * Updates the exported event name in the event table. This needs to be called when some
         * user name field in the parent repeated group changes.
         */
        void updateOwnEventName() {
            if (eventTableEntryHolder == null) return;
            eventTableEntryHolder.getEntry().updateDescription(getEventName());
        }

        /**
         * @return the user name of this event.
         */
        private String getEventName() {
            StringBuilder b = new StringBuilder(entry.key);
            // Adds user names.
            parentVisibleKeys.forEach((k, v) -> {
                if (v.trim().length() > 0) {
                    b.insert(k.length() - 1, "," + v);
                }
            });
            // Clips some common uninteresting patterns.
            Matcher m = segmentPrefixRe.matcher(b);
            if (m.find()) {
                b.delete(m.start(), m.end());
            }
            m = entrySuffixRe.matcher(b);
            if (m.find()) {
                b.delete(m.start(), m.end());
            }
            // Prefix with the node name.
            if (nodeName.length() > 0) {
                b.insert(0, nodeName);
                b.insert(nodeName.length(), ".");
            }
            // Now we need to translate from zero-based indexes to 1-based indexes.
            for (int i = b.length() - 1; i >= 0; --i) {
                if (b.charAt(i) == '(') {
                    int j = i+1;
                    while (j < b.length() && Character.isDigit(b.charAt(j))) {
                        ++j;
                    }
                    if (j > i+1) {
                        int val = Integer.parseInt(b.substring(i + 1, j));
                        ++val;
                        b.replace(i+1,j,Integer.toString(val));
                    }
                }
            }
            return b.toString();
        }

        @Override
        protected void additionalButtons() {
            final JTextField tf = textField;
            p3.add(Box.createHorizontalStrut(5));
            addCopyPasteButtons(p3, textField);
            p3.add(Box.createHorizontalStrut(5));
            JButton b = new JButton("Search");
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    searchPane.attachParent(p3, tf);
                }
            });
            p3.add(b);
            p3.add(Box.createHorizontalStrut(5));
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

        @Override
        void updateColor() {
            super.updateColor();
            if (eventTable == null) return;
            // Updates the "other uses of event ID" label.
            String s = textField.getText();
            if (s.equals(lastEventText)) {
                return;
            }
            lastEventText = s;
            EventID id;
            try {
                 id = new EventID(s);
            } catch(RuntimeException e) {
                // Event is not in the right format. Ignore.
                return;
            }
            if (eventTableEntryHolder != null) {
                if (eventTableEntryHolder.getEntry().getEvent().equals(id)) {
                    return;
                }
                releaseListener();
            }
            if (id.equals(nullEvent)) {
                // Ignore event if it is the null event.
                eventNamesLabel.setVisible(false);
                return;
            }
            eventTableEntryHolder = eventTable.addEvent(id, getEventName());
            eventTableEntryHolder.getList().addPropertyChangeListener(eventListUpdateListener);
            updateEventDescriptionField(eventTableEntryHolder.getList());
        }

        private void releaseListener() {
            if (eventTableEntryHolder == null) return;
            eventTableEntryHolder.getList().removePropertyChangeListener(eventListUpdateListener);
            eventTableEntryHolder.release();
            eventTableEntryHolder = null;
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
