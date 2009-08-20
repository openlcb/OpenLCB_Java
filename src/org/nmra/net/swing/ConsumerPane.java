// ConsumerPane.java

package org.nmra.net.swing;

import javax.swing.*;
import javax.swing.text.*;

import org.nmra.net.*;
import org.nmra.net.implementations.*;

/**
 * Pane provides simple GUI for consumer: A button.
 * <p>
 * Connection to listener is handled outside this.
 *
 * @author	Bob Jacobsen   Copyright (C) 2009
 * @version	$Revision$
 */
public class ConsumerPane extends JPanel  {

    final static int DELAY = 2000;
    
    public ConsumerPane(String name, SingleConsumerNode node) throws Exception {
        this.name = name;
        if (name != null) sendLabel.setText(name);
        else sendLabel.setText(node.getEventID().toString());
        
        sendLabel.setVisible(true);
        sendLabel.setOpaque(true);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add items to GUI
        add(sendLabel);

        timer.setRepeats(false);
        
        // listen to node for event
        node.addPropertyChangeListener(new java.beans.PropertyChangeListener(){
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("Event")) {
                    sendLabel.setBackground(java.awt.Color.gray);
                    sendLabel.repaint();
                    timer.start();
                } else if (e.getPropertyName().equals("EventID")) {
                    if (ConsumerPane.this.name == null) sendLabel.setText(e.getNewValue().toString());
                    System.out.println("new "+e.getNewValue().toString());
                }
            }
        });

    }
    
    String name;
    protected JLabel sendLabel = new JLabel();
		
	javax.swing.Timer timer = new javax.swing.Timer(DELAY, 
	        new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendLabel.setBackground(new JLabel().getBackground());
                        sendLabel.repaint();
                    }
                });	
}
