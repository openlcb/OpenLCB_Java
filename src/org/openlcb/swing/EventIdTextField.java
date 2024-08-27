// EventIdTextField.java

package org.openlcb.swing;

import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.MaskFormatter;

import org.openlcb.EventID;

/**
 * Text field for entry of forced-valid EventID string.
 *
 * Due to class loader issues, this is constructed in library form (as a
 * static method you call to get the field) rather than a subclass.
 *
 * @author	Bob Jacobsen   Copyright (C) 2012
 * @version	$Revision$
 */
public class EventIdTextField extends JFormattedTextField  {
    /** Comment for <code>serialVersionUID</code>. */
    private static final long serialVersionUID = 44833863963351863L;

    private final static Logger logger = Logger.getLogger(EventIdTextField.class.getName());

    public static JFormattedTextField getEventIdTextField() {
        JFormattedTextField retval = new JFormattedTextField(
                createFormatter("HH.HH.HH.HH.HH.HH.HH.HH"));

        // Let's size the event ID fields for the longest event ID in pixels.
        retval.setValue("DD.DD.DD.DD.DD.DD.DD.DD");
        retval.setPreferredSize(retval.getPreferredSize());
        retval.setValue("00.00.00.00.00.00.00.00");
        
        retval.setToolTipText("EventID as eight-byte dotted-hex string, "
                + "e.g. 01.02.0A.AB.34.56.78.00 "
                + "You can drag&drop or ctrl-click for more options");
                
        retval.setDragEnabled(true);
        retval.setTransferHandler(new CustomTransferHandler());
        
        configurePopUp(retval);

        return retval;
    }
    
    private static void configurePopUp(JFormattedTextField textfield) {
                
        // JMRI's apps.Apps.java line 330 adds cut/copy/paste to all JTextComponents
        //  (Also serves as example of cut/copy/paste code)
        
        JPopupMenu popup = createPopupMenu(textfield);
        //textfield.add(popup);
        textfield.setComponentPopupMenu(popup);
        textfield.setInheritsPopupMenu(false);
    }
    
    private static void checkAndShowPopup(JFormattedTextField textfield, MouseEvent event) {
        if ( event.isPopupTrigger() ) {
            // user requested the popup menu
            JPopupMenu popup = createPopupMenu(textfield);
            popup.show(textfield, event.getX(), event.getY());
        }
    }
    
    private static JPopupMenu createPopupMenu(JFormattedTextField textfield) {
        JPopupMenu popup = new JPopupMenu();
        
        // add the usual copy and paste operators
        // TODO: see CdiPanel line 1633 for how to properly copy and paste
        
        JMenuItem menuItem = new JMenuItem("Copy");
        popup.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = textfield.getText();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        textfield.selectAll();
                    }
                });
                StringSelection eventToCopy = new StringSelection(s);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(eventToCopy, new ClipboardOwner() {
                    @Override
                    public void lostOwnership(Clipboard clipboard, Transferable transferable) {
                    }
                });
            }
        });

        menuItem = new JMenuItem("Paste");
        popup.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                    textfield.setText(pasteValue);
                }
            }
        });
             
        // create a submenu for well-known events
        popup.add(makeWellKnownEventMenu(textfield));
        
        // Add the time events
        popup.add( makeClockEventMenuItem(textfield));

        // Add the accessory decoder events
        popup.add(makeDccAccessoryEventMenuItem(textfield));
        
        // popup.add("Extended DCC accessory decoder events ...");
        // popup.add("DCC turnout feedback events ...");
        // popup.add("DCC system sensor feedback events ...");
        
        return popup;
    }
    
    public static JMenu makeWellKnownEventMenu(JFormattedTextField textfield) {
        JMenu wkeMenu = new JMenu("Insert well-known event");
        wkeMenu.add(new EventIdInserter(
            "Emergency off (de-energize)",           "01.00.00.00.05.00.FF.FF", textfield));
        wkeMenu.add(new EventIdInserter(
            "Clear emergency off (energize)",        "01.00.00.00.00.00.FF.FE", textfield));
        wkeMenu.add(new EventIdInserter(
            "Emergency stop of all operations",      "01.00.00.00.05.00.FF.FD", textfield));
        wkeMenu.add(new EventIdInserter(
            "Clear emergency stop of all operations","01.00.00.00.00.00.FF.FC", textfield));
        return wkeMenu;
    }
    
    public static JMenuItem makeClockEventMenuItem(JFormattedTextField textfield) {
        JMenuItem menuItem = new JMenuItem("Insert Clock event...");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JDialog dialog = new JDialog();
                dialog.setTitle("Select Clock Settings");

                JPanel innerPanel = new JPanel(new FlowLayout());
                JComboBox<String> clockBox = new JComboBox<String>(
                        new String[]{
                            "Default Fast Clock",
                            "Default Real-Time Clock",
                            "Alternate Clock 1",
                            "Alternate Clock 2"
                });
                innerPanel.add(clockBox);
                util.com.toedter.calendar.JHourMinuteChooser chooser = new util.com.toedter.calendar.JHourMinuteChooser();
                innerPanel.add(chooser);
                JButton setButton = new JButton("Set");
                innerPanel.add(setButton);
                setButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String prefix = "01.01.00.00.01.0"+clockBox.getSelectedIndex();
                        int minute = Integer.parseInt(chooser.getMinute());
                        int hour = Integer.parseInt(chooser.getHour());
                        if (! chooser.getMeridian().equals("AM")) hour = hour+12;
                        textfield.setText(prefix+String.format(".%02X.%02X", hour, minute));
                        dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
                    }
                });
        
                dialog.add(innerPanel);
                dialog.setModal(true);
                dialog.pack();
                dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
                dialog.setVisible(true);
            }
        });
        return menuItem;
    }

    public static JMenuItem makeDccAccessoryEventMenuItem(JFormattedTextField textfield) {
        JMenuItem menuItem = new JMenuItem("Insert DCC accessory decoder events ...");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JDialog dialog = new JDialog();
                dialog.setTitle("Select DCC Accessory Decoder");

                JPanel innerPanel = new JPanel(new FlowLayout());

                JTextField number = new JTextField(12);
                number.setText("1");
                innerPanel.add(number);

                JComboBox<String> onOffBox = new JComboBox<String>(
                        new String[]{
                            "Reversed/Inactive/Off",
                            "Normal/Active/On"
                });
                innerPanel.add(onOffBox);

                JButton setButton = new JButton("Set");
                innerPanel.add(setButton);
                setButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int from = Integer.parseInt(number.getText().trim());
                        
                        // See JMRI OlcnAddress line 89 for Event ID coding
                        int DD = (from-1) & 0x3;
                        int aaaaaa = (( (from-1) >> 2)+1 ) & 0x3F;
                        int AAA = ( (from) >> 8) & 0x7;
                        long event = 0x0101020000FF0000L | (AAA << 9) | (aaaaaa << 3) | (DD << 1);
                        event |= onOffBox.getSelectedIndex();
 
                        EventID id = new EventID(String.format("%016X", event));
                        textfield.setText(id.toShortString());
                        dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
                    }
                });
        
                dialog.add(innerPanel);
                dialog.setModal(true);
                dialog.pack();
                dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
                dialog.setVisible(true);
            }
        });
        return menuItem;
    }
  
    private static class EventIdInserter extends JMenuItem {
        public EventIdInserter(String name, String value, JFormattedTextField target) {
            super(name);
            this.value = value;
            this.target = target;
            
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    target.setText(value);
                }
            });
        }
        final String value;
        final JFormattedTextField target;
        
    }
    
    
    private static MaskFormatter createFormatter(String s) {
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter(s);
        } catch (java.text.ParseException exc) {
            logger.log(Level.SEVERE, "formatter is bad: {0}", exc.getMessage());
        }
        return formatter;
    }

    static class CustomTransferHandler extends TransferHandler {
        /** Comment for <code>serialVersionUID</code>. */
        private static final long serialVersionUID = 3749257357774177433L;

        @Override
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }
    
        @Override
        public Transferable createTransferable(JComponent c) {
            return new StringSelection(((JTextComponent) c).getSelectedText());
        }
    
        @Override
        public void exportDone(JComponent c, Transferable t, int action) { }
    
        @Override
        public boolean canImport(TransferSupport ts) {
            return ts.getComponent() instanceof JTextComponent;
        }
    
        @Override
        public boolean importData(TransferSupport ts) {
            try {
                ((JTextComponent) ts.getComponent()).setText(
                        (String) ts.getTransferable()
                        .getTransferData(DataFlavor.stringFlavor));
                return true;
            } catch(UnsupportedFlavorException e) {
                return false;
            } catch(IOException e) {
                return false;
            }
        }
    }
}
