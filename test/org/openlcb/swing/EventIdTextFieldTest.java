package org.openlcb.swing;

import org.openlcb.*;
import org.openlcb.implementations.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;
import javax.swing.text.*;

/**
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class EventIdTextFieldTest extends TestCase {
    
    JFrame frame;
    
    public void setUp() throws Exception {
        
        frame = new JFrame();
        frame.setTitle("EventIdTextField Test");
        
    }
    
    public void tearDown() {
    }
            
    public void testVisibleVersion() {
        JFormattedTextField p = EventIdTextField.getEventIdTextField();

        frame.add( p );

        frame.pack();
        frame.setVisible(true);

        Assert.assertEquals("00.00.00.00.00.00.00.00", p.getValue());
        
    }
   
    // from here down is testing infrastructure
    
    public EventIdTextFieldTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {EventIdTextFieldTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EventIdTextFieldTest.class);
        return suite;
    }
}
