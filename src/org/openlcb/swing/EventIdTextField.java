// EventIdTextField.java

package org.openlcb.swing;

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
