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
        setText(""+initialValue);
    }

    /**
     * Constructor that sets the default value to 255
     *
     * @param initialValue value to display at start
     */
    public MemorySpaceSelector() {
        this(255);
    }

    /**
     * Get the selected value, constrained to 0-255 inclusive
     *
     */
    public int getMemorySpace() {
        int value = Integer.parseInt(getText());
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
