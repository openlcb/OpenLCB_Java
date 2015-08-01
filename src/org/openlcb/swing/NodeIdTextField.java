// NodeIdTextField.java

package org.openlcb.swing;

import javax.swing.*;
import javax.swing.text.*;

import org.openlcb.*;

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

    static public JFormattedTextField getNodeIdTextField() {
        JFormattedTextField retval = new JFormattedTextField(createFormatter("HH.HH.HH.HH.HH.HH"));
        
        retval.setValue("00.00.00.00.00.00");
        retval.setToolTipText("NodeID as six-byte dotted-hex string, e.g. 01.02.0A.AB.34.56");
          
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

}
