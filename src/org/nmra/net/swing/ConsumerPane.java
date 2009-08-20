// ConsumerPane.java

package org.nmra.net.swing;

import javax.swing.*;
import javax.swing.text.*;

import org.nmra.net.*;

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
    
    public ConsumerPane(String name) throws Exception {
        sendButton.setText(name);
        sendButton.setVisible(true);
        sendButton.setToolTipText("Button shows event");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add items to GUI
        add(sendButton);

        // connect actions to buttons
        sendButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });

        timer.setRepeats(false);

    }
    
    protected JButton sendButton = new JButton();

    public synchronized void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
    }
	
	public Connection getConnection(){ return new InputLink(); }
	
	javax.swing.Timer timer = new javax.swing.Timer(DELAY, 
	        new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendButton.setSelected(false);
                    }
                });

	
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
            sendButton.setSelected(true);
            timer.start();
	    }
	}
	
}
