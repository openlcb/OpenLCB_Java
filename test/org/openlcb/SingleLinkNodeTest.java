package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class SingleLinkNodeTest extends TestCase {
    
    boolean result;
    
    NodeID nodeID = new NodeID(new byte[]{1,2,3,4,5,6});
    
    public void testInitialization() {
        result = false;
        Connection testConnection = new AbstractConnection(){
            public void put(Message msg, Connection node) {
                if (msg.equals(new InitializationCompleteMessage(nodeID)))
                    result = true;
                else
                    Assert.fail("Wrong message: "+msg);
            }
        };
        SingleLinkNode node = new SingleLinkNode(
                                            nodeID,
                                            testConnection);
                                            
        node.initialize();
        
        Assert.assertTrue(result);
    }
    
    // from here down is testing infrastructure
    
    public SingleLinkNodeTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SingleLinkNodeTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SingleLinkNodeTest.class);
        return suite;
    }
}
