// ProducerPane.java

package org.nmra.net.swing;

import javax.swing.*;
import javax.swing.text.*;

import org.nmra.net.*;
import org.nmra.net.implementations.*;

/**
 * Pane provides simple GUI for producer: A button.
 *
 * @author	Bob Jacobsen   Copyright (C) 2009
 * @version	$Revision$
 */
public class ProducerPane extends JPanel  {

    public ProducerPane(String name, SingleProducerNode node) throws Exception {
        this.node = node;

        sendButton.setText(name);
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
    }
 
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
