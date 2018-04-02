package org.openlcb;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.openlcb.implementations.DatagramService;
import org.openlcb.implementations.MemoryConfigurationService;
import org.openlcb.implementations.MemoryConfigurationService.McsWriteHandler;
import org.openlcb.implementations.MemoryConfigurationService.McsWriteStreamMemo;

//
//  LoaderClient.java
//
//
//  Created by David on 2015-12-29.
//
//

public class LoaderClient extends MessageDecoder {
    private static final Logger logger = Logger.getLogger(LoaderClient.class.getName());

    enum State { IDLE, ABORT, FREEZE, INITCOMPL, PIP, PIPREPLY, SETUPSTREAM, STREAM, STREAMDATA, DG, UNFREEEZE, SUCCESS, FAIL };
    Connection connection;
    Connection fromDownstream;
    MemoryConfigurationService mcs;
    DatagramService dcs;
    MimicNodeStore store;
    String errorString;

    State state;
    NodeID src;
    NodeID dest;
    int space;
    long address;
    byte[] content;
    LoaderStatusReporter feedback;

    private static final int ERR_CHECKSUM_FAILED = 0x2088;
    private static final int ERR_FILE_CORRUPTED = 0x1089;
    private static final int ERR_FILE_INAPPROPRIATE = 0x1088;

    public static abstract class LoaderStatusReporter {
        public abstract void onProgress(float percent);
        public abstract void onDone(int errorCode, String errorString);
    }
    
    public LoaderClient( Connection _connection, MemoryConfigurationService _mcs, DatagramService _dcs ) {
                                       //System.out.println("LoaderClient init");
        connection = _connection;
        dcs = _dcs;
        mcs = _mcs;
        timer = new Timer("OpenLCB LoaderClient Timeout Timer");
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
    
    
    public void doLoad(NodeID _src, NodeID _dest, int _space, long _address, byte[] _content, LoaderStatusReporter _feedback) {
        src = _src;
        dest = _dest;
        space = _space;
        address = _address;
        content = _content;
        state = State.IDLE;
        feedback = _feedback;
        sendFreeze();  // allow restarts
    }
    
    final int DG_OK = 0x0000;     // double check these
    final int DG_FAIL = 0x0100;   // note that this value is used in DGMeteringBuffer to denote time-out
    final int DG_RESEND = 0x0200;
    void sendFreeze() {
                                                // System.out.println("lSendFreeze ");
        state = State.FREEZE;
                                                // System.out.println("lsendFREEZE Enter: "+state);
        dcs.sendData(
            new DatagramService.DatagramServiceTransmitMemo(dest, new int[]{0x20, 0xA1, space}) {
                @Override
                public void handleSuccess(int flags) {
                    if(state==State.FREEZE) {
                        state = State.PIP;
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                sendPipRequest();
                                startTimeout(3000);
                            }
                        }, 130);
                    } else {
                        // ignore; maybe a late timeout callback.
                    }
                }

                @Override
                public void handleFailure(int errorCode) {
                    // It is actually OK to have this fail because the remote node may have
                    // rebooted.
                    handleSuccess(0);
                }
            });
    }

    private Timer timer;
    private TimerTask task = null;
    void startTimeout(int period) {
        task = new TimerTask(){
            public void run(){
                timerExpired();
            }
        };
        timer.schedule(task, period);
    }
    void endTimeout() {
        if (task != null) task.cancel();
        else {
            state = State.FAIL;
            
        }
        task = null;
    }
    void timerExpired() {
        failWith(1, "Timed out");
    }

    //@Override
    public void handleInitializationComplete(InitializationCompleteMessage msg, Connection sender){
                                        //System.out.println("lhandleInitializationComplete state: "+state);
//        if (state == State.FREEZE && msg.getSourceNodeID().equals(dest)) { state = State.PIP; sendPipRequest(); }
//        if (state == State.INITCOMPL && msg.getSourceNodeID().equals(dest)) { state = State.PIP; sendPipRequest(); }
    }
    void sendPipRequest() {
        state = State.PIPREPLY;
                                       //System.out.println("lSendPipRequest "+state);
        Message msg = new ProtocolIdentificationRequestMessage(src, dest);
        connection.put(msg, this);
    }
    @Override
    public void handleProtocolIdentificationReply(ProtocolIdentificationReplyMessage msg, Connection sender){
                                       //System.out.println("lhandleProtocolIdentificationReply Enter:"+state);
                                       // System.out.println("lmsg.getSourceNodeID():"+msg.getSourceNodeID());
                                //System.out.println("lmsg.getValue():"+String.format("0x%12X",msg.getValue()));
        endTimeout();
        if (state == State.PIPREPLY && msg.getSourceNodeID().equals(dest)) {
            if((msg.getValue()&0x000010000000L)==0) {
                state=State.FAIL;
                errorString = "Target not in Upgrade state.";
            }
            else if((msg.getValue()&0x200000000000L)!=0) {
                state = State.SETUPSTREAM;
                                    //System.out.println("lStream ok:"+state);
                setupStream();
            } else if((msg.getValue()&0x400000000000L)!=0) {
                state = State.DG;
                                    //System.out.println("lDGs ok:"+state);
               sendDGs();
            } else {
                state = State.FAIL;
                errorString = "Target has no Streams nor Datagrams!";
            }
        }
                                    //System.out.println("lhandleProtocolIdentificationReply Exit:"+state);
    }

    int bufferSize;      // chunk size
    int startaddr;
    int endaddr;
    int totalmsgs;
    int sentmsgs;
    int location;
    int nextIndex;
    private int errorCounter;
    float progress;
    float replyCount;
    float expectedTransactions;
    byte destStreamID;
    byte sourceStreamID = 4;  // notional value

// ============================= STREAMS ==============================================
    void setupStream() {
                                      // System.out.println("lSetup Stream ");
        bufferSize = 64;
        state = State.STREAM;
        
        mcs.request(new McsWriteStreamMemo(dest, space, address, 4) {
            @Override
            public void handleSuccess() {
                sendStream();
            }

            @Override
            public void handleFailure(String where, int errorCode) {
                state = State.FAIL;
                logger.warning("Failed to setup stream at " + where + ": error 0x" + Integer
                        .toHexString(errorCode));
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
        int[] data = new int[size];
        // copy the needed data
        for (int i=0; i<size; i++) data[i] = content[nextIndex+i];
                                         // System.out.println("\nsendStreamNext: "+data);
        Message m = new StreamDataSendMessage(src, dest, data);
        connection.put(m, this);
        // are we done?
        nextIndex = nextIndex+size;
        feedback.onProgress(100.0F * (float)nextIndex / (float)content.length);
        if (nextIndex < content.length) return; // wait for Data Proceed message
        // yes, say we're done
        m = new StreamDataCompleteMessage(src, dest, sourceStreamID, destStreamID);
        connection.put(m, this);
        sendUnfreeze();
        state = State.SUCCESS;
    }
    //public void sendStreamComplete() {
        //connection.put(new StreamDataCompleteMessage(src, dest, sourceStreamID, destStreamID), this);
    //    sendUnfreeze();
    //}
    public void handleStreamDataProceed(StreamDataProceedMessage msg, Connection sender){
                                      // System.out.println("handleStreamDataProceed");
        sendStreamNext();
    }
    
    
// ============================= DATAGRAMS ==============================================
    void sendDGs() {
                                       //System.out.println("\nlsendDGs: ");
        nextIndex = 0;
        bufferSize = 64;
        replyCount = 0;
        errorCounter = 0;
        expectedTransactions = content.length / bufferSize;
        sendDGNext();
    }
    void sendDGNext() {
        final int size = Math.min(bufferSize, content.length-nextIndex);
                                    //System.out.println("lsendDGNext Enter: "+state);
                                    //System.out.println("content.length: "+content.length);
                                    //System.out.println("nextIndex: "+nextIndex);
                                    //System.out.println("lbufferSize: "+bufferSize);
                                    //System.out.println("lsize: "+size);
        byte[] data = new byte[size];
        // copy the needed data
        for (int i=0; i<size; i++) data[i] = content[nextIndex+i];
        
        mcs.requestWrite(dest, space, nextIndex, data, new McsWriteHandler() {
            @Override
            public void handleFailure(int errorCode) {
                if (++errorCounter > 3) {
                    failWith(errorCode, "Repeated errors writing to firmware space.");
                } else {
                    sendDGNext();
                }
            }

            @Override
            public void handleSuccess() {
                nextIndex += size;
                errorCounter = 0;
                float p = 100.0F * nextIndex / content.length;
                feedback.onProgress(p);
                if(nextIndex<content.length) sendDGNext();
                else {
                    state = State.SUCCESS;
                    sendUnfreeze();
                }
            }
        });
    }

    void sendUnfreeze() {
        dcs.sendData(new DatagramService.DatagramServiceTransmitMemo(dest, new int[]{0x20, 0xA0, space}) {

            @Override
            public void handleSuccess(int flags) {
                if (state == State.SUCCESS) {
                    feedback.onProgress((float) 100.0);
                    feedback.onDone(0, "");
                }
            }

            @Override
            public void handleFailure(int errorCode) {
                if (errorCode == DatagramRejectedMessage.DATAGRAM_REJECTED_DST_REBOOT) {
                    // that's ok
                    handleSuccess(0);
                } else if (state == State.SUCCESS){
                    failWith(errorCode, "Download Failed in UnFreeze");
                } // else we already reported a failure
            }

        });
    }

    private void failWith(int errorCode, String errorString) {
        boolean b = (state != State.FAIL && state != State.UNFREEEZE);
        state = State.FAIL;
        String tmpString = null;
        if (errorCode == ERR_CHECKSUM_FAILED) {
            tmpString = "Failed download checksum; try again";
        } else if (errorCode == ERR_FILE_CORRUPTED) {
            tmpString = "File corrupted";
        } else if (errorCode == ERR_FILE_INAPPROPRIATE) {
            tmpString = "The firmware data is incompatible with this hardware node";
        }
        if (tmpString != null) {
            errorString = tmpString + " - " + errorString;
        }
        feedback.onDone(errorCode, errorString);
        if (b) {
            sendUnfreeze();
        }
    }

    /*
     * clean up local storage
     */
    public void dispose(){
       timer.cancel();
    }
}
