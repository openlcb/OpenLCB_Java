package org.openlcb.cdi.swing;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.openlcb.EventID;
import org.openlcb.NodeID;
import org.openlcb.Utilities;
import org.openlcb.cdi.CdiRep;
import org.openlcb.cdi.cmd.BackupConfig;
import org.openlcb.cdi.cmd.RestoreConfig;
import org.openlcb.cdi.impl.ConfigRepresentation;
import org.openlcb.implementations.EventTable;
import org.openlcb.swing.EventIdTextField;
import org.openlcb.ProducerConsumerEventReportMessage;

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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

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
 * @author  Pete Cressman Copyright 2020
 */
public class CdiPanel extends JPanel {
    private static final Logger logger = Logger.getLogger(CdiPanel.class.getName());

    private static final Color COLOR_EDITED = new Color(0xffd280); // orange
    private static final Color COLOR_UNFILLED = new Color(0xffff00); // yellow
    private static final Color COLOR_WRITTEN = new Color(0xffffff); // white
    private static final Color COLOR_ERROR = new Color(0xff0000); // red
    private static final Pattern segmentPrefixRe = Pattern.compile("^seg[0-9]*[.]");
    private static final Pattern entrySuffixRe = Pattern.compile("[.]child[0-9]*$");
    private static final Color COLOR_COPIED = COLOR_EDITED; // orange

    /**
     * We always use the same file chooser in this class, so that the user's
     * last-accessed directory remains available.
     */
    static JFileChooser fci = new JFileChooser();
    {
        fci.setSelectedFile(new File(".txt"));
    }

    private ConfigRepresentation rep;
    private EventTable eventTable = null;
    private String nodeName = "";
    private boolean _changeMade = false;    // set true when a write is done to the hardware.
    private boolean _unsavedRestore = false;    // set true when a restore is done.
    private boolean _panelChange = false;   // set true when a panel item changed.
    private JButton _saveButton;
    private Color COLOR_DEFAULT;
    private List<util.CollapsiblePanel> segmentPanels = new ArrayList<>();
    private List<util.CollapsiblePanel> navPanels = new ArrayList<>();
    private final Color COLOR_BACKGROUND;
    private CollapsiblePanel sensorHelperPanel;
    /// Panel at the bottom of the window with command buttons.
    //private JPanel bottomPanel;
    /// To get focus to the bottom panel, this component needs to be activated.
    private JComponent bottomPanelHead;

    public CdiPanel () {
        super();
        tabColorTimer = new Timer("OpenLCB CDI Reader Tab Color Timer");
        COLOR_BACKGROUND = getBackground().darker();
        setForeground(COLOR_BACKGROUND);
    }

    /**
     *
     * @param dir the current directory where backup and restore dialogs will open.
     */
    public CdiPanel (File dir) {
        this();
        // dir is jmri.util.FileUtil.getUserFilesPath()
        fci.setCurrentDirectory(dir);
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
        contentPanel.setBackground(COLOR_BACKGROUND);

        scrollPane = new JScrollPane(contentPanel);
        Dimension minScrollerDim = new Dimension(800, 12);
        scrollPane.setMinimumSize(minScrollerDim);
        scrollPane.getVerticalScrollBar().setUnitIncrement(30);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane);

        bottomPanel = new JPanel();
        //buttonBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomPanel.setLayout(new FlowLayout());
        JButton bb = new JButton("Refresh All");
        bb.setToolTipText("Discards all changes and loads the freshest value from the hardware for all entries.");
        bb.addActionListener(actionEvent -> reloadAll());
        bottomPanelHead = bb;
        bottomPanel.add(bb);
        addNavigationHelper(null, bottomPanel, bb);

        _saveButton = new JButton("Save Changes");
        COLOR_DEFAULT = _saveButton.getBackground();
        _saveButton.setToolTipText("Writes every changed value to the hardware.");
        _saveButton.addActionListener(actionEvent -> saveChanged());
        bottomPanel.add(_saveButton);

        bb = new JButton("Backup...");
        bb.setToolTipText("Creates a file on your computer with all saved settings from this node. Use the \"Save Changes\" button first.");
        bb.addActionListener(actionEvent -> runBackup());
        bottomPanel.add(bb);

        bb = new JButton("Restore...");
        bb.setToolTipText("Loads a file with backed-up settings. Does not change the hardware settings, so use \"Save Changes\" afterwards.");
        bb.addActionListener(actionEvent -> runRestore());
        bottomPanel.add(bb);

        if (rep.getConnection() != null && rep.getRemoteNodeID() != null) {
            bb = new JButton("Reboot");
            bb.setToolTipText("Requests the configured node to restart.");
            bb.addActionListener(actionEvent -> runReboot());
            addButtonToMoreFunctions(bb);

            bb = new JButton("Update Complete");
            bb.setToolTipText("Tells the configured node that the you are done with changing the settings and they should be taking effect now. Might restart the node.");
            bb.addActionListener(actionEvent -> runUpdateComplete());
            addButtonToMoreFunctions(bb);

            bb = new JButton("Factory Reset");
            bb.setToolTipText("Resets the node to its factory default content");
            bb.addActionListener(actionEvent -> runFactoryReset());
            addButtonToMoreFunctions(bb);
        }

        createSensorCreateHelper();

        bottomPanel.setMaximumSize(bottomPanel.getMinimumSize());
        add(bottomPanel);

        _changeMade = false;
        setSaveClean();

        synchronized(rep) {
            if (rep.getRoot() != null) {
                displayCdi();
            } else {
                displayLoadingProgress();
            }
        }

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                updateWidth();
                super.componentResized(componentEvent);
            }
        });
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

        // Calls into JMRI to add the Create Sensor and Create Turnout buttons.
        factory.handleGroupPaneEnd(createHelper);
        sensorHelperPanel = new CollapsiblePanel("Sensor/Turnout creation", createHelper);
        sensorHelperPanel.setBackground(getForeground());
        sensorHelperPanel.setExpanded(false);
        sensorHelperPanel.setBorder(BorderFactory.createMatteBorder(10,0,10,0, getForeground()));
        //cp.setMinimumSize(new Dimension(0, cp.getPreferredSize().height));
        add(sensorHelperPanel);
    }

    /**
     * Load from a CDI representation with a default {@link GuiItemFactory}.
     *
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
            bottomPanel.add(c);
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
            bottomPanel.add(moreButton);
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

    private void saveChanged() {
        for (EntryPane entry : allEntries) {
            if (entry.isDirty()) {
                entry.writeDisplayTextToNode();
            }
        }
        checkForSave();

        logger.info("Save changes done.");
    }

    private void checkForSave() {
        for (EntryPane entry : allEntries) {
            if (entry.isDirty()) {
                setSaveDirty();
                return;   // do nothing, still dirty
            }
        }
        _unsavedRestore = false;
        setSaveClean();
    }

    /**
     * Triggers a warning at the close of this dialog that a Sensor has been made.
     * This triggers a message the panel file needs to be saved in JMRI.
     *
     * @param uName unused.
     */
    public void madeSensor(String uName) {
        _panelChange = true;
    }

    /**
     * Triggers a warning at the close of this dialog that a Turnout has been made.
     * This triggers a message the panel file needs to be saved in JMRI.
     *
     * @param uName unused.
     */
    public void madeTurnout(String uName) {
        _panelChange = true;
    }

    public void runBackup() {
        // First select a file to save to.
        fci.setDialogTitle("Save configuration backup file");

        fci.rescanCurrentDirectory();
        fci.setSelectedFile(new File(generateFileName()));
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
            logger.info("Config backup done.");
    }

    // Class that generates the filename for a Backup operation.
    public static class FileNameGenerator {
        public String generateFileName(ConfigRepresentation rep, String nodeName) {
            String fileName = rep.getRemoteNodeAsString();
            if (!nodeName.isEmpty()) {
                fileName += "-"+nodeName;
            }
            if (rep.getCdiRep() != null && rep.getCdiRep().getIdentification() != null) {
                // info not present if configuration hasn't been read yet
                fileName += "-"+rep.getCdiRep().getIdentification().getSoftwareVersion();
            }
            fileName += "-"+java.time.LocalDate.now();
            fileName += "-"+java.time.LocalTime.now().format( // use default time zone
                            java.time.format.DateTimeFormatter.ofPattern("HH-mm-ss")
                        );

            fileName = fileName.replace(" ", "_"); // don't use spaces in file names!

            return "config." + fileName + ".txt";
        }
    }
    public static FileNameGenerator fileNameGenerator = new FileNameGenerator(); // this can be replaced to change the generated file name pattern
    private String generateFileName() {
        return fileNameGenerator.generateFileName(rep, nodeName);
    }


    public void runRestore() {
        // First select a file to save to.
        fci.setDialogTitle("Open configuration restore file");
        fci.rescanCurrentDirectory();

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
        _unsavedRestore = true;
    }

    private void runReboot() {
        rep.getConnection().getDatagramService().sendData(rep.getRemoteNodeID(), new int[] {0x20, 0xA9});
    }

    private void runFactoryReset() {
        int reply = javax.swing.JOptionPane. showConfirmDialog(this,
                "Do you want to make a backup first?", "Factory Reset", JOptionPane.YES_NO_OPTION);
        if (reply != javax.swing.JOptionPane.NO_OPTION) {
            runBackup();
        }

        reply = javax.swing.JOptionPane. showConfirmDialog(this,
                "This resets node contents. Proceed?", "Factory Reset", JOptionPane.YES_NO_OPTION);
        if (reply != javax.swing.JOptionPane.YES_OPTION) return;

       int[] contentArray = new int[8];
        contentArray[0] = 0x20;
        contentArray[1] = 0xAA;

        byte[] nodeID = rep.getRemoteNodeID().getContents();
        for (int i =0; i < 6; i++)
            contentArray[i+2] = nodeID[i];

        rep.getConnection().getDatagramService().sendData(rep.getRemoteNodeID(), contentArray);
    }

    private void runUpdateComplete() {
        try {
            rep.getConnection().getDatagramService().sendData(rep.getRemoteNodeID(), new int[]{0x20, 0xA8});
        } catch (NullPointerException e) {
            // Ignore nullptr, this happens during tests when a mock object does not have a
            // connection.
        }
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

    JPanel bottomPanel;
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

    /**
     * Remove the listener to the CDI representation that gets a notification that 
     * loading of the CDI is complete.
     * Paired with {@link #addLoadingListener}.
     */
    private void removeLoadingListener() {
        synchronized (rep) {
            if (loadingListener != null) rep.removePropertyChangeListener(loadingListener);
            loadingListener = null;
        }
    }

    /**
     * Add a listener to the CDI representation to get a notification that 
     * loading of the CDI is complete.
     * Paired with {@link #removeLoadingListener}.
     */
    private void addLoadingListener() {
        synchronized(rep) {
            if (loadingListener != null) return;
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

    /**
     * CDI loading is done, update the UI and listeners
     */
    private void hideLoadingProgress() {
        if (loadingPanel == null) return;
        removeLoadingListener();
        loadingPanel.setVisible(false);
    }

    private void displayLoadingProgress() {
        if (loadingPanel == null) {
            createLoadingPane();
            contentPanel.add(loadingPanel);
        }
        addLoadingListener();
        loadingPanel.setVisible(true);
    }

    /**
     * Create a separate thread to render the CDI to the UI.
     * When done, invokes {@link displayComplete} on the Swing/AWT thread
     */
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
                addNavigationActions(sensorHelperPanel);
                EventQueue.invokeLater(() -> displayComplete());
            }
        }, "openlcb-cdi-render").start();
    }

    /**
     * Rendering thread is complete, show the CDI display in the UI.
     * Must be invoked on the Swing/AWT thread
     */
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
        setSaveClean();
        notifyTabColorRefresh();
        SwingUtilities.invokeLater(() -> {
            JFrame f = (JFrame)SwingUtilities.getAncestorOfClass(JFrame.class, this);
            if (f == null) {
                logger.log(Level.FINE, "Could not add close window listener");
                return;
            }
            // The frame gets disposed in the closing event handler.
            f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            f.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    targetWindowClosingEvent(e);
                }
            });
        });
        segmentPanels.forEach(p -> p.setExpanded(false));
    }

    private void targetWindowClosingEvent(WindowEvent e) {
        StringBuilder sb = new StringBuilder();
        if (_unsavedRestore) {
            sb.append("The configuration was restored but not saved.");
            sb.append("\n");
        }
        boolean save = _unsavedRestore;
        int num_dirty = 0;
        final int MAX_DIRTY_TO_SHOW = 10;
        for (EntryPane entry : allEntries) {
            if (entry.isDirty()) {
                if (++num_dirty <= MAX_DIRTY_TO_SHOW) {
                    GetEntryNameVisitor nameGetter = new GetEntryNameVisitor(entry);
                    rep.visit(nameGetter);
                    sb.append(nameGetter.getName());
                    sb.append(" has not been saved.");
                    sb.append("\n");
                }
                save = true;
            }
        }
        if (num_dirty > MAX_DIRTY_TO_SHOW) {
            sb.append(num_dirty - MAX_DIRTY_TO_SHOW);
            sb.append(" additional entries have not been saved.");
            sb.append("\n");
        }
        if (_panelChange) {
            sb.append("The panel tables have been changed. To keep these changes, save the panel file.");
            sb.append("\n");
        }
        if (num_dirty > 0) {
            sb.append("\nPress Cancel to go back and save these changes.");
            Object[] options = { "Discard changes", "Cancel" };
            int confirm = JOptionPane.showOptionDialog(this, sb.toString(),
                    "Unsaved changes", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[1]);
            if (confirm != 0) {
                return;
            }
        } else if (_panelChange) {
            JOptionPane.showMessageDialog(this, sb.toString(),
                    "Tables are changed",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        if (_changeMade) {
            runUpdateComplete();
        }
        release();
        JFrame f = (JFrame)SwingUtilities.getAncestorOfClass(JFrame.class, this);
        f.dispose();
    }

    /**
     * Updates the save changes button to mark the dialog dirty.
     */
    private void setSaveDirty() {
        SwingUtilities.invokeLater(() -> {
            _saveButton.setBackground(COLOR_EDITED);
            _saveButton.setEnabled(true);
        });
    }

    /**
     * Updates the Save Changes button to mark the dialog clean.
     */
    private void setSaveClean() {
        SwingUtilities.invokeLater(() -> {
            _saveButton.setBackground(COLOR_DEFAULT);
            _saveButton.setEnabled(false);
        });
    }

    /**
     * Visitor class to find a full name for changed elements
     * that have not been saved.
     */
    private class GetEntryNameVisitor extends ConfigRepresentation.Visitor {
        CdiRep.Item item;
        int segNum = 1;
        String segName = null;
        String groupName = null;
        String groupRepName = null;
        String entryName = null;
        String fullName = null;
        boolean done = false;

        GetEntryNameVisitor(EntryPane ep) {
            item = ep.item;
        }

        @Override
        public void visitSegment(ConfigRepresentation.SegmentEntry e) {
            if (!done) {
                groupName = null;
                groupRepName = null;
                segName = e.segment.getName();
                segNum++;
                visitContainer(e);
            }
        }

        @Override
        public void visitGroupRep(ConfigRepresentation.GroupRep e) {
            if (!done) {
                groupRepName = e.group.getName() + e.index;
                visitContainer(e);
            }
        }

        @Override
        public void visitGroup(ConfigRepresentation.GroupEntry e) {
            if (!done) {
                groupName = e.group.getName();
                visitContainer(e);
            }
        }

        @Override
        public void visitLeaf(ConfigRepresentation.CdiEntry e) {
            if (!done && item.equals(e.getCdiItem())) {
                entryName = e.getCdiItem().getName();
                StringBuilder sb = new StringBuilder();
                sb.append("Item \"");
                sb.append(entryName);
                sb.append("\"");
                if (groupRepName == null) {
                    groupRepName = groupName;
                } else if (groupName != null) {
                    sb.append(" in ");
                    sb.append(groupName);
                }
                if (groupRepName != null) {
                    sb.append(" of group \"");
                    sb.append(groupRepName);
                    sb.append("\"");
                }
                sb.append(" in segment ");
                if (segName == null || segName.isEmpty()) {
                    sb.append("#");
                    sb.append(segNum);
                } else {
                    sb.append(segName);
                }
                fullName =  sb.toString();
                done = true;
            }
        }

        String getName() {
            if (fullName == null) {
                return "NotFound";
            }
            return fullName;
        }
    }

    private void repack() {
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win != null) win.pack();
    }

    private void updateWidth() {
        int w = getSize().width - 4;
        // Delays updating the segment width until the rendering of the panels is complete.
        runNowOrLater(()->{ segmentPanels.forEach(p->p.setMaximumWidth(w)); });
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
        checkForSave();
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
            if (foundEntry != null) {
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

            String name = "Segment" + (e.getName() != null ? (": " + e.getName()) : "");
            util.CollapsiblePanel cPanel = new util.CollapsiblePanel(name, currentPane);
            segmentPanels.add(cPanel);
            addNavigationActions(cPanel);
            // cPanel.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED)); //debugging
            cPanel.setAlignmentY(Component.TOP_ALIGNMENT);
            cPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            cPanel.setBorder(BorderFactory.createMatteBorder(10,0,0,0, getForeground()));

            super.visitSegment(e);
            contentPanel.add(cPanel);
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

            if (groupPane.getComponentCount() > 0) {
                if (oldPane instanceof SegmentPane) {
                    // we make toplevel groups collapsible.
                    groupPane.setBorder(null);
                    CollapsiblePanel cPanel = new CollapsiblePanel(groupPane.getName(), groupPane);
                    // cPanel.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED)); //debugging
                    cPanel.setAlignmentY(Component.TOP_ALIGNMENT);
                    cPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    oldPane.add(cPanel);
                    addNavigationActions(cPanel);
                } else {
                    oldPane.add(groupPane);
                }
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
            final String name = item.getRepName(e.index, item.getReplication());
            //currentPane.setBorder(BorderFactory.createTitledBorder(name));
            
            // set the name of this pane, which names the tab
            currentPane.setName(name);
            
            // Finds a string field that could be used as a caption.
            FindDescriptorVisitor vv = new FindDescriptorVisitor();
            vv.visitContainer(e);

            if (vv.foundEntry != null) {
                // here a unique descriptor has been found
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
                                    // we have a new name from a description, use it
                                    String newName = (name + " (" + source.lastVisibleValue + ")");
                                    tabPanel.setName(newName);
                                    if (parentTabs.getTabCount() >= e.index) {
                                        JComponent tabLabel = getTabLabel(parentTabs, e.index-1, newName, e);
                                        parentTabs.setTabComponentAt(e.index - 1, tabLabel);
                                    }
                                    downstreamName = source.lastVisibleValue;
                                } else {
                                    // use the name created above from the repName
                                    if (parentTabs.getTabCount() >= e.index) {
                                        // update the name listed in the tab
                                        JComponent tabLabel = getTabLabel(parentTabs, e.index-1, name, e);
                                        parentTabs.setTabComponentAt(e.index - 1, tabLabel);
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

            // add this new pane to the combined tab pane
            currentTabbedPane.add(currentPane);
            tabsByKey.put(e.key, currentTabbedPane);
            
            // set the tab to a label with copy/pasteValue
            int index = currentTabbedPane.indexOfComponent(currentPane);
            JComponent tabLabel = getTabLabel(currentTabbedPane, index, name, e);
            currentTabbedPane.setTabComponentAt(index, tabLabel);
            
        }

        /**
         * Generate the tab label for a group item.
         * Including any needed navigation, tooltip, popup menu, etc.
         * @param parentTabbedPane The tabbed pane which it to be navigated
         * @param name The name to display
         * @param rep the configuration data representation
         * @return Tab label component
         */
        protected JComponent getTabLabel(JTabbedPane parentTabbedPane, int index, String name, ConfigRepresentation.GroupRep rep) {
            JLabel tabLabel = new JLabel(name);
            
            tabLabel.addMouseListener(new MouseAdapter() {

                // for click logic: https://stackoverflow.com/questions/46840814/right-click-jpopupmenu-on-jtabbedpane
                @Override
                public void mouseClicked(MouseEvent event) {

                    // isPopupTrigger doesn't work on all platforms, all versions?
                    boolean isPopup = (event.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0 || event.isPopupTrigger();
                    if ( !isPopup ) {
                        // user selected a tab
                        parentTabbedPane.setSelectedIndex(index);
                    } else {
                        // user requested the popup menu
                        // move to tab, in case non-active one clicked
                        parentTabbedPane.setSelectedIndex(index);
                        
                        JPopupMenu popupMenu = new JPopupMenu();
                        JMenuItem menuItem = new JMenuItem("Copy");
                        popupMenu.add(menuItem);
                        menuItem.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                performGroupReplCopy(index, rep);
                            }
                        });
                        menuItem = new JMenuItem("Paste");
                        popupMenu.add(menuItem);
                        menuItem.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                performGroupReplPaste(index, rep);
                            }
                        });
                        
                        popupMenu.show(tabLabel, event.getX(), event.getY());
                    }
                }

            });
            
            return tabLabel;
        }
        
        /**
         * Perform a "copy" operation on a selected group tab
         */
        protected void performGroupReplCopy(int index, ConfigRepresentation.GroupRep rep) {
            String result = groupReplToString(rep);
            
            // store to clipboard
            StringSelection selection = new StringSelection(result);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);            
            
        }
                
        /**
         * Copy an entire group replication to a String
         */
        protected String groupReplToString(ConfigRepresentation.GroupRep rep) {
            StringBuilder result = new StringBuilder();

            ConfigRepresentation.Visitor visitor = new ConfigRepresentation.Visitor() {
 
                @Override
                public void visitString(ConfigRepresentation.StringEntry e) {
                   writeEntry(e.key, e.getValue());
                }

                @Override
                public void visitInt(ConfigRepresentation.IntegerEntry e) {
                   writeEntry(e.key, Long.toString(e.getValue()));
                }

                @Override
                public void visitEvent(ConfigRepresentation.EventEntry e) {
                   writeEntry(e.key, org.openlcb.Utilities.toHexDotsString(e.getValue
                           ().getContents()));
                }
                
                protected void writeEntry(String key, String entry) {
                    result.append(key);
                    result.append("=");
                    // result.append(entry); // use the value currently in CD
                    result.append(entriesByKey.get(key).getCurrentValue()); // use value currently in UI
                    result.append("\n");
                }
            };
            
            visitor.visitGroupRep(rep);
            return new String(result);
        }  
            
        
        /**
         * Perform a "paste" operation into a selected group tab
         */
        protected void performGroupReplPaste(int index, ConfigRepresentation.GroupRep rep) {
            // retrieve from clipboard
            Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable t = c.getContents( null );
            String newContentString = "";
            if ( t.isDataFlavorSupported(DataFlavor.stringFlavor) ) {
                try {
                    newContentString = (String)t.getTransferData( DataFlavor.stringFlavor );
                } catch (UnsupportedFlavorException | IOException e) {
                    // this can never happen as we checked before
                    return;
                }
            } // this should always have succeeded, but if it doesn't the match below will fail
            
            // store the values that are going to be replaced
            String previousContentString = groupReplToString(rep); 

            String[] newContentLines = newContentString.split("\n");
            String[] previousContentLines = previousContentString.split("\n");
            
            // compare keys to see if the variables match up
            // this version just checks the line count, more could be added here
            if (previousContentLines.length != newContentLines.length) {
                logger.log(Level.WARNING, "Cannot paste into a mis-matching entry type");
                // provide a system alert to notify user
                Toolkit.getDefaultToolkit().beep();
                // end of attempt to paste
                return;
            }
            
            // change the repl number in the newContentLines to this index
            // First, find the prefix we're going to change
            List<String> list = java.util.Arrays.asList(newContentLines);
            String prefix = Utilities.longestLeadingSubstring(list);
            // That prefix should end with "(1)."
            
            // replace and rebuild the lines
            StringBuilder processedContentSB = new StringBuilder();
            for (int i = 0; i<newContentLines.length; i++) {
                newContentLines[i] = rep.key+"."+newContentLines[i].substring(prefix.length())+"\n";
                processedContentSB.append(newContentLines[i]);
            }
            // at this point, processedContent has the new values to restore
            String processedContent = new String(processedContentSB);
            
            // finally, feed to the restore procedure
            BufferedReader reader = new BufferedReader(new StringReader(processedContent));
            
            // This is largely taken from the runRestore method in this class
            RestoreConfig.parseConfigFromReader(reader, new RestoreConfig.ConfigCallback() {
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
            _unsavedRestore = true;
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

    /// Goes backwards to the previous (collapsible) segment.
    private void navigateUp(util.CollapsiblePanel current) {
        int index = navPanels.indexOf(current);
        if (index < 0) {
            // start from bottom
            index = navPanels.size();
        }
        while (--index >= 0) {
            if (navPanels.get(index).isShowing()) {
                navPanels.get(index).getHeader().requestFocusInWindow();
                return;
            }
        }
        bottomPanelHead.requestFocusInWindow();
    }

    /// Goes forwards to the next (collapsible) segment.
    private void navigateDown(util.CollapsiblePanel current) {
        int index = navPanels.indexOf(current);
        if (index < 0) {
            // start from top
            index = -1;
        }
        while (++index < navPanels.size()) {
            if (navPanels.get(index).isShowing()) {
                navPanels.get(index).getHeader().requestFocusInWindow();
                return;
            }
        }
        bottomPanelHead.requestFocusInWindow();
    }

    private void addNavigationActions(util.CollapsiblePanel cPanel) {
        navPanels.add(cPanel);
        addNavigationHelper(cPanel, cPanel, cPanel.getHeader());
    }

    /**
     * Adds handling of F6/ shift-F6 to a given panel.
     * @param navigationKey Reference from which to go up/down in the navPanels array.
     * @param panel JPanel that is the root of the set of components that form tis navigation step
     * @param header What is the first component in this panel that should receive focus
     */
    private void addNavigationHelper(CollapsiblePanel navigationKey, JPanel panel,
                                     JComponent header) {
        panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift F6"), "focusCurrentSegment");
        panel.getActionMap().put("focusCurrentSegment", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                header.requestFocusInWindow();
            }
        });
        header.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("shift F6"),
                "focusPreviousSegment");
        header.getActionMap().put("focusPreviousSegment", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                navigateUp(navigationKey);
            }
        });
        panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("F6"), "focusNextSegment");
        panel.getActionMap().put("focusNextSegment", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                navigateDown(navigationKey);
            }
        });
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

        util.CollapsiblePanel ret = new util.CollapsiblePanel("Identification", p);
        segmentPanels.add(ret);
        addNavigationActions(ret);
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
        area.setFont(UIManager.getFont("TextArea.font"));
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

    private abstract class EntryPane extends JPanel {
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
            if (textComponent instanceof JTextArea) {
                p3.add(new JScrollPane(textComponent));
            } else {
                p3.add(textComponent);
            }
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
//                EventQueue.invokeLater(() -> checkForSave());
            } else {
                textComponent.setBackground(COLOR_EDITED);
                dirty = true;
                setSaveDirty();
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
        protected abstract void updateDisplayText(@NonNull String value);

        // returns the currently displayed value ("" if none).
        @NonNull
        protected abstract String getDisplayText();
        
        /**
         * Get the current value as a String.
         * Usually, this is the display text, but in the case of a 
         * {@link IntPane} with a map it's the integer value of the 
         * current selection.
         * @return Current value for storage as a String.
         */
        @NonNull
        protected String getCurrentValue() {
            return getDisplayText();
        }
    }

    private class EventIdPane extends EntryPane {
        private final ConfigRepresentation.EventEntry entry;
        JFormattedTextField textField;
        JLabel eventNamesLabel = null;
        EventTable.EventTableEntryHolder eventTableEntryHolder = null;
        String lastEventText;
        PropertyChangeListener eventListUpdateListener;
        // Stores the user names of known parent repeats. key: a group rep key that's a prefix of
        // the current key. value: the user name coming from the respective string valued field.
        Map<String, String> parentVisibleKeys = new TreeMap<>(Collections.reverseOrder());

        JPopupMenu eventidMoreMenu = new JPopupMenu();
        JButton eventidMoreButton;

        EventIdPane(ConfigRepresentation.EventEntry e) {
            super(e, "EventID");
            entry = e;

            textField = factory.handleEventIdTextField(EventIdTextField.getEventIdTextField());
            textComponent = textField;

            if (eventTable != null) {
                eventNamesLabel = new JLabel();
                eventNamesLabel.setFont(UIManager.getFont("TextArea.font"));
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

            JButton bb = factory.handleProduceButton(new JButton("Trigger"));
            bb.setToolTipText("Click to fire this event.");
            bb.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    NodeID node = rep.getConnection().getNodeId();
                    EventID ev = new EventID(org.openlcb.Utilities.bytesFromHexString((String)textField.getText()));
                    rep.getConnection().getOutputConnection().put(new ProducerConsumerEventReportMessage(node, ev), rep.getConnection().getOutputConnection());
                }
            });
            addButtonToEventidMoreFunctions(bb);

            JButton bAS = factory.handleProduceButton(new JButton("Make Sensor"));
            bAS.setToolTipText("Add a JMRI sensor with the Event ID.");
            bAS.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    factory.makeSensor(textField.getText(), getEventName());
                }
            });
            addButtonToEventidMoreFunctions(bAS);

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

        private void addButtonToEventidMoreFunctions(final JButton b) {
            if (eventidMoreButton == null) {
                eventidMoreButton = new JButton("More...");
                eventidMoreButton.setToolTipText("Additional actions you can do with this Event ID");
                eventidMoreButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        showEventidMoreFunctionsMenu();
                    }
                });
                p3.add(eventidMoreButton);
            }
            Action a = new AbstractAction(b.getText()) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    b.doClick();
                }
            };
            eventidMoreMenu.add(a);

        }

        private void showEventidMoreFunctionsMenu() {
            eventidMoreMenu.show(eventidMoreButton, 0, eventidMoreButton.getHeight());
        }

        @Override
        protected void writeDisplayTextToNode() {
            byte[] contents = org.openlcb.Utilities.bytesFromHexString((String) textField
                    .getText());
            entry.setValue(new EventID(contents));
            _changeMade = true;
            notifyTabColorRefresh();
        }

        @Override
        protected void updateDisplayText(@NonNull String value) {
            textField.setText(value);
        }

        @NonNull
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


    private class IntPane extends EntryPane {
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
            _changeMade = true;
            notifyTabColorRefresh();
        }

        @Override
        protected void updateDisplayText(@NonNull String value) {
            if (textField != null) textField.setText(value);
            if (box != null) box.setSelectedItem(value);
        }

        @NonNull
        @Override
        protected String getDisplayText() {
            String s = (box == null) ? (String) textField.getText()
                    : (String) box.getSelectedItem();
            return s == null ? "" : s;
        }

        /**
         * Get the current value as a numerical String.
         * Usually, this is the display text, but in the case of a 
         * a map it's the integer value of the 
         * current selection.
         * @return Current value for storage as a String.
         */
        @NonNull
        protected String getCurrentValue() {
            String s;
            if (box==null) {
                s = (String) textField.getText();
            } else {
                String entry = (String) box.getSelectedItem();
                s = map.getKey(entry);  
            }
            return s == null ? "" : s;
        }

    }

    private class StringPane extends EntryPane {
        JTextComponent textField;
        private final ConfigRepresentation.StringEntry entry;

        StringPane(ConfigRepresentation.StringEntry e) {
            super(e, "String");
            this.entry = e;

            if (entry.size <= 64) {
                JTextField jtf = new JTextField(entry.size) {
                    public Dimension getMaximumSize() {
                        return getPreferredSize();
                    }
                };
                jtf = factory.handleStringValue(jtf);
                textField = jtf;
            } else {
                // Long string. Show multi-line editor
                JTextArea jta = new JTextArea(Math.min(40, (int)(entry.size / 60)), 80);
                jta.setEditable(true);
                jta.setLineWrap(true);
                jta.setWrapStyleWord(true);
                jta = factory.handleEditorValue(jta);
                textField = jta;
            }
            textComponent = textField;
            textComponent.setToolTipText("String of up to "+entry.size+" characters");
            init();
        }

        @Override
        protected void writeDisplayTextToNode() {
            entry.setValue(textField.getText());
            _changeMade = true;
            notifyTabColorRefresh();
        }

        @Override
        protected void updateDisplayText(@NonNull String value) {
            textField.setText(value);
        }

        @NonNull
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
        /**
         * Allows replacement of the Read button in the interface
         * @param button proposed Read button for the interface
         * @return the actual Read button for the interface
         */
        public JButton handleReadButton(JButton button) {
            return button;
        }
        
        /**
         * Allows replacement of the Write button in the interface
         * @param button proposed Write button for the interface
         * @return the actual Write button for the interface
         */
        public JButton handleWriteButton(JButton button) {
            return button;
        }
        
        /**
         * Allows replacement of the More... button on an event ID in the interface
         * @param button proposed More... button for the interface
         * @return the actual More... button for the interface
         */
        public JButton handleEventidMoreButton(JButton button) {
            return button;
        }
        
        /**
         * Allows replacement of the Produce button in the interface
         * @param button proposed Produce button for the interface
         * @return the actual Produce button for the interface
         */
        public JButton handleProduceButton(JButton button) {
           return button;
        }
        
        /**
         * Process pressing the Make Sensor button
         * @param ev EventId to use
         * @param mdesc Description to Use
         */
        public void makeSensor(String ev, String mdesc) {
            return;
        }
        
        /**
         * A new CDI group is being constructed.  This makes the place for that
         * available so that it can be replaced if need be.
         * Paired with {@link handleGroupPaneEnd}.
         *
         * @param pane The GUI panel that will be populated with the 
         *              representation of the group. In {@link CdiPanel}, 
         *              this is a {@link GroupPane}.
         */
        public void handleGroupPaneStart(JPanel pane) {
            return;
        }

        /**
         * Called after a group have been constructedd.
         * Paired with {@link handleGroupPaneStart}.
         *
         * @param pane The GUI panel that has been populated with the 
         *              representation of the group. In {@link CdiPanel}, 
         *              this is a {@link GroupPane}.
         */
        public void handleGroupPaneEnd(JPanel pane) {
            return;
        }
        
        /**
         * Allow separate processing of an Event ID entry field
         * @param field The proposed EventID entry field
         * @return The EventID entry field to use
         */
        public JFormattedTextField handleEventIdTextField(JFormattedTextField field) {
            return field;
        }
        
        /** Allow updates of the input field for a text value.
         * @param value The proposed input field.
         * @return The input field to use.
         */
        public JTextField handleStringValue(JTextField value) {
            return value;
        }
        
        /** Allow updates of the input field for a text value.
         * In {@link CdiPanel}, this is an input field with a length of more than 64.
         * @param value The proposed input field.
         * @return The input field to use.
         */
        public JTextArea handleEditorValue(JTextArea value) {
            return value;
        }
    }
}
