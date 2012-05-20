package org.openlcb.implementations;

import org.openlcb.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class DatagramServiceTest extends TestCase {
    
    NodeID hereID = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID farID = new NodeID(new byte[]{1,2,3,4,5,7});
    Connection testConnection;
    java.util.ArrayList<Message> messagesReceived;
    boolean flag;
    DatagramService service;
    
    public void setUp() {
        messagesReceived = new java.util.ArrayList<Message>();
        testConnection = new AbstractConnection(){
            public void put(Message msg, Connection sender) {
                messagesReceived.add(msg);
            }
        };
        service = new DatagramService(hereID, testConnection);
        flag = false;
    }
    
    
    
    public void testCtorViaSetup() {
    }
    
    public void testRcvMemoIsRealClass() {
        DatagramService.DatagramServiceReceiveMemo m20 = 
            new DatagramService.DatagramServiceReceiveMemo(0x20);
        DatagramService.DatagramServiceReceiveMemo m21 = 
            new DatagramService.DatagramServiceReceiveMemo(0x21);
        
        Assert.assertTrue(m20.equals(m20));
        Assert.assertTrue(!m20.equals(null));
        Assert.assertTrue(!m20.equals(m21));
        
    }

    public void testXmtMemoIsRealClass() {
        DatagramService.DatagramServiceTransmitMemo m20 = 
            new DatagramService.DatagramServiceTransmitMemo(farID,new int[]{1});
        DatagramService.DatagramServiceTransmitMemo m21 = 
            new DatagramService.DatagramServiceTransmitMemo(farID,new int[]{1});
        DatagramService.DatagramServiceTransmitMemo m22 = 
            new DatagramService.DatagramServiceTransmitMemo(farID,new int[]{2});
        DatagramService.DatagramServiceTransmitMemo m23 = 
            new DatagramService.DatagramServiceTransmitMemo(farID,new int[]{1,2});
        
        Assert.assertTrue(m20.equals(m20));
        Assert.assertTrue(m20.equals(m21));
        Assert.assertTrue(!m20.equals(null));
        Assert.assertTrue(!m20.equals(m22));
        Assert.assertTrue(!m20.equals(m23));
        Assert.assertTrue(!m22.equals(m20));
        Assert.assertTrue(!m23.equals(m20));
        
    }

    public void testRegisterForData() {
        DatagramService.DatagramServiceReceiveMemo m20 = 
            new DatagramService.DatagramServiceReceiveMemo(0x20);

        service.registerForReceive(m20);        
    }

    public void testReceiveDGbeforeReg() {
        DatagramService.DatagramServiceReceiveMemo m20 = 
            new DatagramService.DatagramServiceReceiveMemo(0x20){
                @Override
                public void handleData(NodeID n, int[] data, DatagramService.ReplyMemo service) {
                    flag = true;
                    service.acceptData(0); 
                }
            };
        
        Message m = new DatagramMessage(farID, hereID, new int[]{0x020});
      
        Assert.assertEquals(0,messagesReceived.size());

        Assert.assertTrue(!flag);
        service.put(m, null);
        Assert.assertTrue(!flag);
        
        Assert.assertEquals(1,messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0) instanceof DatagramRejectedMessage);
    }

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


    public void testSendOK() {
        int[] data = new int[]{1,2,3,4,5};
        DatagramService.DatagramServiceTransmitMemo memo = 
            new DatagramService.DatagramServiceTransmitMemo(farID,data) {
                @Override
                public void handleReply(int code) { 
                    flag = true;
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
    
    
    // from here down is testing infrastructure
    
    public DatagramServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DatagramServiceTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DatagramServiceTest.class);
        return suite;
    }
}
