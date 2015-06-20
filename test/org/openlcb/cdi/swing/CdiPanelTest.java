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
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision: 2175 $
 */
public class CdiPanelTest extends TestCase {
    
    // from here down is testing infrastructure
    
    public CdiPanelTest(String s) {
        super(s);
    }

    public void testDisplay() {
        JFrame f = new JFrame();
        f.setTitle("Configuration Demonstration");
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
        m.loadCDI(
            new org.openlcb.cdi.jdom.JdomCdiRep(
                org.openlcb.cdi.jdom.SampleFactory.getBasicSample()
            )
        );
        
        f.add( m );

        // show
        f.pack();
        f.setVisible(true);        
    }
    
    public void testLocoCdiDisplay() {
        JFrame f = new JFrame();
        f.setTitle("Locomotive CDI Demonstration");
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
        Element root = null;
        try {
            SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", false);  // argument controls validation
            Document doc = builder.build(new BufferedInputStream(new FileInputStream(new File("NMRAnetDatabaseTrainNode.xml"))));
            root = doc.getRootElement();
        } catch (Exception e) { System.out.println("While reading file: "+e);}
        
        m.loadCDI(
            new org.openlcb.cdi.jdom.JdomCdiRep(root)
        );
        
        f.add( m );

        // show
        f.pack();
        f.setVisible(true);        
    }
    
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {CdiPanelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CdiPanelTest.class);
        return suite;
    }
}
