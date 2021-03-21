// NodeIdTextField.java

package org.openlcb.swing;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;

/**
 * Text field for entry of forced-valid NodeID string.
 *
 * Due to class loader issues, this is constructed in library form (as a
 * static method you call to get the field) rather than a subclass.
 *
 * @author	Bob Jacobsen   Copyright (C) 2012, 2015
 * @version	$Revision$
 */
public class NodeIdTextField extends JFormattedTextField  {
    /** Comment for <code>serialVersionUID</code>. */
    private static final long serialVersionUID = -2449245636268993230L;

    private static final Logger logger = Logger.getLogger(NodeIdTextField.class.getName());

    public static JFormattedTextField getNodeIdTextField() {
        JFormattedTextField retval = new JFormattedTextField(createFormatter("HH.HH.HH.HH.HH.HH"));
        
        retval.setValue("00.00.00.00.00.00");
        retval.setToolTipText("NodeID as six-byte dotted-hex string, e.g. 01.02.0A.AB.34.56");
        retval.setDragEnabled(true);
        retval.setTransferHandler(new EventIdTextField.CustomTransferHandler());
        
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
}
