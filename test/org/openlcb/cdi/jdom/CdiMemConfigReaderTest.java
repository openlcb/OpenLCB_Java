package org.openlcb.cdi.jdom;

import org.openlcb.*;
import org.openlcb.implementations.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;
/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision: 34 $
 */
public class CdiMemConfigReaderTest extends TestCase {

    NodeID nidHere = new NodeID(new byte[]{0,0,0,0,0,1});
    NodeID nidThere = new NodeID(new byte[]{0,0,0,0,0,2});
    
    Connection connection = new AbstractConnection() {
        public void put(Message msg, Connection sender) {}
    };
    
    MimicNodeStore store;
    MemoryConfigurationService service;
    int spaceCount;
    
    DatagramService dgs;
    String testString = "string to be checked which involves more than 64 characters, so its three datagrams at least, because two originally worked and perhaps three did not ";
    byte[] content;
    
    public void setUp() throws Exception {
        store = new MimicNodeStore(connection, nidHere);
        dgs = new DatagramService(null, null);
        
        store.addNode(nidThere);
        
        spaceCount = 0; // number sent so far
        content = testString.getBytes();
        content[content.length-1] = 0;
        
        service = new MemoryConfigurationService(nidHere, dgs) {
            public void request(MemoryConfigurationService.McsWriteMemo memo) {
            }
        
            public void request(MemoryConfigurationService.McsReadMemo memo) {
                if ( spaceCount*64 >= content.length) return; // done

                int space = 0xFD;
                long address = 0;
                byte[] data = new byte[Math.min(CdiMemConfigReader.LENGTH, content.length - CdiMemConfigReader.LENGTH*spaceCount)];
                                
                for (int i = 0; (i<CdiMemConfigReader.LENGTH) && ((i+spaceCount*CdiMemConfigReader.LENGTH)<content.length); i++) 
                    data[i] = content[i+spaceCount*CdiMemConfigReader.LENGTH];
                
                spaceCount++;
                memo.handleReadData(nidThere, space, address, data);
            }
        
            public void request(MemoryConfigurationService.McsConfigMemo memo) {
            }
            
            public void request(MemoryConfigurationService.McsAddrSpaceMemo memo) {
            }
        };
        
    }
    
    public void tearDown() {
    }
            
    public void testSetup() {
        // just calls setup & teardown
    }
           
    public void testCtor() {
        CdiMemConfigReader cmcr = new CdiMemConfigReader(nidHere, store, service);   
    }
    java.io.Reader rdr;
    public void testCycle() throws java.io.IOException {
        CdiMemConfigReader cmcr = new CdiMemConfigReader(nidHere, store, service); 
        CdiMemConfigReader.ReaderAccess a = new CdiMemConfigReader.ReaderAccess() {
            public void provideReader(java.io.Reader r) {
                rdr = r;
            }
        };
        
        rdr = null;
        cmcr.startLoadReader(a);
        Assert.assertTrue(rdr != null);
        Assert.assertEquals("1", testString.getBytes()[0], rdr.read());
        Assert.assertEquals("2", testString.getBytes()[1], rdr.read());
        int count = 2;
        while (rdr.read() > 0) count++;
        Assert.assertEquals("length", testString.length()-1, count); // -1 for trailing zero on input
    }
           
    
    // from here down is testing infrastructure
    
    public CdiMemConfigReaderTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {CdiMemConfigReaderTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CdiMemConfigReaderTest.class);
        return suite;
    }
}
