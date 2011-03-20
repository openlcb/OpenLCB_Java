package org.openlcb.cdi.swing;

import javax.swing.*;
import java.awt.*;

import org.openlcb.cdi.CdiRep;

/**
 * Simple example CDI display.
 *
 * Works with a CDI reader.
 *
 * @author  Bob Jacobsen   Copyright 2011
 * @version $Revision: -1 $
 */
public class CdiPanel extends JPanel {

    public CdiPanel () { super(); }
    
    public void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }
    
    public void loadCDI(CdiRep c) {
        add(createIdentificationPane(c));
    }

    JPanel createIdentificationPane(CdiRep c) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createTitledBorder("Identification"));
        
        JPanel p1;
        
        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p.add(p1);
        p1.add(new JLabel("Manufacturer: "));
        p1.add(new JLabel(c.getIdentification().getManufacturer()));
        
        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p.add(p1);
        p1.add(new JLabel("Model: "));
        p1.add(new JLabel(c.getIdentification().getModel()));
        
        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p.add(p1);
        p1.add(new JLabel("Hardware Version: "));
        p1.add(new JLabel(c.getIdentification().getHardwareVersion()));
        
        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p.add(p1);
        p1.add(new JLabel("Software Version: "));
        p1.add(new JLabel(c.getIdentification().getSoftwareVersion()));
        
        // include map if present
        
        CdiRep.Map map = c.getIdentification().getMap();
        if (map != null) {
            java.util.List keys = map.getKeys();
            for (int i = 0; i<keys.size(); i++) {
                String key = (String)keys.get(i);

                p1 = new JPanel();
                p1.setLayout(new FlowLayout());
                p.add(p1);
                p1.add(new JLabel(key+": "));
                p1.add(new JLabel(map.getEntry(key)));
                
            }
        }
        
        return p;
    }
}
