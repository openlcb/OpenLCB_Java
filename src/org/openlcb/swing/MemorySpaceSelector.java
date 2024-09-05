// MemorySpaceSelector.java

package org.openlcb.swing;

import java.util.logging.Logger;

import javax.swing.JTextField;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Java Swing component to select a node, populated
 * from a MimicNodeStore
 *
 * @author	Bob Jacobsen   Copyright (C) 2012
 */
public class MemorySpaceSelector extends JTextField  {


    /**
     * Constructor that allows you to set the initial displayed value
     *
     * @param initialValue value to display at start
     */
    public MemorySpaceSelector(int initialValue) {
        super();
        setText("0x"+Integer.toHexString(initialValue).toUpperCase());
    }

    /**
     * Constructor that sets the default value to 255
     */
    public MemorySpaceSelector() {
        this(255);
    }

    /**
     * Get the selected value, constrained to 0-255 inclusive
     *
     */
    public int getMemorySpace() {
        int value = 0;
        String input = getText().trim().toLowerCase();
        if (input.startsWith("0x")) {
            // hexadecimal case
            value = Integer.parseInt(input.substring(2), 16);
        } else {
            // decimal case
            value = Integer.parseInt(input);
        }
        if (value < 0 ) { 
            value = 0;
            setText(""+value);
        }
        else if (value > 255) {
            value = 255;
            setText(""+value);
        }
        return value;
    }
    
    private static final Logger log = Logger.getLogger(MemorySpaceSelector.class.getName());
}
