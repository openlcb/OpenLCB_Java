package org.openlcb.implementations;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlcb.AbstractConnection;
import org.openlcb.Connection;
import org.openlcb.DatagramAcknowledgedMessage;
import org.openlcb.DatagramMessage;
import org.openlcb.DatagramRejectedMessage;
import org.openlcb.Message;
import org.openlcb.NodeID;

/**
 * @author  Bob Jacobsen   Copyright 2012
 */
public class DatagramServiceTest {
    NodeID hereID = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID farID = new NodeID(new byte[]{1,2,3,4,5,7});
    Connection testConnection;
    java.util.ArrayList<Message> messagesReceived;
    boolean flag;
    DatagramService service;
   
    @Before 
    public void setUp() {
        messagesReceived = new java.util.ArrayList<Message>();
        testConnection = new AbstractConnection(){
            @Override
            public void put(Message msg, Connection sender) {
                messagesReceived.add(msg);
            }
        };
        service = new DatagramService(hereID, testConnection);
        flag = false;
    }

    @After
    public void tearDown(){
        messagesReceived = null;
        testConnection = null;
        service = null;
    }    
    
    @Test 
    public void testCtorViaSetup() {
        Assert.assertNotNull("exists",service);
    }
    
    @Test
    public void testRcvMemoIsRealClass() {
        DatagramService.DatagramServiceReceiveMemo m20 = 
            new DatagramService.DatagramServiceReceiveMemo(0x20);
        DatagramService.DatagramServiceReceiveMemo m21 = 
            new DatagramService.DatagramServiceReceiveMemo(0x21);
        
        Assert.assertTrue(m20.equals(m20));
        Assert.assertTrue(!m20.equals(null));
        Assert.assertTrue(!m20.equals(m21));
    }

    @Test
    public void testXmtMemoIsRealClass() {
        class TestMemo extends DatagramService.DatagramServiceTransmitMemo {
            public TestMemo(NodeID dest, int[] data) {
                super(dest, data);
            }

            @Override
            public void handleSuccess(int flags) {

            }

            @Override
            public void handleFailure(int errorCode) {

            }
        }

        DatagramService.DatagramServiceTransmitMemo m20 = 
            new TestMemo(farID,new int[]{1});

        DatagramService.DatagramServiceTransmitMemo m21 = 
            new TestMemo(farID,new int[]{1});
        DatagramService.DatagramServiceTransmitMemo m22 = 
            new TestMemo(farID,new int[]{2});
        DatagramService.DatagramServiceTransmitMemo m23 = 
            new TestMemo(farID,new int[]{1,2});
        DatagramService.DatagramServiceTransmitMemo m24 = 
            new TestMemo(hereID,new int[]{1,2});
        
        Assert.assertTrue(!m20.equals(null));
        Assert.assertTrue(!m21.equals(null));
        Assert.assertTrue(!m22.equals(null));
        Assert.assertTrue(!m23.equals(null));
        Assert.assertTrue(!m24.equals(null));

        Assert.assertTrue(m20.equals(m20));
        Assert.assertTrue(m20.equals(m21));

        Assert.assertTrue(!m21.equals(m22));
        Assert.assertTrue(!m21.equals(m23));
        Assert.assertTrue(!m21.equals(m24));
        Assert.assertTrue(!m22.equals(m23));
        Assert.assertTrue(!m22.equals(m24));
        Assert.assertTrue(!m23.equals(m24));
    }

    @Test
    public void testRegisterForData() {
        DatagramService.DatagramServiceReceiveMemo m20 = 
            new DatagramService.DatagramServiceReceiveMemo(0x20);

        service.registerForReceive(m20);        
    }

    @Test
    public void testReceiveDGbeforeReg() {
        Message m = new DatagramMessage(farID, hereID, new int[]{0x020});
      
        Assert.assertEquals(0,messagesReceived.size());

        Assert.assertTrue(!flag);
        service.put(m, null);
        Assert.assertTrue(!flag);
        
        Assert.assertEquals(1,messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0) instanceof DatagramRejectedMessage);
    }

    @Test
    public void testReceiveFirstDG() {
        DatagramService.DatagramServiceReceiveMemo m20 = 
            new DatagramService.DatagramServiceReceiveMemo(0x20){
                @Override
                public void handleData(NodeID n, int[] data, DatagramService.ReplyMemo service) {
                    flag = true;
                    service.acceptData(0); 
                }
            };

        service.registerForReceive(m20);  
        
        Message m = new DatagramMessage(farID, hereID, new int[]{0x020});
      
        Assert.assertTrue(!flag);
        service.put(m, null);
        Assert.assertTrue(flag);
        
        Assert.assertEquals(1,messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0) instanceof DatagramAcknowledgedMessage);
    }

    @Test
    public void testReceiveWrongDGType() {
        DatagramService.DatagramServiceReceiveMemo m20 = 
            new DatagramService.DatagramServiceReceiveMemo(0x20){
                @Override
                public void handleData(NodeID n, int[] data, DatagramService.ReplyMemo service) {
                    flag = true;
                    service.acceptData(0); 
                }
            };

        service.registerForReceive(m20);  
        
        Message m = new DatagramMessage(farID, hereID, new int[]{0x21});
      
        Assert.assertTrue(!flag);
        service.put(m, null);
        Assert.assertTrue(!flag);
        
        Assert.assertEquals(1,messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0) instanceof DatagramRejectedMessage);
    }

    @Test
    public void testReceiveWrongDest() {
        DatagramService.DatagramServiceReceiveMemo m20 = 
            new DatagramService.DatagramServiceReceiveMemo(0x20){
                @Override
                public void handleData(NodeID n, int[] data, DatagramService.ReplyMemo service) {
                    flag = true;
                    service.acceptData(0); 
                }
            };

        service.registerForReceive(m20);  
        
        Message m = new DatagramMessage(farID, farID, new int[]{0x20});
      
        Assert.assertTrue(!flag);
        service.put(m, null);
        Assert.assertTrue(!flag);
        
        Assert.assertEquals(0,messagesReceived.size());
    }


    @Test
    public void testSendOK() {
        int[] data = new int[]{1,2,3,4,5};
        DatagramService.DatagramServiceTransmitMemo memo = 
            new DatagramService.DatagramServiceTransmitMemo(farID,data) {
                @Override
                public void handleSuccess(int flags) {
                    flag = true;
                }

                @Override
                public void handleFailure(int errorCode) {
                    flag = true;
                    Assert.assertEquals("Send failed. error code is ", 0, errorCode);
                }
            };
            
        service.sendData(memo);
        
        Assert.assertEquals("init messages", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0)
                           .equals(new DatagramMessage(hereID, farID, data)));

        // Accepted
        Message m = new DatagramAcknowledgedMessage(farID, hereID);
        messagesReceived = new java.util.ArrayList<Message>();

        Assert.assertTrue(!flag);
        service.put(m, null);
        Assert.assertTrue(flag);

        Assert.assertEquals("1st messages", 0, messagesReceived.size());
    }
}
