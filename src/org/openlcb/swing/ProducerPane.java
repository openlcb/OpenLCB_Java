// ProducerPane.java

package org.openlcb.swing;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.openlcb.AbstractConnection;
import org.openlcb.Connection;
import org.openlcb.Message;
import org.openlcb.implementations.SingleProducerNode;

/**
 * Pane provides simple GUI for producer: A button.
 *
 * @author	Bob Jacobsen   Copyright (C) 2009
 * @version	$Revision$
 */
public class ProducerPane extends JPanel {
    /** Comment for <code>serialVersionUID</code>. */
    private static final long serialVersionUID = 3746472517189015417L;

    private static final Logger logger = Logger.getLogger(ProducerPane.class.getName());

    public ProducerPane(String name, SingleProducerNode node) {
        this.node = node;

        this.name = name;
        if (name != null) {
            sendButton.setText(name);
        } else {
            sendButton.setText(node.getEventID().toString());
        }

        sendButton.setVisible(true);
        sendButton.setToolTipText("Click to fire event");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add items to GUI
        add(sendButton);

        // connect actions to buttons
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });

        // listen to node for eventID change
        node.addPropertyChangeListener(new java.beans.PropertyChangeListener(){
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("EventID")) {
                    if (ProducerPane.this.name == null) {
                        sendButton.setText(e.getNewValue().toString());
                    }
                    logger.log(Level.FINE, "new {0}", e.getNewValue());
                }
            }
        });
    }
 
    String name;
    protected JButton sendButton = new JButton();
    protected SingleProducerNode node;
    
    public synchronized void sendButtonActionPerformed(ActionEvent e) {
        node.send();
    }
	
	public Connection getConnection(){
	    return new InputLink();
	}
	
	/**
	 * Captive class to capture data.
	 * <p>
	 * Not a node by itself, this just listens to a Connection.
	 * <p>
	 * This implementation doesn't distinguish the source of a message, but it could.
	 */
	class InputLink extends AbstractConnection {
	    public InputLink() { }
	    
	    @Override
        public void put(Message msg, Connection sender) { }
	}
}
