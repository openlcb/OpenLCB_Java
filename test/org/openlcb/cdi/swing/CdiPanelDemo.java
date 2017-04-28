package org.openlcb.cdi.swing;

import javax.swing.JFrame;

import java.io.*;
import javax.swing.*;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.openlcb.cdi.impl.ConfigRepresentation;
import org.openlcb.cdi.impl.DemoReadWriteAccess;
import org.openlcb.cdi.jdom.JdomCdiRep;
import org.openlcb.implementations.EventTable;

import static org.openlcb.cdi.impl.DemoReadWriteAccess.demoRepFromFile;

/**
 * Provide some demo &amp; development tools for CDI
 *
 * @author  Bob Jacobsen   Copyright 2012, 2015
 * @version $Revision: 2175 $
 */
public class CdiPanelDemo {
        
    public CdiPanelDemo() {
    }

    public void displayFile(String fileName) {
        JFrame f = new JFrame();
        CdiPanel m = new CdiPanel();
        EventTable t = new EventTable();
        File file;
        if (fileName == null) {
            // find file & load file
            fci.setDialogTitle("Find desired script file");
            fci.rescanCurrentDirectory();

            int retVal = fci.showOpenDialog(null);
            // handle selection or cancel
            if (retVal != JFileChooser.APPROVE_OPTION) {
                // Run the script from it's filename
                System.out.println("No file selectedd");
            }
            file = fci.getSelectedFile();
        } else {
            file = new File(fileName);
        }
        f.setTitle(file.getName());

        ConfigRepresentation configRep = demoRepFromFile(file);
        m.setEventTable("Demo node", t);
        m.initComponents(configRep,
                new CdiPanel.GuiItemFactory() {
                    public JButton handleReadButton(JButton button) {
                        //System.out.println("process button");
                        button.setBorder(BorderFactory.createLineBorder(java.awt.Color.yellow));
                        return button;
                    }
                }
        );

        f.add(m);

        // show
        f.pack();
        f.setVisible(true);        
    }
    
    /**
     * We always use the same file chooser in this class, so that the user's
     * last-accessed directory remains available.
     */
    static JFileChooser fci = new JFileChooser();

    // Main entry point
    static public void main(String[] args) {
        CdiPanelDemo d = new CdiPanelDemo();
        String fname = null;
        if (args.length > 0) {
            fname = args[0];
            System.out.println("Using input file " + fname);
        }
        d.displayFile(fname);
    }
}
