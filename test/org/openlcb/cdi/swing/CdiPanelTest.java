package org.openlcb.cdi.swing;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.mockito.Mockito;
import org.openlcb.cdi.impl.ConfigRepresentation;
import org.openlcb.cdi.impl.DemoReadWriteAccess;
import org.openlcb.cdi.jdom.JdomCdiRep;
import org.openlcb.cdi.jdom.SampleFactory;

import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
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

        DemoReadWriteAccess access = spy(new DemoReadWriteAccess());
        ConfigRepresentation rep = new ConfigRepresentation(access, new JdomCdiRep(
                SampleFactory.getOffsetSample()));
        m.initComponents(rep, new CdiPanel.GuiItemFactory() {
                    public JButton handleReadButton(JButton button) {
                        readButtons.add(button);
                        return button;
                    }});

        final int[][] preloadOffsets = {
                {0, 2, 14},
                {153, 167 + 1 - 153, 13},
                {179, 224 + 9 - 179, 13},
                {254, 2, 13},
        };
        for (int i = 0; i < preloadOffsets.length; ++i) {
            verify(access).doRead(eq((long) preloadOffsets[i][0]), eq(preloadOffsets[i][2]), eq
                    (preloadOffsets[i][1]), any());
        }
        verifyNoMoreInteractions(access);

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
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CdiPanelTest.class);
        return suite;
    }
}
