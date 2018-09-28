package org.openlcb.swing;

import org.openlcb.*;
import org.openlcb.implementations.*;

import org.junit.*;

import javax.swing.*;
import javax.swing.text.*;

/**
 *
 * @author  Bob Jacobsen   Copyright 2012
 */
public class EventIdTextFieldTest  {
    
    JFrame frame;
 
    @Before   
    public void setUp() throws Exception {
        
        frame = new JFrame();
        frame.setTitle("EventIdTextField Test");
        
    }
   
    @After 
    public void tearDown() {
	frame.setVisible(false);
	frame.dispose();
        frame = null;
    }
    
    @Test    
    public void testVisibleVersion() {
        JFormattedTextField p = EventIdTextField.getEventIdTextField();

        frame.add( p );

        frame.pack();
        frame.setVisible(true);

        Assert.assertEquals("00.00.00.00.00.00.00.00", p.getValue());
        
    }
}
