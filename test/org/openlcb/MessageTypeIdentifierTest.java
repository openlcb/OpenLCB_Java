package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class MessageTypeIdentifierTest extends TestCase {

    public void testCtor() {
        MessageTypeIdentifier mti1 = MessageTypeIdentifier.InitializationComplete;
    }

    public void testEquals() {
        MessageTypeIdentifier mti1 = MessageTypeIdentifier.InitializationComplete;
        MessageTypeIdentifier mti2 = MessageTypeIdentifier.InitializationComplete;
        Assert.assertEquals(mti1, mti2);
    }

    public void testNotEquals() {
        MessageTypeIdentifier mti1 = MessageTypeIdentifier.InitializationComplete;
        MessageTypeIdentifier mti2 = MessageTypeIdentifier.VerifyNodeIdAddressed;
        Assert.assertTrue(!mti1.equals(mti2));
    }

    public void testToString() {
        MessageTypeIdentifier mti1 = MessageTypeIdentifier.InitializationComplete;
        Assert.assertEquals(mti1.toString(), "InitializationComplete");
        
    }
    
    // from here down is testing infrastructure
    
    public MessageTypeIdentifierTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MessageTypeIdentifierTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MessageTypeIdentifierTest.class);
        return suite;
    }
}
