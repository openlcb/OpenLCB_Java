// MemConfigReadWritePane.java

package org.openlcb.swing.memconfig;

import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openlcb.MimicNodeStore;
import org.openlcb.NodeID;
import org.openlcb.implementations.MemoryConfigurationService;

/**
 * Provide read/write access to a node
 *
 * @author	Bob Jacobsen   Copyright (C) 2012
 * @version	$Revision$
 */
public class MemConfigReadWritePane extends JPanel {
    /** Comment for <code>serialVersionUID</code>. */
    private static final long serialVersionUID = -6666836889862724299L;
    
    NodeID node;
    MimicNodeStore store;
    MemoryConfigurationService service;

    public MemConfigReadWritePane(NodeID node, MimicNodeStore store,
            MemoryConfigurationService service) {
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
    JComboBox<String> addrSpace = new JComboBox<String>(
            new String[] {"CDI", "All", "Config", "None"});

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
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                         readPerformed();
                    }
                });
        p.add(b); 
        b = new JButton("Write");
        b.addActionListener(new java.awt.event.ActionListener() {
                    @Override
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
        service.requestRead(node, space, addr, length,
                new MemoryConfigurationService.McsReadHandler() {
            @Override
            public void handleFailure ( int code){
                readDataField.setText("Failed: 0x" + Integer.toHexString(code));
            }

            @Override
            public void handleReadData (NodeID dest,int space, long address, byte[] data){
                readDataField.setText(org.openlcb.Utilities.toHexSpaceString(data));
            }
        });
    }

    public void writePerformed() {
        int space = 0xFF - addrSpace.getSelectedIndex();
        long addr = Integer.parseInt(configAddressField.getText(), 16);
        byte[] content = org.openlcb.Utilities.bytesFromHexString(writeDataField.getText());
        service.requestWrite(node, space, addr, content,
                new MemoryConfigurationService.McsWriteHandler() {
            @Override
            public void handleFailure(int errorCode) {
                readDataField.setText("Write failed: 0x" + Integer.toHexString(errorCode));
            }

            @Override
            public void handleSuccess() {
                // ignore
            }
        });
    }
}
