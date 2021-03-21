// ConsumerPane.java

package org.openlcb.swing;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openlcb.implementations.SingleConsumerNode;

/**
 * Pane provides simple GUI for consumer: A button.
 * <p>
 * Connection to listener is handled outside this.
 *
 * @author	Bob Jacobsen   Copyright (C) 2009
 * @version	$Revision$
 */
public class ConsumerPane extends JPanel  {
    /** Comment for <code>serialVersionUID</code>. */
    private static final long serialVersionUID = 4037237914141482534L;

    static final int DELAY = 2000;
    private static final Logger logger = Logger.getLogger(ConsumerPane.class.getName());
    
    public ConsumerPane(String name, SingleConsumerNode node) {
        this.name = name;
        if (name != null) {
            sendLabel.setText(name);
        } else {
            sendLabel.setText(node.getEventID().toString());
        }
        
        sendLabel.setVisible(true);
        sendLabel.setOpaque(true);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add items to GUI
        add(sendLabel);

        timer.setRepeats(false);
        
        // listen to node for event
        node.addPropertyChangeListener(new java.beans.PropertyChangeListener(){
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("Event")) {
                    sendLabel.setBackground(java.awt.Color.gray);
                    sendLabel.repaint();
                    timer.start();
                } else if (e.getPropertyName().equals("EventID")) {
                    if (ConsumerPane.this.name == null) {
                        sendLabel.setText(e.getNewValue().toString());
                    }
                    logger.log(Level.FINE, "new {0}", e.getNewValue());
                }
            }
        });
    }
    
    String name;
    protected JLabel sendLabel = new JLabel();
		
	javax.swing.Timer timer = new javax.swing.Timer(DELAY, 
            new java.awt.event.ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            sendLabel.setBackground(new JLabel().getBackground());
            sendLabel.repaint();
        }
    }); 
}
