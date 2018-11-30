package org.openlcb;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.openlcb.implementations.DatagramService;
import org.openlcb.implementations.MemoryConfigurationService;
import org.openlcb.implementations.MemoryConfigurationService.McsWriteHandler;
import org.openlcb.implementations.MemoryConfigurationService.McsWriteStreamMemo;

import static org.openlcb.ProtocolIdentification.Protocol;

//
//  LoaderClient.java
//
//
//  Created by David on 2015-12-29.
//
//

public class LoaderClient extends MessageDecoder {
    private static final Logger logger = Logger.getLogger(LoaderClient.class.getName());
    private static final byte SRC_STREAM_ID = 4;
    private static final int PIP_TIMEOUT_MSEC = 3000;
    private static final int FREEZE_REBOOT_TIMEOUT_MSEC = 60000;
    private static final int STREAM_INIT_TIMEOUT_MSEC = 10000;
    private static final int STREAM_DATA_PROCEED_TIMEOUT_MSEC = 120000;

    enum State { IDLE, ABORT, FREEZE, INITCOMPL, PIP, PIPREPLY, SETUPSTREAM, STREAM, STREAMDATA, DG, UNFREEEZE, SUCCESS, FAIL };

    // These objects are injected in the constructor.
    Connection connection;
    MemoryConfigurationService mcs;
    DatagramService dcs;

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

    private void sendFreeze() {
        state = State.FREEZE;
        dcs.sendData(
            new DatagramService.DatagramServiceTransmitMemo(dest, new int[]{0x20, 0xA1, space}) {
                // Ignores both success and failure callback, because the state machine will
                // proceed on the Node Init Complete message below.
                @Override
                public void handleSuccess(int flags) {
                }

                @Override
                public void handleFailure(int errorCode) {
                }
            });
        state = State.INITCOMPL;
        startTimeout(FREEZE_REBOOT_TIMEOUT_MSEC);
    }

    private Timer timer;
    private TimerTask task = null;
    private void startTimeout(int period_msec) {
        task = new TimerTask(){
            public void run(){
                timerExpired();
            }
        };
        timer.schedule(task, period_msec);
    }
    private void endTimeout() {
        if (task != null) task.cancel();
        else {
            state = State.FAIL;
        }
        task = null;
    }
    private void timerExpired() {
        failWith(1, "Timed out in state " + state.name());
    }

    /**
     * Checks if a message is coming back from the target node to us.
     * @param msg an addressed message
     * @return true iff the addressed message is coming from the loader target and addressed to
     * the loader client node.
     */
    private boolean isReply(Message msg) {
        if (!msg.getSourceNodeID().equals(dest)) {
            return false;
        }
        if (msg instanceof AddressedMessage) {
            if (!((AddressedMessage) msg).getDestNodeID().equals(src)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void handleInitializationComplete(InitializationCompleteMessage msg, Connection sender){
                                        //System.out.println("lhandleInitializationComplete state: "+state);
        if (state == State.INITCOMPL && isReply(msg)) {
            endTimeout();
            state = State.PIP;
            sendPipRequest();
        }
        if (state == State.SUCCESS && isReply(msg)) {
            state = State.IDLE;
        }
    }
    private void sendPipRequest() {
        state = State.PIPREPLY;
        Message msg = new ProtocolIdentificationRequestMessage(src, dest);
        connection.put(msg, this);
        startTimeout(PIP_TIMEOUT_MSEC);
    }
    @Override
    public void handleProtocolIdentificationReply(ProtocolIdentificationReplyMessage msg, Connection sender){
        if (state == State.PIPREPLY && isReply(msg)) {
            endTimeout();
            ProtocolIdentification pi = new ProtocolIdentification(msg.getSourceNodeID(), msg);
            if(!pi.hasProtocol(Protocol.FirmwareUpgradeActive)) {
                failWith(1, "Target not in Upgrade state.");
            } else if(pi.hasProtocol(Protocol.Stream)) {
                state = State.SETUPSTREAM;
                setupStream();
            } else if(pi.hasProtocol(Protocol.Datagram)) {
                state = State.DG;
                sendDGs();
            } else {
                failWith(1, "Target has no Streams nor Datagrams!");
            }
        }
    }

    private int bufferSize;      // chunk size
    private int nextIndex;
    private int errorCounter;
    private byte destStreamID;

    // ============================= STREAMS ==============================================
    private void setupStream() {
        bufferSize = 16384;
        state = State.STREAM;

        mcs.request(new McsWriteStreamMemo(dest, space, address, SRC_STREAM_ID) {
            @Override
            public void handleSuccess() {
                sendStream();
            }

            @Override
            public void handleFailure(String where, int errorCode) {
                String f = "Failed to setup stream at " + where + ": error 0x" + Integer
                        .toHexString(errorCode);
                logger.warning(f);
                failWith(errorCode, f);
            }
        });
    }

    private void sendStream() {
                                      // System.out.println("lSend Stream ");
        // @todo the destStreamID is probably bogus at this point. Check why it is needed here.
        StreamInitiateRequestMessage m = new StreamInitiateRequestMessage(src, dest, bufferSize, SRC_STREAM_ID, destStreamID);
        connection.put(m, this);
        startTimeout(STREAM_INIT_TIMEOUT_MSEC);
    }

    @Override
    public void handleStreamInitiateReply(StreamInitiateReplyMessage msg, Connection sender){
                                      // System.out.println("handleStreamInitiateReply ");
        // pick up buffer size to use
        if(state==State.STREAM && isReply(msg) && SRC_STREAM_ID == msg.getSourceStreamID()) {
            endTimeout();
            this.bufferSize = msg.getBufferSize();
            this.destStreamID = msg.getDestinationStreamID();
            // init transfer
            nextIndex = 0;
            // send data
            state=State.STREAMDATA;
            sendStreamNext();
        }
    }
    private void sendStreamNext() {
        int size = Math.min(bufferSize, content.length-nextIndex);
        int[] data = new int[size];
        // copy the needed data
        for (int i=0; i<size; i++) data[i] = content[nextIndex+i];
                                         // System.out.println("\nsendStreamNext: "+data);
        Message m = new StreamDataSendMessage(src, dest, destStreamID, data);
        connection.put(m, this);
        // are we done?
        nextIndex = nextIndex+size;
        feedback.onProgress(100.0F * (float)nextIndex / (float)content.length);
        if (nextIndex < content.length) {
            startTimeout(STREAM_DATA_PROCEED_TIMEOUT_MSEC);
            return; // wait for Data Proceed message
        }
        // yes, say we're done
        m = new StreamDataCompleteMessage(src, dest, SRC_STREAM_ID, destStreamID);
        connection.put(m, this);
        sendUnfreeze();
        state = State.SUCCESS;
    }

    @Override
    public void handleStreamDataProceed(StreamDataProceedMessage msg, Connection sender){
                                      // System.out.println("handleStreamDataProceed");
        if (state == State.STREAMDATA && isReply(msg)) {
            endTimeout();
            sendStreamNext();
        }
    }


    // ============================= DATAGRAMS ==============================================
    private void sendDGs() {
                                       //System.out.println("\nlsendDGs: ");
        nextIndex = 0;
        bufferSize = 64;
        errorCounter = 0;
        sendDGNext();
    }

    private void sendDGNext() {
        final int size = Math.min(bufferSize, content.length-nextIndex);
                                    //System.out.println("lsendDGNext Enter: "+state);
                                    //System.out.println("content.length: "+content.length);
                                    //System.out.println("nextIndex: "+nextIndex);
                                    //System.out.println("lbufferSize: "+bufferSize);
                                    //System.out.println("lsize: "+size);
        byte[] data = new byte[size];
        // copy the needed data
        System.arraycopy(content, nextIndex, data, 0, size);

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

    private void sendUnfreeze() {
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
