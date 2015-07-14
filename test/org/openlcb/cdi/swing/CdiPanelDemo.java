package org.openlcb.cdi.swing;

import javax.swing.JFrame;
import org.openlcb.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.*;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

/**
 * Provide some demo & development tools for CDI
 *
 * @author  Bob Jacobsen   Copyright 2012, 2015
 * @version $Revision: 2175 $
 */
public class CdiPanelDemo extends TestCase {
        
    public CdiPanelDemo() {
    }

    public void displayFile() {
        JFrame f = new JFrame();
        CdiPanel m = new CdiPanel();
                
        m.initComponents(new CdiPanel.ReadWriteAccess(){
                @Override
                public void doWrite(long address, int space, byte[] data) {
                        System.out.println(data.length);
                        System.out.println("write "+address+" "+space+": "+org.openlcb.Utilities.toHexDotsString(data));
                    }
                @Override
                public void doRead(long address, int space, int length, CdiPanel.ReadReturn handler) {
                        handler.returnData(new byte[]{1,2,3,4,5,6,7,8});
                        System.out.println("read "+address+" "+space);
                    }            
            },
                new CdiPanel.GuiItemFactory() {
                    public JButton handleReadButton(JButton button) {
                        //System.out.println("process button");
                        button.setBorder(BorderFactory.createLineBorder(java.awt.Color.yellow));
                        return button;
                }
            }
        );
        
        // find file & load file
        fci.setDialogTitle("Find desired script file");
        fci.rescanCurrentDirectory();

        int retVal = fci.showOpenDialog(null);
        // handle selection or cancel
        if (retVal != JFileChooser.APPROVE_OPTION) {
            File file = fci.getSelectedFile();
            // Run the script from it's filename
            System.out.println("No file selected");
        }

        f.setTitle(fci.getSelectedFile().getName());

        Element root = null;
        try {
            SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", false);  // argument controls validation
            Document doc = builder.build(new BufferedInputStream(new FileInputStream(fci.getSelectedFile())));
            root = doc.getRootElement();
        } catch (Exception e) { System.out.println("While reading file: "+e);}
        
        m.loadCDI(
            new org.openlcb.cdi.jdom.JdomCdiRep(root)
        );
        
        JScrollPane sp = new JScrollPane(m);
        f.add( sp );

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
        d.displayFile();
    }
}
