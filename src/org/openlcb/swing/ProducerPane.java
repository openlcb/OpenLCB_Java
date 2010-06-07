// ProducerPane.java

package org.openlcb.swing;

import javax.swing.*;
import javax.swing.text.*;

import org.openlcb.*;
import org.openlcb.implementations.*;

/**
 * Pane provides simple GUI for producer: A button.
 *
 * @author	Bob Jacobsen   Copyright (C) 2009
 * @version	$Revision$
 */
public class ProducerPane extends JPanel  {

    public ProducerPane(String name, SingleProducerNode node) {
        this.node = node;

        this.name = name;
        if (name != null) sendButton.setText(name);
        else sendButton.setText(node.getEventID().toString());

        sendButton.setVisible(true);
        sendButton.setToolTipText("Click to fire event");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add items to GUI
        add(sendButton);

        // connect actions to buttons
        sendButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });

        // listen to node for eventID change
        node.addPropertyChangeListener(new java.beans.PropertyChangeListener(){
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("EventID")) {
                    if (ProducerPane.this.name == null) sendButton.setText(e.getNewValue().toString());
                    System.out.println("new "+e.getNewValue().toString());
                }
            }
        });
    }
 
    String name;
    protected JButton sendButton = new JButton();
    protected SingleProducerNode node;
    
    public synchronized void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        node.send();
    }
	
	public Connection getConnection(){ return new InputLink(); }
	
	/** Captive class to capture data.
	 * <p>
	 * Not a node by itself, this just listens to a Connection.
	 * <p>
	 * This implementation doesn't distinguish the source of a message, but it could.
	 */
	class InputLink implements Connection {
	    public InputLink() {
	    }
	    
	    public void put(Message msg, Connection sender) {
	    }
	}
	
}
