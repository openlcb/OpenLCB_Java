package org.openlcb.implementations;

import org.openlcb.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
    
    public void setUp() {
        messagesReceived = new java.util.ArrayList<Message>();
        flag = false;
        testConnection = new Connection(){
            public void put(Message msg, Connection sender) {
                messagesReceived.add(msg);
            }
        };
        datagramService = new DatagramService(hereID, testConnection);
        
        service = new MemoryConfigurationService(hereID, datagramService);
    }
    
    
    
    public void testCtorViaSetup() {
    }
    
    public void testReadMemoIsRealClass() {
        MemoryConfigurationService.McsReadMemo m20 = 
            new MemoryConfigurationService.McsReadMemo(farID,0xFD, 0x0000, 2);
        MemoryConfigurationService.McsReadMemo m20a = 
            new MemoryConfigurationService.McsReadMemo(farID,0xFD, 0x0000, 2);
        MemoryConfigurationService.McsReadMemo m21 = 
            new MemoryConfigurationService.McsReadMemo(hereID,0xFD, 0x0000, 2);
        MemoryConfigurationService.McsReadMemo m22 = 
            new MemoryConfigurationService.McsReadMemo(farID,0xFE, 0x0000, 2);
        MemoryConfigurationService.McsReadMemo m23 = 
            new MemoryConfigurationService.McsReadMemo(farID,0xFD, 0x0001, 2);
        MemoryConfigurationService.McsReadMemo m24 = 
            new MemoryConfigurationService.McsReadMemo(farID,0xFD, 0x0000, 3);
        
        Assert.assertTrue(m20.equals(m20));
        Assert.assertTrue(m20.equals(m20a));
        
        Assert.assertTrue(!m20.equals(null));
        
        Assert.assertTrue(!m20.equals(m21));
        Assert.assertTrue(!m20.equals(m22));
        Assert.assertTrue(!m20.equals(m23));
        Assert.assertTrue(!m20.equals(m24));
        
    }

    public void testWriteMemoIsRealClass() {
        MemoryConfigurationService.McsWriteMemo m20 = 
            new MemoryConfigurationService.McsWriteMemo(farID,0xFD, 0x0000, new int[]{1,2});
        MemoryConfigurationService.McsWriteMemo m20a = 
            new MemoryConfigurationService.McsWriteMemo(farID,0xFD, 0x0000, new int[]{1,2});
        MemoryConfigurationService.McsWriteMemo m21 = 
            new MemoryConfigurationService.McsWriteMemo(hereID,0xFD, 0x0000, new int[]{1,2});
        MemoryConfigurationService.McsWriteMemo m22 = 
            new MemoryConfigurationService.McsWriteMemo(farID,0xFE, 0x0000, new int[]{1,2});
        MemoryConfigurationService.McsWriteMemo m23 = 
            new MemoryConfigurationService.McsWriteMemo(farID,0xFD, 0x0001, new int[]{1,2});
        MemoryConfigurationService.McsWriteMemo m24 = 
            new MemoryConfigurationService.McsWriteMemo(farID,0xFD, 0x0000, new int[]{1});
        MemoryConfigurationService.McsWriteMemo m25 = 
            new MemoryConfigurationService.McsWriteMemo(farID,0xFD, 0x0000, new int[]{1,2,3});
        MemoryConfigurationService.McsWriteMemo m26 = 
            new MemoryConfigurationService.McsWriteMemo(farID,0xFD, 0x0000, new int[]{1,5,3});
        
        Assert.assertTrue(m20.equals(m20));
        Assert.assertTrue(m20.equals(m20a));
        
        Assert.assertTrue(!m20.equals(null));
        
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
        int[] data = new int[]{1,2};
        MemoryConfigurationService.McsWriteMemo memo = 
            new MemoryConfigurationService.McsWriteMemo(farID, space, address, data) {
                public void handleWriteReply(int code) { 
                    flag = true;
                }
            };

        // test executes the callbacks instantly; real connections might not
        Assert.assertTrue(!flag);
        service.request(memo);
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

//     public void testReceiveDGbeforeReg() {
//         DatagramService.DatagramServiceReceiveMemo m20 = 
//             new DatagramService.DatagramServiceReceiveMemo(0x20){
//                 public int handleData(int[] data) { 
//                     flag = true;
//                     return 0; 
//                 }
//             };
//         
//         Message m = new DatagramMessage(farID, hereID, new int[]{0x020});
//       
//         Assert.assertEquals(0,messagesReceived.size());
// 
//         Assert.assertTrue(!flag);
//         service.put(m, null);
//         Assert.assertTrue(!flag);
//         
//         Assert.assertEquals(1,messagesReceived.size());
//         Assert.assertTrue(messagesReceived.get(0) instanceof DatagramRejectedMessage);
//     }
// 
//     public void testReceiveFirstDG() {
//         DatagramService.DatagramServiceReceiveMemo m20 = 
//             new DatagramService.DatagramServiceReceiveMemo(0x20){
//                 public int handleData(int[] data) { 
//                     flag = true;
//                     return 0; 
//                 }
//             };
// 
//         service.registerForReceive(m20);  
//         
//         Message m = new DatagramMessage(farID, hereID, new int[]{0x020});
//       
//         Assert.assertTrue(!flag);
//         service.put(m, null);
//         Assert.assertTrue(flag);
//         
//         Assert.assertEquals(1,messagesReceived.size());
//         Assert.assertTrue(messagesReceived.get(0) instanceof DatagramAcknowledgedMessage);
//     }
// 
//     public void testReceiveWrongDGType() {
//         DatagramService.DatagramServiceReceiveMemo m20 = 
//             new DatagramService.DatagramServiceReceiveMemo(0x20){
//                 public int handleData(int[] data) { 
//                     flag = true;
//                     return 0; 
//                 }
//             };
// 
//         service.registerForReceive(m20);  
//         
//         Message m = new DatagramMessage(farID, hereID, new int[]{0x21});
//       
//         Assert.assertTrue(!flag);
//         service.put(m, null);
//         Assert.assertTrue(!flag);
//         
//         Assert.assertEquals(1,messagesReceived.size());
//         Assert.assertTrue(messagesReceived.get(0) instanceof DatagramRejectedMessage);
//     }
// 
// 
//     public void testSendOK() {
//         int[] data = new int[]{1,2,3,4,5};
//         DatagramService.DatagramServiceTransmitMemo memo = 
//             new DatagramService.DatagramServiceTransmitMemo(farID,data) {
//                 public void handleReply(int code) { 
//                     flag = true;
//                 }
//             };
//             
//         service.sendData(memo);
//         
//         Assert.assertEquals("init messages", 1, messagesReceived.size());
//         Assert.assertTrue(messagesReceived.get(0)
//                            .equals(new DatagramMessage(hereID, farID, data)));
// 
//         // Accepted
//         Message m = new DatagramAcknowledgedMessage(farID, hereID);
//         messagesReceived = new java.util.ArrayList<Message>();
// 
//         Assert.assertTrue(!flag);
//         service.put(m, null);
//         Assert.assertTrue(flag);
// 
//         Assert.assertEquals("1st messages", 0, messagesReceived.size());
//         
//     }
    
    
    // from here down is testing infrastructure
    
    public MemoryConfigurationServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MemoryConfigurationServiceTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MemoryConfigurationServiceTest.class);
        return suite;
    }
}
