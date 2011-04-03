package scenarios;

import javax.swing.*;
import java.awt.event.*;

/**
 * Simple interface to select which scenario to run
 *
 * @author  Bob Jacobsen   Copyright 2010
 * @version $Revision$
 */
public class ScenarioRunner  {

    // Main entry point
    static public void main(String[] args) {
    
        JFrame f = new JFrame("OpenLCB Demo Selector");
        
        JPanel p = new JPanel();
        f.add(p);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        
        p.add(new JLabel("Click a button to start the"));
        p.add(new JLabel("corresponding OpenLCB demo."));
        p.add(new JLabel(""));
        JButton b;
        b = new JButton("Configuration Tool Demo");
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
                    scenarios.ConfigDemoApplet.main((String[])null);
                } catch (Exception ex) {ex.printStackTrace();}
            }
        });
        p.add(b);
        b = new JButton("Blue/Gold Configuration Demo");
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
                    scenarios.BlueGoldCheck.main((String[])null);
                } catch (Exception ex) {ex.printStackTrace();}
            }
        });
        p.add(b);
        
        f.pack();
        f.setVisible(true);
    }

}
