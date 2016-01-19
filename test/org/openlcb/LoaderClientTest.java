package org.openlcb;

import org.openlcb.*;
import org.openlcb.implementations.*;
import org.openlcb.LoaderClient;
//import org.openlcb.DatagramAcknowledged;

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

    public void testLoaderClientDG() {
        reporter = null;

        data =new byte[]{1,2,3,4,5,6,7,8,9,10};
        LoaderClient xmt = new LoaderClient( hereID,farID, 45, 0, data, reporter, testConnection, mcs, dcs);
        xmt.doLoad();
    // Freeze
        Assert.assertEquals("Freeze", 1, messagesReceived.size());
                                // System.out.println("testLoaderClientDG freeze");
        Assert.assertTrue(messagesReceived.get(0).equals(new DatagramMessage(hereID,farID,new int[]{0x20, 0xA1, 45})));
        messagesReceived.clear();
        dcs.put(new DatagramAcknowledgedMessage(farID,hereID), null);
        xmt.put(new InitializationCompleteMessage(farID), null);
    // PIPRequest
        Assert.assertEquals("PIPRequest", 1, messagesReceived.size());
                                //System.out.println("testLoaderClientDG PIPRequest");
        Assert.assertTrue(messagesReceived.get(0).equals(new ProtocolIdentificationRequestMessage(hereID,farID))); // DGs ok
        messagesReceived.clear();
        xmt.put(new ProtocolIdentificationReplyMessage(farID,hereID,0x40100000), null);
    // First DG
        Assert.assertEquals("first DG", 1, messagesReceived.size());
                                //System.out.println("testLoaderClientDG first DG "+messagesReceived.size());
                                //System.out.println("Msg0: "+(messagesReceived.get(0) != null ? messagesReceived.get(0).toString() : " == null"));
        Assert.assertTrue(messagesReceived.get(0).equals(new DatagramMessage(hereID,farID,new byte[]{1,2,3,4,5,6,7,8}))); // DGs ok
        messagesReceived.clear();
        dcs.put(new DatagramAcknowledgedMessage(farID,hereID),null);
    // Second DG
        Assert.assertEquals("second DG", 2, messagesReceived.size());
                                //System.out.println("DG2: "+(messagesReceived.get(0) != null ? messagesReceived.get(0).toString() : " == null"));
                                //System.out.println("Unfreeze: "+(messagesReceived.get(1) != null ? messagesReceived.get(1).toString() : " == null"));
        Assert.assertTrue(messagesReceived.get(0).equals(new DatagramMessage(hereID,farID,new byte[]{9,10}))); // DGs ok
        xmt.put(new DatagramAcknowledgedMessage(farID,hereID),null);
    // Unfreeze
        Assert.assertTrue(messagesReceived.get(1).equals(new DatagramMessage(hereID,farID,new int[]{0x20, 0xA0, 45})));
    }


    
    public void testLoaderClientStream() {

        reporter = null;
        //new LoaderClient.LoaderStatusReporter
        //(
        //    void onProgress(float percent){};
        //    void onDone(int errorCode, String errorString){};
        //);
        data = new byte[]{'a','b','c','d','e','f','g','h','i','j'};
        LoaderClient xmt = new LoaderClient( hereID,farID, 45, 0, data, reporter, testConnection, mcs, dcs);
        xmt.doLoad();
    // Freeze
        Assert.assertEquals("Freeze", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0).equals(new DatagramMessage(hereID,farID,new int[]{0x20, 0xA1, 45})));
        messagesReceived.clear();
        dcs.put(new DatagramAcknowledgedMessage(farID,hereID), null);
        messagesReceived.clear();
        xmt.put(new InitializationCompleteMessage(farID), null);
    // PIPRequest
        Assert.assertEquals("PIPReq", 1, messagesReceived.size());
                                   // System.out.println("Msg0: "+(messagesReceived.get(0) != null ? messagesReceived.get(0).toString() : " == null"));
        Assert.assertTrue(messagesReceived.get(0).equals(new ProtocolIdentificationRequestMessage(hereID,farID))); // DGs ok
        messagesReceived.clear();
        xmt.put(new ProtocolIdentificationReplyMessage(farID,hereID,0x20100000), null);
    // McsWriteStream request
                                    //System.out.println(">>>test McsWriteStream request");
        Assert.assertEquals("McsWriteStream request", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0).equals(new DatagramMessage(hereID,farID,new int[]{0x20, 0x20, 0x00, 0x00, 0x00, 0x00, 0x2D, 0x04})));
        messagesReceived.clear();
        Message m = new DatagramAcknowledgedMessage(farID,hereID);
        dcs.put(m, null);
    // Stream Setup
                                      // System.out.println(">>>test Stream Setup");
        Assert.assertEquals("StreamSetup", 1, messagesReceived.size());
                                      // System.out.println("Msg0: "+(messagesReceived.get(0) != null ? messagesReceived.get(0).toString() : " == null"));
        Assert.assertTrue(messagesReceived.get(0).equals(new StreamInitiateRequestMessage(hereID,farID,64,(byte)4,(byte)0))); // Stream negn
        messagesReceived.clear();
        // *********** note small buffersize! **********
                                      // System.out.println("tStreamInitiateReplyMessage");
        xmt.put(new StreamInitiateReplyMessage(hereID,farID,6,(byte)4,(byte)6), null);
    // Stream Data
                                      // System.out.println(">>>Stream Data");
        Assert.assertEquals("stream data", 1, messagesReceived.size());
                                      // System.out.println("Msg0: "+(messagesReceived.get(0) != null ? messagesReceived.get(0).toString() : " == null"));
        Assert.assertTrue(messagesReceived.get(0).equals(new StreamDataSendMessage(hereID,farID,new byte[]{'a','b','c','d','e','f'},(byte)6)));
        messagesReceived.clear();
                                      // System.out.println(">>>StreamDataProceedMessage");
        xmt.put(new StreamDataProceedMessage(farID,hereID,(byte)4,(byte)6),null);
        Assert.assertEquals("second stream data, stream complete, unfreeze", 3, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0).equals(new StreamDataSendMessage(hereID,farID,new byte[]{'g','h','i','j'},(byte)6)));
        Assert.assertTrue(messagesReceived.get(1).equals(new StreamDataCompleteMessage(hereID,farID,(byte)4,(byte)6)));
    // Unfreeze
        Assert.assertTrue(messagesReceived.get(2).equals(new DatagramMessage(hereID,farID,new int[]{0x20, 0xA0, 45}))); // Unfreeze
        messagesReceived.clear();
    }

    
    public void testLoaderClientStream2() {
        
        reporter = null;
        //new LoaderClient.LoaderStatusReporter
        //(
        //    void onProgress(float percent){};
        //    void onDone(int errorCode, String errorString){};
        //);
        data = new byte[]{'a','b','c','d','e','f','g','h','i','j'};
        LoaderClient xmt = new LoaderClient( hereID,farID, 45, 0, data, reporter, testConnection, mcs, dcs);
        xmt.doLoad();
        // Freeze
        Assert.assertEquals("Freeze", 1, messagesReceived.size());
        Assert.assertTrue(messagesReceived.get(0).equals(new DatagramMessage(hereID,farID,new int[]{0x20, 0xA1, 45})));
        messagesReceived.clear();
        dcs.put(new DatagramAcknowledgedMessage(farID,hereID), null);
        messagesReceived.clear();
        xmt.put(new InitializationCompleteMessage(farID), null);
        // PIPRequest
        Assert.assertEquals("PIPReq", 1, messagesReceived.size());
                                      // System.out.println("Msg0: "+(messagesReceived.get(0) != null ? messagesReceived.get(0).toString() : " == null"));
        Assert.assertTrue(messagesReceived.get(0).equals(new ProtocolIdentificationRequestMessage(hereID,farID))); // DGs ok
        messagesReceived.clear();
        xmt.put(new ProtocolIdentificationReplyMessage(farID,hereID,0x20100000), null);
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
        Assert.assertTrue(messagesReceived.get(0).equals(new StreamDataSendMessage(hereID,farID,new byte[]{'a','b','c','d','e','f','g','h','i','j'},(byte)6)));
        // StreamComplete
        Assert.assertTrue(messagesReceived.get(1).equals(new StreamDataCompleteMessage(hereID,farID,(byte)4,(byte)6)));
        // Unfreeze
        Assert.assertTrue(messagesReceived.get(2).equals(new DatagramMessage(hereID,farID,new int[]{0x20, 0xA0, 45}))); // Unfreeze
        messagesReceived.clear();
    }
    
 
    // from here down is testing infrastructure
    
    public LoaderClientTest(String s) {
        super(s);
    }
    
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LoaderClientTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }
    
    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LoaderClientTest.class);
        return suite;
    }
}






