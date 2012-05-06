package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class ConfigurationPortalTest extends TestCase {
    boolean result;
    
    NodeID nodeID1 = new NodeID(new byte[]{1,2,3,4,5,6});

    Connection connection = new AbstractConnection(){
        public void put(Message m, Connection node) {
            msg = m;
        }
    };
    
    Message msg = null;
    
    ConfigurationPortal portal = new ConfigurationPortal(nodeID1, connection);
    
    public void setup() {
        msg = null;
    }
    
    public void testCtor() {
        
    }

    public void testRequestWrite() {
    }

    public void testHandling() {
        result = false;
        Node n = new Node(){
            @Override
            public void handleProtocolIdentificationRequest(ProtocolIdentificationRequestMessage msg, Connection sender){
                result = true;
            }
        };
        Message m = new ProtocolIdentificationRequestMessage(nodeID1);
        
        n.put(m, null);
        
        Assert.assertTrue(result);
    }
    
    // from here down is testing infrastructure
    
    public ConfigurationPortalTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ConfigurationPortalTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ConfigurationPortalTest.class);
        return suite;
    }
}
