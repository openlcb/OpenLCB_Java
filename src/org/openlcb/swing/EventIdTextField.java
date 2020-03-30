// EventIdTextField.java

package org.openlcb.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.TransferHandler;
import javax.swing.text.JTextComponent;
import javax.swing.text.MaskFormatter;

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
                + "e.g. 01.02.0A.AB.34.56.78.00");
        retval.setDragEnabled(true);
        retval.setTransferHandler(new CustomTransferHandler());
        
        return retval;
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
