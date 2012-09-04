// MemConfigDescriptionPane.java

package org.openlcb.swing.memconfig;

import javax.swing.*;
import javax.swing.text.*;
import java.beans.PropertyChangeListener;

import org.openlcb.*;
import org.openlcb.Utilities;
import org.openlcb.implementations.*;

/**
 * Display the node's memory configuration capabilities
 *
 * @author	Bob Jacobsen   Copyright (C) 2012
 * @version	$Revision$
 */
public class MemConfigDescriptionPane extends JPanel  {
    
    NodeID node;
    MimicNodeStore store;
    MemoryConfigurationService service;
    
    JLabel commandLabel = new JLabel("       ");
    JLabel highSpaceLabel = new JLabel("       ");
    JLabel lowSpaceLabel = new JLabel("       ");
    
    public MemConfigDescriptionPane(NodeID node, MimicNodeStore store, MemoryConfigurationService service) {
        this.node = node;
        this.store = store;
        this.service = service;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        addLine(commandLabel, "Commands:");
        addLine(highSpaceLabel,"High Address Space:");
        addLine(lowSpaceLabel, "Low Address Space:");
        add(new JSeparator());
    }
    
    void addLine(JComponent j, String name) {
        JPanel p = new JPanel();
        p.setLayout(new java.awt.FlowLayout());
        p.add(new JLabel(name));
        p.add(j);
        add(p);
    }
    
    /**
     * To be invoked after Swing component installation is complete,
     * as it drives display changes.
     */
    public void initComponents() {
        // start by asking for basic config
        MemoryConfigurationService.McsConfigMemo memo = 
            new MemoryConfigurationService.McsConfigMemo(node) {
                public void handleConfigData(NodeID dest, int commands, int lengths, int highSpace, int lowSpace, String name) { 
                    // fill window from values
                    commandLabel.setText("0x"+Utilities.toHexPair(commands>>8)+Utilities.toHexPair(commands));
                    
                    highSpaceLabel.setText("0x"+Utilities.toHexPair(highSpace));
                    lowSpaceLabel.setText("0x"+Utilities.toHexPair(lowSpace));
                    
                    // and start address space read
                    readSpace(dest, highSpace, lowSpace);
                }
            };
        service.request(memo);

    }
    
    void readSpace(NodeID dest, final int highSpace, final int lowSpace) {
        if (highSpace < lowSpace) {
            // done, no further reads
            // force a layout
            revalidate();
            if (getTopLevelAncestor() instanceof JFrame) ((JFrame)getTopLevelAncestor()).pack();
            return;
        }
        MemoryConfigurationService.McsAddrSpaceMemo memo = 
            new MemoryConfigurationService.McsAddrSpaceMemo(node, highSpace) {
                public void handleAddrSpaceData(NodeID dest, int space, long hiAddress, long lowAddress, int flags, String desc) { 
                    // new line with values
                    JPanel p = new JPanel();
                    p.setLayout(new java.awt.FlowLayout());
                    MemConfigDescriptionPane.this.add(p);
                    p.add(new JLabel("Space: 0x"+Utilities.toHexPair(space)));
                    p.add(new JLabel("High address: 0x"+Long.toHexString(hiAddress).toUpperCase()));
                    // and read next space
                    readSpace(dest, highSpace-1, lowSpace);
                }
            };
        service.request(memo);        
    }
    
}
