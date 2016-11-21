package org.openlcb.cdi.swing;

import javax.swing.JFrame;

import org.mockito.Mockito;
import org.openlcb.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.*;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.openlcb.cdi.impl.ConfigRepresentation;
import org.openlcb.cdi.jdom.JdomCdiRep;
import org.openlcb.cdi.jdom.SampleFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.openlcb.cdi.impl.DemoReadWriteAccess.demoRepFromFile;
import static org.openlcb.cdi.impl.DemoReadWriteAccess.demoRepFromSample;

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

        m.initComponents(demoRepFromSample(org.openlcb.cdi.jdom.SampleFactory.getBasicSample()),
                new CdiPanel.GuiItemFactory() {
                    public JButton handleReadButton(JButton button) {
                        //System.out.println("process button");
                        button.setBorder(BorderFactory.createLineBorder(java.awt.Color.yellow));
                        return button;
                }
            }
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

        m.initComponents(demoRepFromFile(new File("NMRAnetDatabaseTrainNode.xml")),
                new CdiPanel.GuiItemFactory() {
                    public JButton handleReadButton(JButton button) {
                        //System.out.println("process button");
                        button.setBorder(BorderFactory.createLineBorder(java.awt.Color.yellow));
                        return button;
                }
            }
        );

        f.add( m );

        // show
        f.pack();
        f.setVisible(true);        
    }

    public void testOffsets() {
      /* Annotated CDI:
<cdi>
  <segment space="13" origin="132">
    <int size="2" offset="21" />  // 153
    <eventid offset="3" />  // 158
    <group offset="1" />
    <int size="1" />  // 167
    <group replication="2" offset="11">  // 179 is first, 206 is second
      <int size="2" offset="3" />  // 182, 209
      <group offset="-5" />  // 179 is first
      <group replication="3">  // 179 is first-first, 209 is second-first
        <string size="9" /> // 179, 188, 197, 206, 215, 224
      </group>
    </group>
    <int size="2" offset="21" />  // 254
  </segment>
  <segment space="14" origin="0">
    <int size="2" />
  </segment>
</cdi>

       */
        CdiPanel m = new CdiPanel();

        final ArrayList<JButton> readButtons = new ArrayList<>();

        CdiPanel.ReadWriteAccess access = mock(CdiPanel.ReadWriteAccess.class);
        ConfigRepresentation rep = new ConfigRepresentation(access, new JdomCdiRep(
                SampleFactory.getOffsetSample()));
        m.initComponents(rep, new CdiPanel.GuiItemFactory() {
                    public JButton handleReadButton(JButton button) {
                        readButtons.add(button);
                        return button;
                    }});

        Mockito.reset(access);

        final int[][] readOffsets = {
                {153, 2, 13},
                {158, 8, 13},
                {167, 1, 13},
                {182, 2, 13},
                {179, 9, 13},
                {188, 9, 13},
                {197, 9, 13},
                {209, 2, 13},
                {206, 9, 13},
                {215, 9, 13},
                {224, 9, 13},
                {254, 2, 13},
                {0, 2, 14}
        };
        for (int i = 0; i < readButtons.size() && i < readOffsets.length; ++i) {
            readButtons.get(i).doClick();
            verify(access).doRead(eq((long)readOffsets[i][0]), eq(readOffsets[i][2]), eq
                    (readOffsets[i][1]), any());
            verifyNoMoreInteractions(access);
        }
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
