package org.openlcb.cdi.jdom;

import org.openlcb.*;
import org.openlcb.implementations.*;

import org.junit.*;

import javax.swing.*;
/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision: 34 $
 */
public class CdiMemConfigReaderTest {

    NodeID nidHere = new NodeID(new byte[]{0,0,0,0,0,1});
    NodeID nidThere = new NodeID(new byte[]{0,0,0,0,0,2});
    
    Connection connection = new AbstractConnection() {
        public void put(Message msg, Connection sender) {}
    };
    
    MimicNodeStore store;
    MemoryConfigurationService service;

    DatagramService dgs;
    byte[] content;
    
    @Before
    public void setUp() throws Exception {
        store = new MimicNodeStore(connection, nidHere);
        dgs = new DatagramService(null, null);
        
        store.addNode(nidThere);
            
    }
    
    @After
    public void tearDown() {
       store.dispose();
       service.dispose();
       store = null;
       service = null;
    }
         
    java.io.Reader rdr;
    long bytesRead;
    
    @Test
    public void testLongCycle() throws java.io.IOException {
        String testString = "string to be checked which involves more than 64 characters, so its three datagrams at least, because two originally worked and perhaps three did not ";
        
        content = testString.getBytes();
        content[content.length-1] = 0;

        // TODO: 5/2/16 replace this with a proper mock
        service = new MemoryConfigurationService(nidHere, dgs) {
            public void requestRead(NodeID dest, int space, long address, int len, McsReadHandler
                    cb) {
                byte[] data = new byte[Math.min(len, (int)(content.length - address))];
                System.arraycopy(content, (int)address, data, 0, data.length);
                cb.handleReadData(nidThere, space, address, data);
            }
        };

        CdiMemConfigReader cmcr = new CdiMemConfigReader(nidHere, store, service);
        CdiMemConfigReader.ReaderAccess a = new CdiMemConfigReader.ReaderAccess() {
            @Override
            public void progressNotify(long _bytesRead, long totalBytes) {
                bytesRead = _bytesRead;
            }

            public void provideReader(java.io.Reader r) {
                rdr = r;
            }
        };
        
        rdr = null;
        cmcr.startLoadReader(a);
        Assert.assertTrue(rdr != null);
        Assert.assertEquals("1", testString.getBytes()[0], rdr.read());
        Assert.assertEquals("2", testString.getBytes()[1], rdr.read());
        Assert.assertEquals(testString.length()-1, bytesRead);
        int count = 2;
        while (rdr.read() > 0) count++;
        Assert.assertEquals("length", testString.length()-1, count); // -1 for trailing zero on input
    }
    
    @Test
    public void test64ByteRead() throws java.io.IOException {
    
        String test64String = "1234567890123456789012345678901234567890123456789012345678901234"; // length = 64
        content = test64String.getBytes();
        content[content.length-1] = 0;

        service = new MemoryConfigurationService(nidHere, dgs) {
            public void requestRead(NodeID dest, int space, long address, int len, McsReadHandler
                    cb) {
                byte[] data = new byte[Math.min(len, (int)(content.length - address))];
                System.arraycopy(content, (int)address, data, 0, data.length);
                cb.handleReadData(nidThere, space, address, data);
            }
        };
        
        CdiMemConfigReader cmcr = new CdiMemConfigReader(nidHere, store, service);
        CdiMemConfigReader.ReaderAccess a = new CdiMemConfigReader.ReaderAccess() {
            @Override
            public void progressNotify(long _bytesRead, long totalBytes) {
                bytesRead = _bytesRead;
            }

            public void provideReader(java.io.Reader r) {
                rdr = r;
            }
        };
        
        rdr = null;
        cmcr.startLoadReader(a);
        Assert.assertTrue(rdr != null);
        Assert.assertEquals("1", test64String.getBytes()[0], rdr.read());
        Assert.assertEquals("2", test64String.getBytes()[1], rdr.read());
        Assert.assertEquals(test64String.length()-1, bytesRead);
        int count = 2;
        while (rdr.read() > 0) count++;
        Assert.assertEquals("length", test64String.length()-1, count); // -1 for trailing zero on input
    }

    @Test
    public void testShortRead() throws java.io.IOException {
    
        String testShortString = "123456789012345678901234567890"; // length = 30
        content = testShortString.getBytes();
        content[content.length-1] = 0;

        service = new MemoryConfigurationService(nidHere, dgs) {
            public void requestRead(NodeID dest, int space, long address, int len, McsReadHandler
                    cb) {
                byte[] data = new byte[Math.min(len, (int)(content.length - address))];
                System.arraycopy(content, (int)address, data, 0, data.length);
                cb.handleReadData(nidThere, space, address, data);
            }
        };
        CdiMemConfigReader cmcr = new CdiMemConfigReader(nidHere, store, service);
        CdiMemConfigReader.ReaderAccess a = new CdiMemConfigReader.ReaderAccess() {
            @Override
            public void progressNotify(long _bytesRead, long totalBytes) {
                bytesRead = _bytesRead;
            }

            public void provideReader(java.io.Reader r) {
                rdr = r;
            }
        };
        
        rdr = null;
        cmcr.startLoadReader(a);
        Assert.assertTrue(rdr != null);
        Assert.assertEquals("1", testShortString.getBytes()[0], rdr.read());
        Assert.assertEquals("2", testShortString.getBytes()[1], rdr.read());
        Assert.assertEquals(testShortString.length()-1, bytesRead);
        int count = 2;
        while (rdr.read() > 0) count++;
        Assert.assertEquals("length", testShortString.length()-1, count); // -1 for trailing zero on input
    }

}
