package org.openlcb.cdi.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.nio.charset.Charset;
import javax.swing.*;
import org.openlcb.cdi.CdiRep;
import org.openlcb.swing.EventIdTextField;

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
    
    /**
     * @param accessor Provides access for read, write operations back to layout
     * @param factory Implements hooks for optional interface elements
     */
    public void initComponents(ReadWriteAccess accessor, GuiItemFactory factory) {
        initComponents(accessor);
        this.factory = new GuiItemFactory();
    }

    /**
     * @param accessor Provides access for read, write operations back to layout
     */
    public void initComponents(ReadWriteAccess accessor) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.accessor = accessor;
    }
    
    ReadWriteAccess accessor;
    GuiItemFactory factory;
    
    public void loadCDI(CdiRep c) {
        add(createIdentificationPane(c));
        
        java.util.List<CdiRep.Segment> segments = c.getSegments();
        for (int i=0; i<segments.size(); i++) {
            add(createSegmentPane(segments.get(i)));
        }
        
        // add glue at bottom
        add(Box.createVerticalGlue());
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
            if (keys.isEmpty()) return null;

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

        long origin = item.getOrigin();
        int space = item.getSpace();
        
        // find and process items
        java.util.List<CdiRep.Item> items = item.getItems();
        if (items != null) {
             DisplayPane pane = null; 
              
             for (int i=0; i<items.size(); i++) {
                CdiRep.Item it = (CdiRep.Item) items.get(i);
                
                origin = origin +it.getOffset();
 
                 if (it instanceof CdiRep.Group) {
                     pane = createGroupPane((CdiRep.Group) it, origin, space);
                 } else if (it instanceof CdiRep.BitRep) {
                     pane = createBitPane((CdiRep.BitRep) it, origin, space);
                 } else if (it instanceof CdiRep.IntegerRep) {
                     pane = createIntPane((CdiRep.IntegerRep) it, origin, space);
                 } else if (it instanceof CdiRep.EventID) {
                     pane = createEventIdPane((CdiRep.EventID) it, origin, space);
                 } else if (it instanceof CdiRep.StringRep) {
                     pane = createStringPane((CdiRep.StringRep) it, origin, space);
                 }
                 if (pane != null) {
                     origin = pane.getOrigin() + pane.getVarSize();
                     p.add(pane);
                 } else {
                     System.out.println("could not process type of " + it);
                 }
            }
        }
        
        JPanel ret = new util.CollapsiblePanel(name, p);
        // ret.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED)); //debugging
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
    
    abstract class DisplayPane extends JPanel {
        DisplayPane(long origin, int space) {
                this.origin = origin;
                this.space = space;
        }
        int getVarSize() { return size; }
        int size;
        
        long getOrigin() { return origin; }
        long origin;
        
        int getVarSpace() { return space; }
        int space;
    }

    DisplayPane createGroupPane(CdiRep.Group item, long origin, int space) {
        DisplayPane ret = new GroupPane(item, origin, space);
        return ret;        
    }
    
    class GroupPane extends DisplayPane {
        GroupPane(CdiRep.Group item, long origin, int space){
            super(origin, space);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            String name = (item.getName() != null ? (item.getName()) : "Group");
            setBorder(BorderFactory.createTitledBorder(name));

            String d = item.getDescription();
            if (d != null) {
                add(createDescriptionPane(d));
            }

            // include map if present
            JPanel p2 = createPropertyPane(item.getMap());
            if (p2 != null) {
                add(p2);
            }

            // find and process items as replicated
            int rep = item.getReplication();
            if (rep == 0) {
                rep = 1;  // default
            }
            JPanel currentPane = this;
            for (int i = 0; i < rep; i++) {
                if (rep != 1) {
                    // nesting a pane
                    currentPane = new JPanel();
                    currentPane.setLayout(new BoxLayout(currentPane, BoxLayout.Y_AXIS));
                    currentPane.setAlignmentX(Component.LEFT_ALIGNMENT);
                    name = (item.getRepName() != null ? (item.getRepName()) : "Group")+" "+(i+1);
                    currentPane.setBorder(BorderFactory.createTitledBorder(name));
                    add(currentPane);
                }
                java.util.List<CdiRep.Item> items = item.getItems();
                if (items != null) {
                    for (int j = 0; j < items.size(); j++) {
                        CdiRep.Item it = (CdiRep.Item) items.get(j);
                        DisplayPane pane = null;
                        
                        origin = origin +it.getOffset();
                        size = size + it.getOffset();
                        
                        // Following code smells bad.  CdiRep is a representational
                        // class, shouldn't contain a "makeRepresentation" method,
                        // but some sort of dispatch would be better than this.
                        
                        if (it instanceof CdiRep.Group) {
                            pane = createGroupPane((CdiRep.Group) it, origin, space);
                        } else if (it instanceof CdiRep.BitRep) {
                            pane = createBitPane((CdiRep.BitRep) it, origin, space);
                        } else if (it instanceof CdiRep.IntegerRep) {
                            pane = createIntPane((CdiRep.IntegerRep) it, origin, space);
                        } else if (it instanceof CdiRep.EventID) {
                            pane = createEventIdPane((CdiRep.EventID) it, origin, space);
                        } else if (it instanceof CdiRep.StringRep) {
                            pane = createStringPane((CdiRep.StringRep) it, origin,space);
                        }
                        if (pane != null) {
                            size = size + pane.getVarSize();
                            origin = pane.getOrigin() + pane.getVarSize();
                            currentPane.add(pane);
                        } else { // pane == null, either didn't select a type or something went wrong in creation.
                            System.out.println("could not process type of " + it);
                        }
                    }
                }
            }
           
        }
        
    }
    
    DisplayPane createBitPane(CdiRep.BitRep item, long origin, int space) {
        return new BitPane(item, origin, space);
    }

    class BitPane extends DisplayPane {

        BitPane(CdiRep.BitRep item, long origin, int space) {
            super(origin, space);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            String name = (item.getName() != null ? item.getName() : "Bit");
            setBorder(BorderFactory.createTitledBorder(name));

            String d = item.getDescription();
            if (d != null) {
                add(createDescriptionPane(d));
            }

            size = item.getSize();
            
            // see if map is present
            String[] labels;
            CdiRep.Map map = item.getMap();
            if ((map != null) && (map.getKeys().size() >= 2)) {
                // first two map values are labels, must be present
                java.util.List<String> keys = map.getKeys();
                labels = new String[]{map.getEntry(keys.get(0)), map.getEntry(keys.get(1))};
            } else {
                labels = new String[]{"On", "Off"};
            }

            JPanel p3 = new JPanel();
            p3.setLayout(new FlowLayout());
            add(p3);
            p3.add(new JComboBox(labels));

            JButton b;
            b = new JButton("Read");
            final ReadReturn handler = new ReadReturn() {
                @Override
                public void returnData(byte[] data) {
                    //textField.setText(org.openlcb.Utilities.toHexDotsString(data));
                    System.err.println("Bit type not implemented yet - org.openlcb.cdi.swing.CdiPanel");
                }
            };
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    accessor.doRead(getOrigin(), getVarSpace(), 8, handler);
                    System.err.println("Bit type not implemented yet - org.openlcb.cdi.swing.CdiPanel");
                }
            });
            p3.add(b);
            b = new JButton("Write");
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    accessor.doWrite(getOrigin(), getVarSpace(), new byte[]{});
                    System.err.println("Bit type not implemented yet - org.openlcb.cdi.swing.CdiPanel");
                }
            });
            p3.add(b);
        }
    }

    DisplayPane createEventIdPane(CdiRep.EventID item, long start, int space) {
        return new EventIdPane(item,start,space);
    }

    class EventIdPane extends DisplayPane {

        JFormattedTextField textField;
        
        EventIdPane(CdiRep.EventID item, long origin, int space) {
            super(origin, space);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            String name = (item.getName() != null ? item.getName() : "EventID");
            setBorder(BorderFactory.createTitledBorder(name));

            String d = item.getDescription();
            if (d != null) {
                add(createDescriptionPane(d));
            }

            size = 8;
            
            JPanel p3 = new JPanel();
            p3.setAlignmentX(Component.LEFT_ALIGNMENT);
            p3.setLayout(new FlowLayout());
            add(p3);

            textField = EventIdTextField.getEventIdTextField();
            p3.add(textField);

            JButton b;
            b = new JButton("Read");
            final ReadReturn handler = new ReadReturn() {
                @Override
                public void returnData(byte[] data) {
                    textField.setValue(org.openlcb.Utilities.toHexDotsString(data));
                }
            };
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    accessor.doRead(getOrigin(), getVarSpace(), 8, handler);
                }
            });
            p3.add(b);
            b = new JButton("Write");
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    byte[] contents = org.openlcb.Utilities.bytesFromHexString((String)textField.getValue());
                    accessor.doWrite(getOrigin(), getVarSpace(), contents);
                 }
            });
            p3.add(b);

        }
    }
    
    DisplayPane createIntPane(CdiRep.IntegerRep item, long origin, int space) {
        return new IntPane(item, origin, space);
    }

    class IntPane extends DisplayPane {

        JTextField textField = null;
        JComboBox box = null;
        CdiRep.Map map = null;
        
        IntPane(CdiRep.IntegerRep item, long origin, int space) {
            super(origin, space);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            String name = (item.getName() != null ? item.getName() : "Integer");
            setBorder(BorderFactory.createTitledBorder(name));

            String d = item.getDescription();
            if (d != null) {
                add(createDescriptionPane(d));
            }

            size = item.getSize();
            
            JPanel p3 = new JPanel();
            p3.setAlignmentX(Component.LEFT_ALIGNMENT);
            p3.setLayout(new FlowLayout());
            add(p3);

            // see if map is present
            String[] labels;
            map = item.getMap();
            if ((map != null) && (map.getKeys().size() > 0)) {
                // map present, make selection box
                box = new JComboBox(map.getValues().toArray(new String[]{""}));
                p3.add(box);
            } else {
                // map not present, just an entry box
                textField = new JTextField(24);
                p3.add(textField);
                textField.setToolTipText("Signed integer value of up to "+size+" bytes");
            }

            JButton b;
            b = new JButton("Read");
            final ReadReturn handler = new ReadReturn() {
                @Override
                public void returnData(byte[] data) {
                    long value = 0;
                    for (int i = 0; i<data.length; i++)
                        value = (value << 8)+(data[i]&0xFF);
                    
                    if (textField != null) {
                        textField.setText(""+value);
                    } else {
                        String key = ""+value;
                        String entry = map.getEntry(key);
                        box.setSelectedItem(entry);
                    }
               }
            };
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    accessor.doRead(getOrigin(), getVarSpace(), size, handler);
                }
            });
            p3.add(b);
            b = new JButton("Write");
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    byte[] content = new byte[size];
                    long value;
                    if (textField != null) {
                        value = Long.parseLong(textField.getText());
                    } else {
                        // have to get key from stored value
                        String entry = (String) box.getSelectedItem();
                        String key = map.getKey(entry);
                        value = Long.parseLong(key);
                    }
                    int i = 0;
                    while (i<content.length) {
                        content[content.length-1-i] = (byte)(value & 0xFF); // content[0] is MSByte
                        value = value >> 8;
                        i++;
                    }
                    accessor.doWrite(getOrigin(), getVarSpace(), content);
                }
            });
            p3.add(b);
        }
    }

    DisplayPane createStringPane(CdiRep.StringRep item, long origin, int space) {
        return new StringPane(item, origin, space);
    }
    
    static final Charset UTF8 = Charset.forName("UTF8");
    
    class StringPane extends DisplayPane {
        JTextField textField;
        
        StringPane(CdiRep.StringRep item, long origin, int space) {
            super(origin, space);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            String name = (item.getName()!=null? item.getName() : "String");
            setBorder(BorderFactory.createTitledBorder(name));
        
            String d = item.getDescription();
            if (d!=null) add(createDescriptionPane(d));
        
            size = item.getSize();
            
            JPanel p3 = new JPanel();
            p3.setAlignmentX(Component.LEFT_ALIGNMENT);
            p3.setLayout(new FlowLayout());
            add(p3);

            textField = new JTextField(size);
            
            p3.add(textField);
            textField.setToolTipText("String of up to "+size+" characters");

            JButton b;
            b = new JButton("Read");
            final ReadReturn handler = new ReadReturn() {
                @Override
                public void returnData(byte[] data) {
                    textField.setText(new String(data, UTF8));
                }
            };
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    accessor.doRead(getOrigin(), getVarSpace(), size, handler);
                }
            });
            p3.add(b);
            b = new JButton("Write");
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    byte[] data = textField.getText().getBytes(UTF8);
                    byte[] content = new byte[(data.length+1 > size) ? size : data.length+1];
                    for (int i = 0; i < content.length-1; i++) {
                        content[i] = data[i];
                    }
                    content[content.length-1] = 0;

                    // write it back in case of truncation
                    byte[] writeBack = new byte[content.length-1];
                    for (int i = 0; i< writeBack.length; i++) writeBack[i] = content[i];
                    textField.setText(new String(writeBack, UTF8));
                    // and to the node
                    accessor.doWrite(getOrigin(), getVarSpace(), content);
                }
            });
            p3.add(b);
        }
     }

     /** 
      * Provide access to e.g. a MemoryConfig service.
      * 
      * Default just writes output for debug
      */
    public static class ReadWriteAccess {
        public void doWrite(long address, int space, byte[] data) {
            System.out.println("Write to "+address+" in space "+space);
        }
        public void doRead(long address, int space, int length, final ReadReturn handler) {
            System.out.println("Read from "+address+" in space "+space);
        }
    }
     
    /** 
     * Handle GUI hook requests if needed
     * 
     * Default behavior is to do nothing
     */
    public static class GuiItemFactory {
        public JButton readButton(JButton button) {
            return button;
        }
    }
     
    /**
     * Memo class for handling read-return data
     */
    abstract public class ReadReturn {
        abstract public void returnData(byte[] data);
    }
}
