package org.openlcb;

import org.openlcb.*;
import org.openlcb.implementations.*;
import org.openlcb.implementations.DatagramService.*;
import org.openlcb.implementations.MemoryConfigurationService.*;
import org.openlcb.implementations.StreamTransmitter.*;
import org.openlcb.implementations.DatagramTransmitter.*;
import org.openlcb.ProtocolIdentificationReplyMessage;
import org.openlcb.StreamInitiateReplyMessage;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

//
//  LoaderClient.java
//
//
//  Created by David on 2015-12-29.
//
//

//#include "LoaderClient.hpp"

public class LoaderClient extends MessageDecoder {
    enum State { IDLE, ABORT, FREEZE, INITCOMPL, PIP, SETUPSTREAM, STREAM, STREAMDATA, DG, UNFREEEZE, SUCCESS, FAIL };
    Connection connection;
    MemoryConfigurationService mcs;
    DatagramService dcs;
    MimicNodeStore store;

    State state;
    NodeID src;
    NodeID dest;
    int space;
    long address;
    byte[] content;
    LoaderStatusReporter feedback;
    
    public abstract class LoaderStatusReporter {
        public abstract void onProgress(float percent);
        public abstract void onDone(int errorCode, String errorString);
    }
    
    public LoaderClient(NodeID _src, NodeID _dest, int _space, long _address, byte[] _content, LoaderStatusReporter _feedback, Connection _connection, MemoryConfigurationService _mcs, DatagramService _dcs ) {
        src = _src;
        dest = _dest;
        space = _space;
        address = _address;
        content = _content.clone();
                                      // System.out.println("LoaderClient: content="+content);
        feedback = _feedback;
        connection = _connection;
        //dcs = new DatagramService(src, connection);
        //mcs = new MemoryConfigurationService(src, dcs);
        dcs = _dcs;
        mcs = _mcs;
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
      <--- stream data proceed
      ...
      <--- DatagramAcknowledged
     THEN:
      ---> UnFreeze
     
     */
    
    
    public void doLoad() {
        state = State.FREEZE;
        sendFreeze();  // allow restarts
    }
    
    final int DG_OK = 0x0000;     // double check these
    final int DG_FAIL = 0x0100;   // note that this value is used in DGMeteringBuffer to denote time-out
    final int DG_RESEND = 0x0200;
    void sendFreeze() {
                                      // System.out.println("lSendFreeze ");
        //DatagramTransmitter d0 = new DatagramTransmitter(src, dest, new int[]{0x20, 0xA1, space}, connection);
        
        dcs.sendData(
            new DatagramService.DatagramServiceTransmitMemo(dest, new int[]{0x20, 0xA1, space}) {
                @Override
                public void handleReply(int code) {
                                                  // System.out.println("Freeze handleReply: ");
                  if((state==State.FREEZE)) {
                      if(code==DG_OK)      { state = State.INITCOMPL; } // DG ok
                      else if(code==DG_FAIL) { state = State.INITCOMPL; } // DG timed out, but ok timeouts
                      //else if((code&DG_RESEND)!=0) { state = State.FREEZE; } // resend ok, so start again
                      else state = State.FAIL;    // Apparently this node doesn't handle DGs
                  } else state = State.FAIL;
                }
            });
    }
    @Override
    public void handleInitializationComplete(InitializationCompleteMessage msg, Connection sender){
                                      // System.out.println("lhandleInitializationComplete");
        if (state == State.FREEZE && msg.getSourceNodeID().equals(dest)) { state = State.PIP; sendPipRequest(); }
        if (state == State.INITCOMPL && msg.getSourceNodeID().equals(dest)) { state = State.PIP; sendPipRequest(); }
    }
    void sendPipRequest() {
                                      // System.out.println("lSendPipRequest ");
        Message msg = new ProtocolIdentificationRequestMessage(src, dest);
        connection.put(msg, this);
        //state = PIPREPLY;
    }
    @Override
    public void handleProtocolIdentificationReply(ProtocolIdentificationReplyMessage msg, Connection sender){
                                      // System.out.println("lhandleProtocolIdentificationReply");
        int retries = 0;
        if (state == State.PIP && msg.getSourceNodeID().equals(dest)) {
            if((msg.getValue()&0x00200000)!=0) {
                state=State.FAIL;
                feedback.onDone(0, "Loader: Target node should not be in Operating state.");
            }
            else if((msg.getValue()&0x00100000)==0) {
                state=State.FAIL; // not in FirmwareUpgrade Operating state
                feedback.onDone(0, "Loader: Target node is not in Firmware Upgrade state.");
            }
            else if((msg.getValue()&0x20000000)!=0) {
                state = State.SETUPSTREAM;
                setupStream();
            } else if((msg.getValue()&0x40000000)!=0) {
                state = State.DG;
                //feedback.onDone(0, "BollicksDG!?");
               sendDGs();
            } else {
                state = State.FAIL;
                feedback.onDone(0, "Loader: Target node does not support Streams nor Datagram!?");
            }
        }
    }

    int bufferSize;      // chunk size
    int startaddr;
    int endaddr;
    int totalmsgs;
    int sentmsgs;
    int location;
    int nextIndex;
    float progress;
    
    byte destStreamID;
    byte sourceStreamID = 4;  // notional value
    
    void setupStream() {
                                      // System.out.println("lSetup Stream ");
        bufferSize = 64;
        state = State.STREAM;
        
        mcs.request(new McsWriteStreamMemo(dest, space, address) {
            @Override
            public void handleWriteReply(int code) {
                                      // System.out.println("Reply mcs.request McsWriteStreamMemo handleWriteReply: ");
                sendStream();
            }
        });
    }
        
    void sendStream() {
                                      // System.out.println("lSend Stream ");
        StreamInitiateRequestMessage m = new StreamInitiateRequestMessage(src, dest, bufferSize, sourceStreamID, destStreamID);
        connection.put(m, this);
    }
    
    void handleStreamDataCompleteMessage() {
                                      // System.out.println("l>>>handleStreamDataCompleteMessage");
    }

    public void handleStreamInitiateReply(StreamInitiateReplyMessage msg, Connection sender){
                                      // System.out.println("handleStreamInitiateReply ");
        // pick up buffer size to use
        if(state==State.STREAM && sourceStreamID==msg.getSourceStreamID()) {
            this.bufferSize = msg.getBufferSize();
            this.destStreamID = msg.getDestinationStreamID();
            // init transfer
            nextIndex = 0;
            // send data
            state=State.STREAMDATA;
                                          // System.out.println("Stream proceed ");
            sendStreamNext();
        }
    }
    public void sendStreamNext() {
        int size = Math.min(bufferSize, content.length-nextIndex);
        byte[] data = new byte[size];
        // copy the needed data
        for (int i=0; i<size; i++) data[i] = content[nextIndex+i];
        nextIndex = nextIndex+size;
                                         // System.out.println("\nsendStreamNext: "+data);
        Message m = new StreamDataSendMessage(src, dest, data, destStreamID);
        connection.put(m, this);
        // are we done?
        if (nextIndex < content.length) return; // wait for Data Proceed message
        // yes, say we're done
        m = new StreamDataCompleteMessage(src, dest, sourceStreamID, destStreamID);
        connection.put(m, this);
        sendUnfreeze();
        state = State.SUCCESS;
    }
    public void handleStreamDataProceed(StreamDataProceedMessage msg, Connection sender){
                                      // System.out.println("handleStreamDataProceed");
        sendStreamNext();
    }
    
    void sendDGs() {
                                      // System.out.println("\nlsendDGs: ");
        nextIndex = 0;
        bufferSize = 8;
        sendDGNext();
    }
    void sendDGNext() {
        int size = Math.min(bufferSize, content.length-nextIndex);
                                      // System.out.println("lbufferSize: "+bufferSize);
                                      // System.out.println("lsize: "+size);
            int[] data = new int[size];
            // copy the needed data
            for (int i=0; i<size; i++) data[i] = content[nextIndex+i];
            nextIndex = nextIndex+size;
        dcs.sendData(new DatagramService.DatagramServiceTransmitMemo(dest, data) {
            @Override
            public void handleReply(int code) {
                                              // System.out.println("lDG handleReply");
                sendDGNext();
            }
        });
            // are we done?
        if( nextIndex < content.length ) return;
        sendUnfreeze();
        state = State.SUCCESS;

    }
    
    void sendUnfreeze() {
                                      // System.out.println("lsendUnfreeze");
        dcs.sendData(new DatagramService.DatagramServiceTransmitMemo(dest, new int[]{0x20, 0xA0, space}) {
            public void handleReply(int code) {
            }
        });
    }
}