package scenarios;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Simple interface to select which scenario to run
 *
 * @author  Bob Jacobsen   Copyright 2010
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
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    scenarios.ConfigDemoApplet.main(new String[] {});
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        p.add(b);

        b = new JButton("Blue/Gold Configuration Demo");
        b.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    scenarios.BlueGoldCheck.main(new String[] {});
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        p.add(b);
        
        b = new JButton("Display CDI File");
        b.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    org.openlcb.cdi.swing.CdiPanelDemo.main(new String[] {});
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        p.add(b);
        
        f.pack();
        f.setVisible(true);
    }

}
