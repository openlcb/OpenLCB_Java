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
        
        java.util.List<CdiRep.Segment> segments = c.getSegments();
        for (int i=0; i<segments.size(); i++) {
            add(createSegmentPane(segments.get(i)));
        }
    }

    JPanel createIdentificationPane(CdiRep c) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setAlignmentY(Component.TOP_ALIGNMENT);
        //p.setBorder(BorderFactory.createTitledBorder("Identification"));

        CdiRep.Identification id = c.getIdentification();
        
        JPanel p1 = new JPanel();
        p.add(p1);
        p1.setLayout(new util.javaworld.GridLayout2(4,2));
        
        p1.add(new JLabel("Manufacturer: "));
        p1.add(new JLabel(id.getManufacturer()));
        
        p1.add(new JLabel("Model: "));
        p1.add(new JLabel(id.getModel()));
        
        p1.add(new JLabel("Hardware Version: "));
        p1.add(new JLabel(id.getHardwareVersion()));
        
        p1.add(new JLabel("Software Version: "));
        p1.add(new JLabel(id.getSoftwareVersion()));
        
        // include map if present
        JPanel p2 = createPropertyPane(id.getMap());
        if (p2!=null) p.add(p2);
        
        JPanel ret = new util.CollapsiblePanel("Identification", p);
        ret.setAlignmentY(Component.TOP_ALIGNMENT);
        return ret;
    }
    
    JPanel createPropertyPane(CdiRep.Map map) {
        if (map != null) {
            JPanel p2 = new JPanel();
            p2.setBorder(BorderFactory.createTitledBorder("Properties"));
            
            java.util.List keys = map.getKeys();
            if (keys.size() == 0) return null;

            p2.setLayout(new util.javaworld.GridLayout2(keys.size(),2));

            for (int i = 0; i<keys.size(); i++) {
                String key = (String)keys.get(i);

                p2.add(new JLabel(key+": "));
                p2.add(new JLabel(map.getEntry(key)));
                
            }
            return p2;
        } else 
            return null;
    }
    
    JPanel createSegmentPane(CdiRep.Segment item) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setAlignmentY(Component.TOP_ALIGNMENT);
        String name = "Segment"+(item.getName()!=null?(": "+item.getName()):"");
        //p.setBorder(BorderFactory.createTitledBorder(name));

        String d = item.getDescription();
        if (d!=null) p.add(createDescriptionPane(d));
        
        // include map if present
        JPanel p2 = createPropertyPane(item.getMap());
        if (p2!=null) p.add(p2);

        // find and process items
        java.util.List<CdiRep.Item> items = item.getItems();
        if (items != null) {
            for (int i=0; i<items.size(); i++) {
                CdiRep.Item it = (CdiRep.Item) items.get(i);
                if (it instanceof CdiRep.Group) p.add(createGroupPane((CdiRep.Group)it));
                else if (it instanceof CdiRep.Bit) p.add(createBitPane((CdiRep.Bit)it));
                else if (it instanceof CdiRep.Int) p.add(createIntPane((CdiRep.Int)it));
                else if (it instanceof CdiRep.EventID) p.add(createEventIdPane((CdiRep.EventID)it));
            }
        }
        
        JPanel ret = new util.CollapsiblePanel(name, p);
        ret.setAlignmentY(Component.TOP_ALIGNMENT);
        return ret;
    }

    JPanel createDescriptionPane(String d) {
        if (d == null) return null;
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextArea area = new JTextArea(d);
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        area.setFont(UIManager.getFont("Label.font"));
        area.setEditable(false);
        area.setOpaque(false);
        area.setWrapStyleWord(true); 
        area.setLineWrap(true);
        p.add(area);
        return p;
    }
    
    JPanel createGroupPane(CdiRep.Group item) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        String name = "Group"+(item.getName()!=null?(": "+item.getName()):"");
        p.setBorder(BorderFactory.createTitledBorder(name));
        
        String d = item.getDescription();
        if (d!=null) p.add(createDescriptionPane(d));
        
        // include map if present
        JPanel p2 = createPropertyPane(item.getMap());
        if (p2!=null) p.add(p2);

        // find and process items
        java.util.List<CdiRep.Item> items = item.getItems();
        if (items != null) {
            for (int i=0; i<items.size(); i++) {
                CdiRep.Item it = (CdiRep.Item) items.get(i);
                if (it instanceof CdiRep.Group) p.add(createGroupPane((CdiRep.Group)it));
                else if (it instanceof CdiRep.Bit) p.add(createBitPane((CdiRep.Bit)it));
                else if (it instanceof CdiRep.Int) p.add(createIntPane((CdiRep.Int)it));
                else if (it instanceof CdiRep.EventID) p.add(createEventIdPane((CdiRep.EventID)it));
            }
        }

        return p;
    }
    
    JPanel createBitPane(CdiRep.Bit item) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        String name = "Bit"+(item.getName()!=null?(": "+item.getName()):"");
        p.setBorder(BorderFactory.createTitledBorder(name));
        
        String d = item.getDescription();
        if (d!=null) p.add(createDescriptionPane(d));
        
        // see if map is present
        String[] labels;
        CdiRep.Map map = item.getMap();
        if ((map != null) && (map.getKeys().size()>=2)) {
            // first two map values are labels, must be present
            java.util.List<String> keys = map.getKeys();
            labels = new String[]{map.getEntry(keys.get(0)),map.getEntry(keys.get(1))};
        } else {
            labels = new String[]{"On","Off"};
        }

        JPanel p3 = new JPanel();
        p3.setLayout(new FlowLayout());
        p.add(p3);
        p3.add(new JComboBox(labels));
        p3.add(new JButton("Read"));
        p3.add(new JButton("Write"));

        return p;
    }

    JPanel createEventIdPane(CdiRep.EventID item) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        String name = "EventID"+(item.getName()!=null?(": "+item.getName()):"");
        p.setBorder(BorderFactory.createTitledBorder(name));
        
        String d = item.getDescription();
        if (d!=null) p.add(createDescriptionPane(d));
        
        JPanel p3 = new JPanel();
        p3.setAlignmentX(Component.LEFT_ALIGNMENT);
        p3.setLayout(new FlowLayout());
        p.add(p3);

        p3.add(new JTextField(24));
        
        p3.add(new JButton("Read"));
        p3.add(new JButton("Write"));

        return p;
    }

    JPanel createIntPane(CdiRep.Int item) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        String name = "Integer"+(item.getName()!=null?(": "+item.getName()):"");
        p.setBorder(BorderFactory.createTitledBorder(name));
        
        String d = item.getDescription();
        if (d!=null) p.add(createDescriptionPane(d));
        
        JPanel p3 = new JPanel();
        p3.setAlignmentX(Component.LEFT_ALIGNMENT);
        p3.setLayout(new FlowLayout());
        p.add(p3);

        // see if map is present
        String[] labels;
        CdiRep.Map map = item.getMap();
        if ((map != null) && (map.getKeys().size()>=0)) {
            // map present, make selection box
            p3.add(new JComboBox(map.getValues().toArray(new String[]{""})));
        } else {
            // map not present, just an entry box
            p3.add(new JTextField(24));
        }

        p3.add(new JButton("Read"));
        p3.add(new JButton("Write"));

        return p;
    }
}
