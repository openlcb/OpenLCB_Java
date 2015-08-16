// EventIdTextField.java

package org.openlcb.swing;

import java.io.*;
import java.awt.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.text.*;

import org.openlcb.*;

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

    static public JFormattedTextField getEventIdTextField() {
        JFormattedTextField retval = new JFormattedTextField(createFormatter("HH.HH.HH.HH.HH.HH.HH.HH"));
        
        retval.setValue("00.00.00.00.00.00.00.00");
        retval.setToolTipText("EventID as eight-byte dotted-hex string, e.g. 01.02.0A.AB.34.56.78.00");
        retval.setDragEnabled(true);
        retval.setTransferHandler(new CustomTransferHandler());
        
        return retval;
    }
    
	static private MaskFormatter createFormatter(String s) {
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter(s);
        } catch (java.text.ParseException exc) {
            System.err.println("formatter is bad: " + exc.getMessage());
        }
        return formatter;
    }

static class CustomTransferHandler extends TransferHandler {
    
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    public Transferable createTransferable(JComponent c) {
        return new StringSelection(((JTextComponent) c).getSelectedText());
    }

    public void exportDone(JComponent c, Transferable t, int action) {
    }

    public boolean canImport(TransferSupport ts) {
        return ts.getComponent() instanceof JTextComponent;
    }

    public boolean importData(TransferSupport ts) {
        try {
            ((JTextComponent) ts.getComponent())
                .setText((String) ts
                         .getTransferable()
                         .getTransferData(DataFlavor.stringFlavor));
            return true;
        } catch(UnsupportedFlavorException e) {
            return false;
        } catch(IOException e) {
            return false;
        }
    }
}}
