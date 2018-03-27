package org.openlcb.implementations;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.openlcb.*;

/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision: -1 $
 */
public class MemoryConfigurationServiceTest extends TestCase {
    
    NodeID hereID = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID farID = new NodeID(new byte[]{1,2,3,4,5,7});
    Connection testConnection;
    java.util.ArrayList<Message> messagesReceived;
    boolean flag;
    DatagramService datagramService;
    MemoryConfigurationService service;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        messagesReceived = new java.util.ArrayList<Message>();
        flag = false;
        testConnection = new AbstractConnection(){
            @Override
            public void put(Message msg, Connection sender) {
                messagesReceived.add(msg);
            }
        };
        datagramService = new DatagramService(hereID, testConnection);
        
        service = new MemoryConfigurationService(hereID, datagramService);
    }

    @Override
    protected void tearDown(){
       service.dispose();
    }
    
    
    
    public void testCtorViaSetup() {
    }
    
    public void testReadMemoIsRealClass() {
        MemoryConfigurationService.McsReadHandler hnd = new MemoryConfigurationService
                .McsReadHandler() {
            @Override
            public void handleFailure(int code) {}

            @Override
            public void handleReadData(NodeID dest, int space, long address, byte[] data) {}
        };
        MemoryConfigurationService.McsReadMemo m20 = new MemoryConfigurationService.McsReadMemo(farID, 0xFD, 0x0000, 2, hnd);
        MemoryConfigurationService.McsReadMemo m20a = new MemoryConfigurationService.McsReadMemo(farID,0xFD, 0x0000, 2, hnd);
        MemoryConfigurationService.McsReadMemo m21 = new MemoryConfigurationService.McsReadMemo(hereID,0xFD, 0x0000, 2, hnd);
        MemoryConfigurationService.McsReadMemo m22 = new MemoryConfigurationService.McsReadMemo(farID,0xFE, 0x0000, 2, hnd);
        MemoryConfigurationService.McsReadMemo m23 = new MemoryConfigurationService.McsReadMemo(farID,0xFD, 0x0001, 2, hnd);
        MemoryConfigurationService.McsReadMemo m24 = new MemoryConfigurationService.McsReadMemo(farID,0xFD, 0x0000, 3, hnd);
        
        Assert.assertTrue(m20.equals(m20));
        Assert.assertTrue(m20.equals(m20a));
        
        Assert.assertTrue(m20 != null);
        
        Assert.assertTrue(!m20.equals(m21));
        Assert.assertTrue(!m20.equals(m22));
        Assert.assertTrue(!m20.equals(m23));
        Assert.assertTrue(!m20.equals(m24));
        
    }

    public void testWriteMemoIsRealClass() {
        MemoryConfigurationService.McsWriteHandler hnd = new MemoryConfigurationService.McsWriteHandler() {
            @Override
            public void handleSuccess() { }

            @Override
            public void handleFailure(int errorCode) { }
        };
        MemoryConfigurationService.McsWriteMemo m20 =
            new MemoryConfigurationService.McsWriteMemo(farID,0xFD, 0x0000, new byte[]{1,2}, hnd);
        MemoryConfigurationService.McsWriteMemo m20a = 
            new MemoryConfigurationService.McsWriteMemo(farID,0xFD, 0x0000, new byte[]{1,2}, hnd);
        MemoryConfigurationService.McsWriteMemo m21 = 
            new MemoryConfigurationService.McsWriteMemo(hereID,0xFD, 0x0000, new byte[]{1,2}, hnd);
        MemoryConfigurationService.McsWriteMemo m22 = 
            new MemoryConfigurationService.McsWriteMemo(farID,0xFE, 0x0000, new byte[]{1,2}, hnd);
        MemoryConfigurationService.McsWriteMemo m23 = 
            new MemoryConfigurationService.McsWriteMemo(farID,0xFD, 0x0001, new byte[]{1,2}, hnd);
        MemoryConfigurationService.McsWriteMemo m24 = 
            new MemoryConfigurationService.McsWriteMemo(farID,0xFD, 0x0000, new byte[]{1}, hnd);
        MemoryConfigurationService.McsWriteMemo m25 = 
            new MemoryConfigurationService.McsWriteMemo(farID,0xFD, 0x0000, new byte[]{1,2,3}, hnd);
        MemoryConfigurationService.McsWriteMemo m26 = 
            new MemoryConfigurationService.McsWriteMemo(farID,0xFD, 0x0000, new byte[]{1,5,3}, hnd);
        
        Assert.assertTrue(m20.equals(m20));
        Assert.assertTrue(m20.equals(m20a));
        
        Assert.assertTrue(m20 != null);
        
        Assert.assertTrue(!m20.equals(m21));
        Assert.assertTrue(!m20.equals(m22));
        Assert.assertTrue(!m20.equals(m23));
        Assert.assertTrue(!m20.equals(m24));
        Assert.assertTrue(!m20.equals(m25));
        Assert.assertTrue(!m20.equals(m26));
        
    }

    public void testSimpleWrite() {
        int space = 0xFD;
        long address = 0x12345678;
        byte[] data = new byte[]{1,2};
        MemoryConfigurationService.McsWriteHandler cb =
            new MemoryConfigurationService.McsWriteHandler() {
                @Override
                public void handleFailure(int errorCode) {
                    assertEquals("Write failed. error code is ", 0, errorCode);
                }

                @Override
                public void handleSuccess() {
                    flag = true;
                }
            };

        // test executes the callbacks instantly; real connections might not
        Assert.assertTrue(!flag);
        service.requestWrite(farID, space, address, data, cb);
        Assert.assertTrue(!flag);
        
        // should have sent datagram
         Assert.assertEquals(1,messagesReceived.size());
         Assert.assertTrue(messagesReceived.get(0) instanceof DatagramMessage);

        // check format of datagram write
        int[] content = ((DatagramMessage)messagesReceived.get(0)).getData();
        Assert.assertTrue(content.length >= 6);
        Assert.assertEquals("datagram type", 0x20, content[0]);
        Assert.assertEquals("write command", 0x00, (content[1]&0xFC));
        
        Assert.assertEquals("address", address, ((long)content[2]<<24)+((long)content[3]<<16)+((long)content[4]<<8)+(long)content[5] );
        
        if (space >= 0xFD) {
            Assert.assertEquals("space bits", space&0x3, content[1]&0x3);
            Assert.assertEquals("data length", content.length-6, data.length);
            for (int i = 0; i<data.length; i++)
                Assert.assertEquals("data byte "+i, content[i+6], data[i]);
        } else {
            Assert.assertEquals("space byte", space, content[6]);
            Assert.assertEquals("data length", content.length-7, data.length);
            for (int i = 0; i<data.length; i++)
                Assert.assertEquals("data byte "+i, content[i+7], data[i]);
        }
        
        // datagram reply comes back 
        Message m = new DatagramAcknowledgedMessage(farID, hereID);

        Assert.assertTrue(!flag);
        datagramService.put(m, null);
        Assert.assertTrue(flag);
        
    }

        /*
    public void testSimpleRead() {
        int space = 0xFD;
        long address = 0x12345678;
        int length = 4;
        MemoryConfigurationService.McsReadMemo memo = 
            new MemoryConfigurationService.McsReadMemo(farID, space, address, length) {
                @Override
                public void handleFailure(int code) {
                    flag = true;
                    assertEquals("Read failed. error code is ", 0, code);
                }

                @Override
                public void handleReadData(NodeID dest, int readSpace, long readAddress, byte[] readData) { 
                    flag = true;
                    Assert.assertEquals("space", space, readSpace);
                    Assert.assertEquals("address", address, readAddress);
                    Assert.assertEquals("data length", 1, readData.length);
                    Assert.assertEquals("data[0]", 0xAA, readData[0]&0xFF);
                }
            };

        // test executes the callbacks instantly; real connections might not
        Assert.assertFalse(flag);
        service.request(memo);
        Assert.assertFalse(flag);
        
        // should have sent datagram
         Assert.assertEquals(1,messagesReceived.size());
         Assert.assertTrue(messagesReceived.get(0) instanceof DatagramMessage);

        // check format of datagram read
        int[] content = ((DatagramMessage)messagesReceived.get(0)).getData();
        Assert.assertTrue(content.length >= 6);
        Assert.assertEquals("datagram type", 0x20, content[0]);
        Assert.assertEquals("read command", 0x40, (content[1]&0xFC));
        
        Assert.assertEquals("address", address, ((long)content[2]<<24)+((long)content[3]<<16)+((long)content[4]<<8)+(long)content[5] );
        
        if (space >= 0xFD) {
            Assert.assertEquals("space bits", space&0x3, content[1]&0x3);
            Assert.assertEquals("data length", length, content[6]);
        } else {
            Assert.assertEquals("space byte", space, content[6]);
            Assert.assertEquals("data length", length, content[7]);
        }
        
        // datagram reply comes back 
        Message m = new DatagramAcknowledgedMessage(farID, hereID, 0x80);

        Assert.assertFalse(flag);
        datagramService.put(m, null);
        Assert.assertFalse(flag);
        
        // now return data
        flag = false;
        content[1] = content[1]|0x04;  //change command to response
        content[content.length-1] = 0xAA;  // 1st data byte
        
        m = new DatagramMessage(farID, hereID, content);

        Assert.assertFalse(flag);
        datagramService.put(m, null);
        Assert.assertTrue(flag);
        
    }

    public void testTwoSimpleReadsInSequence() {
        int space = 0xFD;
        long address = 0x12345678;
        int length = 4;
        MemoryConfigurationService.McsReadMemo memo = 
            new MemoryConfigurationService.McsReadMemo(farID, space, address, length) {
                @Override
                public void handleFailure(int code) {
                    flag = true;
                    assertEquals("Read failed. error code is ", 0, code);
                }

                @Override
                public void handleReadData(NodeID dest, int readSpace, long readAddress, byte[] readData) { 
                    flag = true;
                    Assert.assertEquals("space", space, readSpace);
                    Assert.assertEquals("address", address, readAddress);
                    Assert.assertEquals("data length", 1, readData.length);
                    Assert.assertEquals("data[0]", 0xAA, readData[0]&0xFF);
                }
            };

        // start of 1st pass
        {
            // test executes the callbacks instantly; real connections might not
            Assert.assertFalse(flag);
            service.request(memo);
            Assert.assertFalse(flag);
        
            // should have sent datagram
             Assert.assertEquals(1,messagesReceived.size());
             Assert.assertTrue(messagesReceived.get(0) instanceof DatagramMessage);

            // check format of datagram read
            int[] content = ((DatagramMessage)messagesReceived.get(0)).getData();
            Assert.assertTrue(content.length >= 6);
            Assert.assertEquals("datagram type", 0x20, content[0]);
            Assert.assertEquals("read command", 0x40, (content[1]&0xFC));
        
            Assert.assertEquals("address", address, ((long)content[2]<<24)+((long)content[3]<<16)+((long)content[4]<<8)+(long)content[5] );
        
            if (space >= 0xFD) {
                Assert.assertEquals("space bits", space&0x3, content[1]&0x3);
                Assert.assertEquals("data length", length, content[6]);
            } else {
                Assert.assertEquals("space byte", space, content[6]);
                Assert.assertEquals("data length", length, content[7]);
            }
        
            // datagram reply comes back 
            Message m = new DatagramAcknowledgedMessage(farID, hereID);

            Assert.assertFalse(flag);
            datagramService.put(m, null);
            Assert.assertFalse(flag);
        
            // now return data
            flag = false;
            content[1] = content[1]|0x04;  //change command to response
            content[content.length-1] = 0xAA;  // 1st data byte
        
            m = new DatagramMessage(farID, hereID, content);

            Assert.assertFalse(flag);
            datagramService.put(m, null);
            Assert.assertTrue(flag);
        }  
        
        // start of 2nd pass
        messagesReceived = new java.util.ArrayList<Message>();
        flag = false;
        {
            // test executes the callbacks instantly; real connections might not
            Assert.assertFalse(flag);
            service.request(memo);
            Assert.assertFalse(flag);
        
            // should have sent datagram
             Assert.assertEquals(1,messagesReceived.size());
             Assert.assertTrue(messagesReceived.get(0) instanceof DatagramMessage);

            // check format of datagram read
            int[] content = ((DatagramMessage)messagesReceived.get(0)).getData();
            Assert.assertTrue(content.length >= 6);
            Assert.assertEquals("datagram type", 0x20, content[0]);
            Assert.assertEquals("read command", 0x40, (content[1]&0xFC));
        
            Assert.assertEquals("address", address, ((long)content[2]<<24)+((long)content[3]<<16)+((long)content[4]<<8)+(long)content[5] );
        
            if (space >= 0xFD) {
                Assert.assertEquals("space bits", space&0x3, content[1]&0x3);
                Assert.assertEquals("data length", length, content[6]);
            } else {
                Assert.assertEquals("space byte", space, content[6]);
                Assert.assertEquals("data length", length, content[7]);
            }
        
            // datagram reply comes back 
            Message m = new DatagramAcknowledgedMessage(farID, hereID, 0x80);

            Assert.assertFalse(flag);
            datagramService.put(m, null);
            Assert.assertFalse(flag);
        
            // now return data
            flag = false;
            content[1] = content[1]|0x04;  //change command to response
            content[content.length-1] = 0xAA;  // 1st data byte
        
            m = new DatagramMessage(farID, hereID, content);

            Assert.assertFalse(flag);
            datagramService.put(m, null);
            Assert.assertTrue(flag);
        }
    }

    public void testSimpleReadFails() {
        int space = 0xFD;
        long address = 0x12345678;
        int length = 4;
        MemoryConfigurationService.McsReadMemo memo = 
            new MemoryConfigurationService.McsReadMemo(farID, space, address, length) {
                @Override
                public void handleFailure(int code) {
                    flag = true;
                    assertEquals("Read failed. error code is ", 0, code);
                }

                @Override
                public void handleReadData(NodeID dest, int readSpace, long readAddress, byte[] readData) { 
                    flag = true;
                    Assert.assertEquals("space", space, readSpace);
                    Assert.assertEquals("address", address, readAddress);
                    
                    // data length is zero because of error
                    Assert.assertEquals("data length", 0, readData.length);
                }
            };

        // test executes the callbacks instantly; real connections might not
        Assert.assertFalse(flag);
        service.request(memo);
        Assert.assertFalse(flag);
        
        // should have sent datagram
         Assert.assertEquals(1,messagesReceived.size());
         Assert.assertTrue(messagesReceived.get(0) instanceof DatagramMessage);

        // check format of datagram read
        int[] content = ((DatagramMessage)messagesReceived.get(0)).getData();
        Assert.assertTrue(content.length >= 6);
        Assert.assertEquals("datagram type", 0x20, content[0]);
        Assert.assertEquals("read command", 0x40, (content[1]&0xFC));
        
        Assert.assertEquals("address", address, ((long)content[2]<<24)+((long)content[3]<<16)+((long)content[4]<<8)+(long)content[5] );
        
        if (space >= 0xFD) {
            Assert.assertEquals("space bits", space&0x3, content[1]&0x3);
            Assert.assertEquals("data length", length, content[6]);
        } else {
            Assert.assertEquals("space byte", space, content[6]);
            Assert.assertEquals("data length", length, content[7]);
        }
        
        // datagram reply comes back 
        Message m = new DatagramAcknowledgedMessage(farID, hereID);

        Assert.assertFalse(flag);
        datagramService.put(m, null);
        Assert.assertFalse(flag);
        
        // now return data
        flag = false;
        content[1] = content[1]|0x04|0x08;  //change command to error response
        content[content.length-1] = 0xAA;  // 1st data byte which is error
        
        m = new DatagramMessage(farID, hereID, content);

        Assert.assertFalse(flag);
        datagramService.put(m, null);
        Assert.assertTrue(flag);
        
    }

    public void testSimpleReadFromSpace1() {
        int space = 0x01;
        long address = 0x12345678;
        int length = 4;
        MemoryConfigurationService.McsReadMemo memo = 
            new MemoryConfigurationService.McsReadMemo(farID, space, address, length) {
                @Override
                public void handleFailure(int code) {
                    flag = true;
                    assertEquals("Write failed. error code is ", 0, code);
                }

                @Override
                public void handleReadData(NodeID dest, int readSpace, long readAddress, byte[] readData) { 
                    flag = true;
                    Assert.assertEquals("space", space, readSpace);
                    Assert.assertEquals("address", address, readAddress);
                    Assert.assertEquals("data length", 1, readData.length);
                    Assert.assertEquals("data[0]", 0xAA, readData[0]&0xFF);
                }
            };

        // test executes the callbacks instantly; real connections might not
        Assert.assertFalse(flag);
        service.request(memo);
        Assert.assertFalse(flag);
        
        // should have sent datagram
         Assert.assertEquals(1,messagesReceived.size());
         Assert.assertTrue(messagesReceived.get(0) instanceof DatagramMessage);

        // check format of datagram read
        int[] content = ((DatagramMessage)messagesReceived.get(0)).getData();
        Assert.assertTrue(content.length >= 6);
        Assert.assertEquals("datagram type", 0x20, content[0]);
        Assert.assertEquals("read command", 0x40, (content[1]&0xFC));
        
        Assert.assertEquals("address", address, ((long)content[2]<<24)+((long)content[3]<<16)+((long)content[4]<<8)+(long)content[5] );
        
        if (space >= 0xFD) {
            Assert.assertEquals("space bits", space&0x3, content[1]&0x3);
            Assert.assertEquals("data length", length, content[6]);
        } else {
            Assert.assertEquals("space byte", space, content[6]);
            Assert.assertEquals("data length", length, content[7]);
        }
        
        // datagram reply comes back 
        Message m = new DatagramAcknowledgedMessage(farID, hereID);

        Assert.assertFalse(flag);
        datagramService.put(m, null);
        Assert.assertFalse(flag);
        
        // now return data
        flag = false;
        content[1] = content[1]|0x04;  //change command to response
        content[content.length-1] = 0xAA;  // 1st data byte

        m = new DatagramMessage(farID, hereID, content);

        Assert.assertFalse(flag);
        datagramService.put(m, null);
        Assert.assertTrue(flag);
        
    }
    */
    public void testConfigMemoIsRealClass() {
        class TestMemo extends MemoryConfigurationService.McsConfigMemo {
            public TestMemo(NodeID dest) {
                super(dest);
            }

            @Override
            public void handleFailure(int code) { }
        }
        MemoryConfigurationService.McsConfigMemo m20 =
            new TestMemo(farID);
        MemoryConfigurationService.McsConfigMemo m20a = 
            new TestMemo(farID);
        MemoryConfigurationService.McsConfigMemo m21 = 
            new TestMemo(hereID);
        
        Assert.assertTrue(m20.equals(m20));
        Assert.assertTrue(m20.equals(m20a));
        
        Assert.assertTrue(m20 != null);
        
        Assert.assertTrue(!m20.equals(m21));
        
    }

    public void testGetConfig() {
        MemoryConfigurationService.McsConfigMemo memo = 
            new MemoryConfigurationService.McsConfigMemo(farID) {
                @Override
                public void handleFailure(int code) {
                    flag = true;
                    assertEquals("Config get failed. error code is ", 0, code);
                }

                @Override
                public void handleConfigData(NodeID dest, int commands, int lengths, int highSpace, int lowSpace, String name) { 
                    flag = true;
                }
            };

        // test executes the callbacks instantly; real connections might not
        Assert.assertFalse(flag);
        service.request(memo);
        Assert.assertFalse(flag);
        
        // should have sent datagram
         Assert.assertEquals(1,messagesReceived.size());
         Assert.assertTrue(messagesReceived.get(0) instanceof DatagramMessage);

        // check format of datagram read
        int[] content = ((DatagramMessage)messagesReceived.get(0)).getData();
        Assert.assertTrue(content.length == 2);
        Assert.assertEquals("datagram type", 0x20, content[0]);
        Assert.assertEquals("read command", 0x80, (content[1]&0xFC));
                
        // datagram reply comes back 
        Message m = new DatagramAcknowledgedMessage(farID, hereID);

        Assert.assertFalse(flag);
        datagramService.put(m, null);
        Assert.assertFalse(flag);
        
        // now return data
        flag = false;
        content = new int[]{0x20, 0x81, 0x55, 0x55, 0xEE, 0xFF, 0x80, 'a', 'b', 'c'};
        m = new DatagramMessage(farID, hereID, content);

        Assert.assertFalse(flag);
        datagramService.put(m, null);
        Assert.assertTrue(flag);
        
    }

    public void testAddrSpaceMemoIsRealClass() {
        MemoryConfigurationService.McsAddrSpaceMemo m20 = 
            new MemoryConfigurationService.McsAddrSpaceMemo(farID,0xFD);
        MemoryConfigurationService.McsAddrSpaceMemo m20a = 
            new MemoryConfigurationService.McsAddrSpaceMemo(farID,0xFD);
        MemoryConfigurationService.McsAddrSpaceMemo m22 = 
            new MemoryConfigurationService.McsAddrSpaceMemo(farID,0xFE);
        MemoryConfigurationService.McsAddrSpaceMemo m23 = 
            new MemoryConfigurationService.McsAddrSpaceMemo(hereID,0xFD);
        
        Assert.assertTrue(m20.equals(m20));
        Assert.assertTrue(m20.equals(m20a));
        
        Assert.assertTrue(m20 != null);
        
        Assert.assertTrue(!m20.equals(m22));
        Assert.assertTrue(!m20.equals(m23));
        
    }

    public void testGetAddrSpace1() {
        int space = 0xFD;
        MemoryConfigurationService.McsAddrSpaceMemo memo = 
            new MemoryConfigurationService.McsAddrSpaceMemo(farID, space) {
                @Override
                public void handleWriteReply(int code) { 
                    flag = true;
                }
                @Override
                public void handleAddrSpaceData(NodeID dest, int space, long hiAddress, long lowAddress, int flags, String desc) { 
                    flag = true;
                    // check contents
                    Assert.assertTrue("space", space == 0xFD);
                    Assert.assertTrue("hiAddress", hiAddress == 0x12345678L);                 
                    Assert.assertTrue("lowAddress", lowAddress == 0x00L);                 
                }
            };

        // test executes the callbacks instantly; real connections might not
        Assert.assertFalse(flag);
        service.request(memo);
        Assert.assertFalse(flag);
        
        // should have sent datagram
         Assert.assertEquals(1,messagesReceived.size());
         Assert.assertTrue(messagesReceived.get(0) instanceof DatagramMessage);

        // check format of datagram read
        int[] content = ((DatagramMessage)messagesReceived.get(0)).getData();
        Assert.assertTrue(content.length == 3);
        Assert.assertEquals("datagram type", 0x20, content[0]);
        Assert.assertEquals("addr space command", 0x84, (content[1]&0xFC));
        Assert.assertEquals("space", space, (content[2]));
                
        // datagram reply comes back 
        Message m = new DatagramAcknowledgedMessage(farID, hereID);

        Assert.assertFalse(flag);
        datagramService.put(m, null);
        Assert.assertFalse(flag);
        
        // now return data
        flag = false;
        content = new int[]{0x20, 0x85, space, 0x12, 0x34, 0x56, 0x78, 0x55};
        m = new DatagramMessage(farID, hereID, content);

        Assert.assertFalse(flag);
        datagramService.put(m, null);
        Assert.assertTrue(flag);
                
    }
    public void testGetAddrSpace2() {
        int space = 0xFD;
        MemoryConfigurationService.McsAddrSpaceMemo memo = 
            new MemoryConfigurationService.McsAddrSpaceMemo(farID, space) {
                @Override
                public void handleWriteReply(int code) { 
                    flag = true;
                }
                @Override
                public void handleAddrSpaceData(NodeID dest, int space, long hiAddress, long lowAddress, int flags, String desc) { 
                    flag = true;
                    // check contents
                    Assert.assertTrue("space", space == 0xFD);
                    Assert.assertTrue("hiAddress", hiAddress == 0xFFFFFFFFL);                 
                    Assert.assertTrue("lowAddress", lowAddress == 0x12345678L);                 
                }
            };

        // test executes the callbacks instantly; real connections might not
        Assert.assertFalse(flag);
        service.request(memo);
        Assert.assertFalse(flag);
        
        // should have sent datagram
         Assert.assertEquals(1,messagesReceived.size());
         Assert.assertTrue(messagesReceived.get(0) instanceof DatagramMessage);

        // check format of datagram read
        int[] content = ((DatagramMessage)messagesReceived.get(0)).getData();
        Assert.assertTrue(content.length == 3);
        Assert.assertEquals("datagram type", 0x20, content[0]);
        Assert.assertEquals("addr space command", 0x84, (content[1]&0xFC));
        Assert.assertEquals("space", space, (content[2]));
                
        // datagram reply comes back 
        Message m = new DatagramAcknowledgedMessage(farID, hereID);

        Assert.assertFalse(flag);
        datagramService.put(m, null);
        Assert.assertFalse(flag);
        
        // now return data
        flag = false;
        content = new int[]{0x20, 0x85, space, 0xFF, 0xFF, 0xFF, 0xFF, 0x55, 0x12, 0x34, 0x56, 0x78};
        m = new DatagramMessage(farID, hereID, content);

        Assert.assertFalse(flag);
        datagramService.put(m, null);
        Assert.assertTrue(flag);
                
    }

    // from here down is testing infrastructure
    
    public MemoryConfigurationServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MemoryConfigurationServiceTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MemoryConfigurationServiceTest.class);
        return suite;
    }
}
