package org.openlcb;

import org.openlcb.*;
import org.openlcb.implementations.*;
import org.openlcb.LoaderClient;
import org.openlcb.LoaderClient.LoaderStatusReporter;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  David Harris   Copyright 2016
 */

/* Protocol:
 ---> memconfig Freeze (DG)
 (<--- DG ok)    -- node may reboot, and not be able to garantee this
 <--- InitComplete
 ---> PIPRequest
 <--- PIPReply
 IF streams implemented then use one:
 ---> memconfig write stream request (DG)
 <--- DG ok
 <--- memconfig write stream reply (DG)
 ---> DG ok
 ---> StreamInitRequest  }
 <--- StreamInitReply    |
 ---> StreamDataSend     }  StreamTransmitter
 ...                     |
 ---> StreamDataComplete }
 ELSE use datagrams:
 ---> DatagramMessage
 <--- DatagramAcknowledged
 ...
 ---> [stop sending data when run out of buffer]
 <--- stream data proceed
 ...
 <--- DatagramAcknowledged
 THEN:
 ---> UnFreeze
 
 */


public class LoaderClientTest extends TestCase {

    NodeID hereID = new NodeID(new byte[]{1,2,3,4,5,6});
    NodeID farID  = new NodeID(new byte[]{1,1,1,1,1,1});
    Connection testConnection;
    DatagramService dcs;
    MemoryConfigurationService mcs;
    public java.util.ArrayList<Message> messagesReceived;
    byte[] data;
    boolean flag;
    LoaderClient.LoaderStatusReporter reporter;
    
    @Override
    public void setUp() {
                                      // System.out.println("SetUp()");
        messagesReceived = new java.util.ArrayList<Message>();
        testConnection = new AbstractConnection(){
            public void put(Message msg, Connection sender) {
                                      //System.out.println("----->"+msg.toString());
                messagesReceived.add(msg);
            }
        };
        dcs = new DatagramService(hereID, testConnection);
        mcs = new MemoryConfigurationService(hereID, dcs);
        flag = false;
    };
 
    @Override
    protected void tearDown(){
       mcs.dispose();
    }


/* Protocol:
 ---> memconfig Freeze (DG)
 (<--- DG ok)    -- node may reboot, and not be able to garantee this
 <--- InitComplete
 ---> PIPRequest
 <--- PIPReply
 IF streams implemented then use one:
 ---> memconfig write stream request (DG)
 <--- DG ok
 <--- memconfig write stream reply (DG)
 ---> DG ok
 ---> StreamInitRequest
 <--- StreamInitReply
 ---> StreamDataSend
 ...
 ---> StreamDataComplete
 ELSE use datagrams:
 ---> DatagramMessage
 <--- DatagramAcknowledged
 ...
 ---> [stop sending data when run out of buffer]
 ...
 <--- DatagramAcknowledged
 THEN:
 ---> UnFreeze
 
 */
    
    //public void testFake() {}

    public void testLoaderClientDGPIPFail1() {
        data =new byte[80];
        LoaderClient xmt = new LoaderClient(testConnection, mcs, dcs);
        xmt.doLoad(hereID,farID, 0xEF, 0, data, new LoaderStatusReporter() {
            public void onProgress(float percent) {
                System.out.println("onProcess:"+percent);
            }
            public void onDone(int errorCode, String errorString) {
                System.out.println("onDone:"+errorCode+": "+errorString);
            }
        });
        System.out.println("LC1 Expect 'Not supported' error.");
        Assert.assertEquals("Freeze", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0).equals(new DatagramMessage(hereID,farID,new int[]{0x20, 0xA1, 0xEF})));
        messagesReceived.clear();
        dcs.put(new DatagramAcknowledgedMessage(farID,hereID), null);
        xmt.put(new InitializationCompleteMessage(farID), null);
        delay(200);
        Assert.assertEquals("PIPRequest", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0).equals(new ProtocolIdentificationRequestMessage(hereID,farID)));
        messagesReceived.clear();
        xmt.put(new ProtocolIdentificationReplyMessage(farID,hereID,0x400000000000L), null);
        xmt.dispose();
    }
    
    
    public void testLoaderClientDGPIPFail2() {
        data =new byte[80];
        LoaderClient xmt = new LoaderClient(testConnection, mcs, dcs);
        xmt.doLoad(hereID,farID, 0xEF, 0, data, new LoaderStatusReporter() {
            public void onProgress(float percent) {
                System.out.println("onProcess:"+percent);
            }
            public void onDone(int errorCode, String errorString) {
                System.out.println("onDone:"+errorCode+": "+errorString);
            }
        });
        System.out.println("LC2 Expect 'Not in Upgrade state' error.");
        Assert.assertEquals("Freeze", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0).equals(new DatagramMessage(hereID,farID,new int[]{0x20, 0xA1, 0xEF})));
        messagesReceived.clear();
        dcs.put(new DatagramAcknowledgedMessage(farID,hereID), null);
        xmt.put(new InitializationCompleteMessage(farID), null);
        delay(200);
        Assert.assertEquals("PIPRequest", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0).equals(new ProtocolIdentificationRequestMessage(hereID,farID)));
        messagesReceived.clear();
        xmt.put(new ProtocolIdentificationReplyMessage(farID,hereID,0x000010000000L), null);
        xmt.dispose();
    }
    
    
    public void testLoaderClientDGPIPFail3() {
        data =new byte[80];
        LoaderClient xmt = new LoaderClient(testConnection, mcs, dcs);
        xmt.doLoad(hereID,farID, 0xEF, 0, data, new LoaderStatusReporter() {
            public void onProgress(float percent) {
                System.out.println("onProcess:"+percent);
            }
            public void onDone(int errorCode, String errorString) {
                System.out.println("onDone:"+errorCode+": "+errorString);
            }
        });
        System.out.println("LC3 Expect 'Node does not support DGs/Streams.' error.");
        Assert.assertEquals("Freeze", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0).equals(new DatagramMessage(hereID,farID,new int[]{0x20, 0xA1, 0xEF})));
        messagesReceived.clear();
        dcs.put(new DatagramAcknowledgedMessage(farID,hereID), null);
        xmt.put(new InitializationCompleteMessage(farID), null);
        delay(200);
        Assert.assertEquals("PIPRequest", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0).equals(new ProtocolIdentificationRequestMessage(hereID,farID)));
        messagesReceived.clear();
        xmt.put(new ProtocolIdentificationReplyMessage(farID,hereID,0x000030000000L), null);
        xmt.dispose();
    }


    public void testLoaderClientDG() {
        data =new byte[80];
        LoaderClient xmt = new LoaderClient(testConnection, mcs, dcs);
        xmt.doLoad(hereID,farID, 0xEF, 0, data, new LoaderStatusReporter() {
            public void onProgress(float percent) {
                //System.out.println("onProcess:"+percent);
            }
            public void onDone(int errorCode, String errorString) {
                System.out.println("onDone:"+errorCode+": "+errorString);
            }
        });
    // Freeze
        Assert.assertEquals("Freeze", 1, messagesReceived.size());
                                //System.out.println("testLoaderClientDG freeze");
        Assert.assertTrue(messagesReceived.get(0).equals(new DatagramMessage(hereID,farID,new int[]{0x20, 0xA1, 0xEF})));
        messagesReceived.clear();
        dcs.put(new DatagramAcknowledgedMessage(farID,hereID), null);
        xmt.put(new InitializationCompleteMessage(farID), null);
        delay(200);
    // PIPRequest
        Assert.assertEquals("PIPRequest", 1, messagesReceived.size());
                                //System.out.println("testLoaderClientDG PIPRequest");
        Assert.assertTrue(messagesReceived.get(0).equals(new ProtocolIdentificationRequestMessage(hereID,farID)));
    // DGs ok
        messagesReceived.clear();
        xmt.put(new ProtocolIdentificationReplyMessage(farID,hereID,0x400030000000L), null);
    // First DG
        Assert.assertEquals("first DG", 1, messagesReceived.size());
                                //System.out.println("testLoaderClientDG first DG "+messagesReceived.size());
                                //System.out.println("Msg0: "+(messagesReceived.get(0) != null ? messagesReceived.get(0).toString() : " == null"));
        int[] data = new int[71];
        data[0]=0x20; data[1]=0; data[2]=0; data[3]=0; data[4]=0; data[5]=0; data[6]=0xEF;
        for(int i=7;i<71;i++) data[i]=0;
        Assert.assertTrue(messagesReceived.get(0).equals(new DatagramMessage(hereID,farID,data))); // DG ok
        messagesReceived.clear();
        dcs.put(new DatagramAcknowledgedMessage(farID,hereID),null);
    // Second DG
        Assert.assertEquals("second DG", 1, messagesReceived.size());
                                //System.out.println("DG2: "+(messagesReceived.get(0) != null ? messagesReceived.get(0).toString() : " == null"));
        data = new int[7+16];
        data[0]=0x20; data[1]=0; data[2]=0; data[3]=0; data[4]=0; data[5]=0x40; data[6]=0xEF;
        for(int i=7;i<(7+16);i++) data[i]=0;
        Assert.assertTrue(messagesReceived.get(0).equals(new DatagramMessage(hereID,farID,data))); // DG ok
        messagesReceived.clear();
        dcs.put(new DatagramAcknowledgedMessage(farID,hereID),null);
    // Unfreeze
        Assert.assertEquals("Unfreeze", 1, messagesReceived.size());
                                //System.out.println("Msg0: "+(messagesReceived.get(0) != null ? messagesReceived.get(0).toString() : " == null"));
        Assert.assertEquals(messagesReceived.get(0),new DatagramMessage(hereID,farID,
                new int []{0x20, 0xA0, 0xEF}));
        xmt.dispose();
    }

    
    
    public void testLoaderClientStream() {
        data = new byte[]{'a','b','c','d','e','f','g','h','i','j'};
        LoaderClient xmt = new LoaderClient(testConnection, mcs, dcs);
        xmt.doLoad(hereID,farID, 45, 0, data, new LoaderStatusReporter() {
            public void onProgress(float percent) {
                //System.out.println("onProcess:"+percent);
            }
            public void onDone(int errorCode, String errorString) {
                System.out.println("onDone:"+errorCode+": "+errorString);
            }
        });
    // Freeze
        Assert.assertEquals("Freeze", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0).equals(new DatagramMessage(hereID,farID,new int[]{0x20, 0xA1, 45})));
        messagesReceived.clear();
        dcs.put(new DatagramAcknowledgedMessage(farID,hereID), null);
        //messagesReceived.clear();
        xmt.put(new InitializationCompleteMessage(farID), null);
    // PIPRequest
        delay(200);
        Assert.assertEquals("PIPReq", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0).equals(new ProtocolIdentificationRequestMessage(hereID,farID))); // DGs ok
        messagesReceived.clear();
        xmt.put(new ProtocolIdentificationReplyMessage(farID,hereID,0x200030000000L), null);
    // McsWriteStream request
                                    //System.out.println(">>>test McsWriteStream request");
        Assert.assertEquals("McsWriteStream request", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0).equals(new DatagramMessage(hereID,farID,new int[]{0x20, 0x20, 0x00, 0x00, 0x00, 0x00, 0x2D, 0x04})));
        messagesReceived.clear();
        Message m = new DatagramAcknowledgedMessage(farID,hereID);
        dcs.put(m, null);
    // Stream Setup
        Assert.assertEquals("StreamSetup", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0).equals(new StreamInitiateRequestMessage(hereID,farID,64,(byte)4,(byte)0))); // Stream negn
        messagesReceived.clear();
        // *********** note small buffersize! **********
        xmt.put(new StreamInitiateReplyMessage(hereID,farID,6,(byte)4,(byte)6), null);
    // Stream Data
        Assert.assertEquals("stream data", 1, messagesReceived.size());
                                        //System.out.println("Msg0: "+(messagesReceived.get(0) != null ? messagesReceived.get(0).toString() : " == null"));
        Assert.assertTrue(messagesReceived.get(0).equals(new StreamDataSendMessage(hereID,farID,new int[]{'a','b','c','d','e','f'})));
        messagesReceived.clear();
        xmt.put(new StreamDataProceedMessage(farID,hereID,(byte)4,(byte)6),null);
        Assert.assertEquals("second stream data, stream complete, unfreeze", 3, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0).equals(new StreamDataSendMessage(hereID,farID,new int[]{'g','h','i','j'})));
        Assert.assertTrue(messagesReceived.get(1).equals(new StreamDataCompleteMessage(hereID,farID,(byte)4,(byte)6)));
    // Unfreeze
        Assert.assertTrue(messagesReceived.get(2).equals(new DatagramMessage(hereID,farID,new int[]{0x20, 0xA0, 45}))); // Unfreeze
        messagesReceived.clear();
        xmt.dispose();
    }


    public void testLoaderClientStream2() {
        data = new byte[]{'a','b','c','d','e','f','g','h','i','j'};
        LoaderClient xmt = new LoaderClient(testConnection, mcs, dcs);
        xmt.doLoad(hereID,farID, 45, 0, data, new LoaderStatusReporter() {
            public void onProgress(float percent) {
                //System.out.println("onProcess:"+percent);
            }
            public void onDone(int errorCode, String errorString) {
                System.out.println("onDone:"+errorCode+": "+errorString);
            }
        });
        // Freeze
        Assert.assertEquals("Freeze", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0).equals(new DatagramMessage(hereID,farID,new int[]{0x20, 0xA1, 45})));
        messagesReceived.clear();
        dcs.put(new DatagramAcknowledgedMessage(farID,hereID), null);
        //messagesReceived.clear();
        xmt.put(new InitializationCompleteMessage(farID), null);
        // PIPRequest
        delay(200);
        Assert.assertEquals("PIPReq", 1, messagesReceived.size());
                                      // System.out.println("Msg0: "+(messagesReceived.get(0) != null ? messagesReceived.get(0).toString() : " == null"));
        Assert.assertTrue(messagesReceived.get(0).equals(new ProtocolIdentificationRequestMessage(hereID,farID))); // DGs ok
        messagesReceived.clear();
        xmt.put(new ProtocolIdentificationReplyMessage(farID,hereID,0x200030000000L), null);
        // McsWriteStream request
        Assert.assertEquals("McsWriteStream request", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0).equals(new DatagramMessage(hereID,farID,new int[]{0x20, 0x20, 0x00, 0x00, 0x00, 0x00, 0x2D, 0x04})));
        messagesReceived.clear();
        Message m = new DatagramAcknowledgedMessage(farID,hereID);
        dcs.put(m, null);
        // Stream Setup
        Assert.assertEquals(new StreamInitiateRequestMessage(hereID, farID, 64, (byte)4, (byte)0), messagesReceived.get(0));
        messagesReceived.clear();
        // *********** note larger buffersize! **********
        xmt.put(new StreamInitiateReplyMessage(hereID,farID,64,(byte)4,(byte)6), null);
        // Stream Data
        Assert.assertEquals("stream data", 3, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0).equals(new StreamDataSendMessage(hereID,farID,new int[]{'a','b','c','d','e','f','g','h','i','j'})));
        // StreamComplete
        Assert.assertTrue(messagesReceived.get(1).equals(new StreamDataCompleteMessage(hereID,farID,(byte)4,(byte)6)));
        // Unfreeze
        Assert.assertTrue(messagesReceived.get(2).equals(new DatagramMessage(hereID,farID,new int[]{0x20, 0xA0, 45}))); // Unfreeze
        messagesReceived.clear();
        xmt.dispose();
    }


    // from here down is testing infrastructure

    private void delay(int msec) {
        long start = System.currentTimeMillis();
        while (true) {
            long left = start + msec - System.currentTimeMillis();
            if (left < 0) return;
            try {
                Thread.sleep(left);
            } catch(InterruptedException e) {}
        }
    }

    public LoaderClientTest(String s) {
        super(s);
    }
    
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LoaderClientTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }
    
    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LoaderClientTest.class);
        return suite;
    }
}






