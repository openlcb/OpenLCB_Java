// MemConfigReadWritePane.java

package org.openlcb.swing.memconfig;

import javax.swing.*;
import javax.swing.text.*;
import java.beans.PropertyChangeListener;
import java.awt.FlowLayout;

import org.openlcb.*;
import org.openlcb.implementations.*;

/**
 * Provide read/write access to a node
 *
 * @author	Bob Jacobsen   Copyright (C) 2012
 * @version	$Revision$
 */
public class MemConfigReadWritePane extends JPanel  {
    
    NodeID node;
    MimicNodeStore store;
    MemoryConfigurationService service;

    public MemConfigReadWritePane(NodeID node, MimicNodeStore store, MemoryConfigurationService service) {
        this.node = node;
        this.store = store;
        this.service = service;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }
    
    void addLine(JComponent j, String name) {
        JPanel p = new JPanel();
        p.setLayout(new java.awt.FlowLayout());
        p.add(new JLabel(name));
        p.add(j);
        add(p);
    }
    
    JTextField readDataField = new JTextField(80);
    JTextField writeDataField = new JTextField(80);
    JTextField configNumberField = new JTextField("40");
    JTextField configAddressField = new JTextField("000000");
    JComboBox addrSpace = new JComboBox(new String[]{"CDI", "All", "Config", "None"});

    /**
     * To be invoked after Swing component installation is complete,
     * as it drives display changes.
     */
    public void initComponents() {
        addLine(readDataField, "Read: ");
        addLine(writeDataField, "Write: ");
        addLine(configNumberField, "Count: ");
        addLine(configAddressField, "Address: ");
        addLine(addrSpace, "Space: ");
                
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        
        JButton b = new JButton("Read");
        b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                         readPerformed();
                    }
                });
        p.add(b); 
        b = new JButton("Write");
        b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                         writePerformed();
                    }
                });
        p.add(b); 
        add(p);
        
    }
    
    public void readPerformed() {
        int space = 0xFF - addrSpace.getSelectedIndex();
        long addr = Integer.parseInt(configAddressField.getText(), 16);
        int length = Integer.parseInt(configNumberField.getText());
        service.request(new MemoryConfigurationService.McsReadMemo(node,space,addr,length){
            public void handleReadData(NodeID dest, int space, long address, byte[] data) { 
                readDataField.setText(org.openlcb.Utilities.toHexSpaceString(data));
            }
        });
    }

    public void writePerformed() {
        int space = 0xFF - addrSpace.getSelectedIndex();
        long addr = Integer.parseInt(configAddressField.getText(), 16);
        byte[] content = org.openlcb.Utilities.bytesFromHexString(writeDataField.getText());
        service.request(new MemoryConfigurationService.McsWriteMemo(node,space,addr,content));
    }
    
}
