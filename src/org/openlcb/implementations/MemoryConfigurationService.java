package org.openlcb.implementations;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.openlcb.FailureCallback;
import org.openlcb.NoReturnCallback;
import org.openlcb.NodeID;
import org.openlcb.Utilities;

/**
 * Service for reading and writing via the Memory Configuration protocol
 * <p>
 * Meant to shield the using code from all the details of that
 * process via read and write primitives.
 * <p>
 * Can accept requests without the using code having to serialize them.
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @author  David Harris   Copyright 2016
 * @version $Revision: -1 $
 */
public class MemoryConfigurationService {
    private static final Logger logger = Logger.getLogger(MemoryConfigurationService.class.getName());
    private static final int DATAGRAM_TYPE = 0x20;

    public static final int SPACE_CDI = 0xFF;
    public static final int SPACE_ALL_MEM = 0xFE;
    public static final int SPACE_CONFIG = 0xFD;
    public static final int SPACE_ACDI_CONST = 0xFC;
    public static final int SPACE_ACDI_USER = 0xFB;
    public static final int SPACE_TRACTION_FDI = 0xFA;
    public static final int SPACE_TRACTION_FUNCTION = 0xF9;

    private static final int SUBCMD_REPLY = 0x10;
    private static final int SUBCMD_ERROR = 0x08;

    private static final int SUBCMD_WRITE = 0x00;
    private static final int SUBCMD_WRITE_STREAM = 0x20;
    private static final int SUBCMD_READ = 0x40;
    private static final int SUBCMD_READ_STREAM = 0x60;
    private final static long TIMEOUT = 3000;
    private long timeoutMillis = TIMEOUT;
    private final static long MAX_TRIES = 3;

    /**
     * @param here       our node ID
     * @param downstream Connection in the direction of the layout
     */
    public MemoryConfigurationService(NodeID here, DatagramService downstream) {
        retryTimer = new Timer("OpenLCB Memory Configuration Service Retry Timer");
        this.here = here;
        this.downstream = downstream;   
        
        // connect to be notified of config service
        downstream.registerForReceive(new DatagramService.DatagramServiceReceiveMemo
                (DATAGRAM_TYPE) {
            // Process a datagram received here; previous request state part of decoding
            //
            // does not allow for overlapping operations, either to a single node nor or multiple
            // types
            // nor to multiple nodes
            //
            // doesn't check for match of reply to memo, but eventually should.
            @Override
            public synchronized void handleData(NodeID dest, int[] data, DatagramService.ReplyMemo
                    service) {
                //log System.out.println("OLCB: handleData");
                service.acceptData(0);
                if (addrSpaceMemo != null) {
                    // doesn't handle decode of desc string, but should
                    int space = data[2] & 0xFF;
                    long highAddress = (((long) data[3] & 0xFF) << 24) | (((long) data[4] & 0xFF)
                            << 16) | (((long) data[5] & 0xFF) << 8) | ((long) data[6] & 0xFF);
                    int flags = data[7] & 0xFF;
                    long lowAddress = 0;
                    if (data.length >= 11)
                        lowAddress = (((long) data[8] & 0xFF) << 24) | (((long) data[9] & 0xFF)
                                << 16) | (((long) data[10] & 0xFF) << 8) | ((long) data[11] & 0xFF);

                    McsAddrSpaceMemo memo = addrSpaceMemo;
                    addrSpaceMemo = null;
                    memo.handleAddrSpaceData(dest, space, highAddress, lowAddress, flags, "");
                    return;
                }
                // config memo may trigger address space read, so do second
                if (configMemo != null) {
                    // doesn't handle decode of name string, but should
                    int commands = (data[2] << 8) + data[3];
                    int options = data[4];
                    int highSpace = data[5];
                    int lowSpace = data[6];
                    McsConfigMemo memo = configMemo;
                    configMemo = null;
                    memo.handleConfigData(dest, commands, options, highSpace, lowSpace, "");
                    return;
                }
                /*
                if (writeMemo != null && ((data[1] & 0xF0) == 0x10)) {
                    McsWriteMemo memo = writeMemo;
                    long retAddress = DatagramUtils.parseLong(data, 2);
                    if (retAddress != memo.address) {
                        logger.warning("Spurious write response datagram. Requested address=" +
                                memo.address + " returned address=" + retAddress);
                        return;
                    }
                    writeMemo = null;
                    boolean spaceByte = ((data[1] & 0x03) == 0);
                    boolean failed = (data[1] & 0x08) != 0;
                    int codeOffset = 6;
                    if (spaceByte) ++codeOffset;
                    int code = failed ? 0x1000 : 0;
                    if (data.length >= codeOffset + 2) {
                        code = (data[codeOffset] << 8) + data[codeOffset + 1];
                    }
                    memo.handleWriteReply(code);
                }*/
                if (writeStreamMemo != null) {
                    // figure out address space uses byte?
                    boolean spaceByte = ((data[1] & 0x03) == 0);
                    int spaceOfs = spaceByte ? 1 : 0;
                    McsWriteStreamMemo memo = writeStreamMemo;
                    writeStreamMemo = null;
                    // TODO: compare the incoming parameters to the information in the memo.
                    if ((data[1] & 0x08) == 0) {
                        // OK
                        memo.handleSuccess();
                    } else {
                        // error
                        memo.handleFailure("WriteStreamReply", DatagramUtils.parseErrorCode(data,
                                6 + spaceOfs));
                    }
                    return;
                }
                int requestCode = getRequestTypeFromResponseType(data[1]);
                RequestWithReplyDatagram memo = null;
                McsRequestMemo rqMemo = null;
                synchronized (this) {
                    if (pendingRequests.containsKey(requestCode)) {
                        rqMemo = pendingRequests.get(requestCode);
                        if (!rqMemo.getDest().equals(dest)) {
                            logger.warning("Spurious MemCfg response datagram " + Utilities
                                    .toHexSpaceString(data)+": expected source " + rqMemo.getDest()
                                    + " actual source " + dest);
                            delayRetryMemo(rqMemo);
                            return;
                        }
                        if (!(rqMemo instanceof RequestWithReplyDatagram)) {
                            logger.warning("Spurious MemCfg response datagram " + Utilities.toHexSpaceString(data)+
                                    ": the request memo does not support response datagrams. " +
                                    "Memo: " + rqMemo);
                            delayRetryMemo(rqMemo);
                            return;
                        }
                        memo = (RequestWithReplyDatagram) rqMemo;
                        if (!memo.compareResponse(data)) {
                            logger.warning("Unexpected MemCfg response datagram from " + dest
                                    .toString() + ": " + memo + " payload " + Utilities
                                    .toHexSpaceString(data));
                            delayRetryMemo(rqMemo);
                            return;
                        } else {
                            checkAndPopMemo(rqMemo);
                        }
                    } else {
                        logger.warning("Could not find a matching memo for MemCfg response " +
                                "datagram from " + dest.toString() + " payload " + Utilities
                                .toHexSpaceString(data));
                    }
                }
                if (memo != null) {
                    rqMemo.foundResponse = true;
                    memo.handleResponseDatagram(data);
                }
            }
        });
    }
    
    
    NodeID here;
    DatagramService downstream;
    private Timer retryTimer;

    public MemoryConfigurationService(MemoryConfigurationService mcs) {
        this(mcs.here, mcs.downstream);
    }

    public void setTimeoutMillis(long t) {
        timeoutMillis = t;
    }

    /**
     * Waits to ensure that all pending timer tasks are complete. Used for testing.
     *
     * @throws java.lang.InterruptedException if interrupted
     */
    public void waitForTimer() throws InterruptedException {
        final Semaphore s = new Semaphore(0);
        retryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                s.release();
            }
        }, 1);
        s.acquire();
    }

    private abstract static class McsRequestMemo {
        protected final NodeID dest;
        // contains the subcommand (second byte) of the datagram for the request, having zeroed
        // out any bits on the space shortcut.
        protected final int requestCode;
        protected final FailureCallback failureCallback;
        boolean foundResponse = false;
        int numTries = 0;

        McsRequestMemo(NodeID dest, int requestCode, FailureCallback cb) {
            this.dest = dest;
            this.requestCode = requestCode;
            this.failureCallback = cb;
        }

        public int getRequestCode() {
            return requestCode;
        }

        protected NodeID getDest() { return dest; }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof McsRequestMemo)) return false;
            McsRequestMemo m = (McsRequestMemo) o;
            if (!this.dest.equals(m.dest)) return false;
            if (getRequestCode() != m.getRequestCode()) return false;
            return true;
        }

        /**
         * Creates the payload of the datagram to transmit.
         * @return the transmit datagram bytes.
         */
        protected abstract int[] renderTransmitDatagram();

    }

    /**
     * Request memos that can receive a reply must implement this method to handle the response
     * datagram.
     */
    private interface RequestWithReplyDatagram {
        /**
         * If a response datagram came back that matches this request (i.e. compareResponse
         * returns true), then this function will be called with the response datagram payload.
         * @param data response datagram payload.
         */
        void handleResponseDatagram(int[] data);

        /**
         * Returns true if the received response belongs to this request.
         * @param data datagram pyaload
         * @return true if the response is for this request
         */
        boolean compareResponse(int[] data);
    }

    /**
     * Request memos that do not have to receive a reply datagram must implement this method in
     * order to allow the datagram transmit OK to be forwarded back to the client.
     */
    private interface RequestWithNoReply {
        NoReturnCallback getNoReturnCallback();
    }

    /**
     * Common base class for all request/response memos that take a space byte and an address
     * offset, such as read, write, read stream, write stream.
     */
    private abstract static class McsAddressedRequestMemo extends McsRequestMemo implements
            RequestWithReplyDatagram {
        protected final int space;
        protected final long address;
        McsAddressedRequestMemo(NodeID dest, int requestCode, int space, long address,
                                FailureCallback cb) {
            super(dest, requestCode, cb);
            this.space = space;
            this.address = address;
        }

        /**
         * Checks whether there is a desired
         * @return 1 if there should be a separate space byte, 0 otherwise.
         */
        protected int getSpaceOffset() {
            return space >= 0xFD ? 0 : 1;
        }

        /**
         * Computes the offset where the payload is in the datagram.
         * @return 7 if there is a separate space byte, 6 if the space is encoded in the low bits.
         */
        protected int getPayloadOffset() {
            return 6 + getSpaceOffset();
        }

        /**
         * Computes the offset where the payload is in the datagram.
         * @param data    the datagram contents
         * @return 7 if there is a separate space byte, 6 if the space is encoded in the low bits.
         */
        protected int getPayloadOffset(int[] data) {
            return 6 + ((data[1] & 0x3) != 0 ? 0 : 1);
        }

        protected void fillRequest(int[] data) {
            data[0] = DATAGRAM_TYPE;
            data[1] = getRequestCode();
            if (space >= 0xFD) {
                data[1] |= space & 3;
            } else {
                data[6] = space & 0xff;
            }
            DatagramUtils.renderLong(data, 2, address);
        }

        /**
         * @return how many bytes to allocate after the (optional) space byte.
         */
        protected abstract int getPayloadLength();

        /**
         * Fills in the payload bytes of the transmit datagram.
         * @param data already allocated array with everything until the payload (space,
         *             address, request code, datagram code) being filled in.
         */
        protected abstract void fillPayload(int[] data);

        @Override
        protected int[] renderTransmitDatagram() {
            int[] data = new int[getPayloadOffset() + getPayloadLength()];
            fillRequest(data);
            fillPayload(data);
            return data;
        }

        @Override
        public boolean compareResponse(int[] data) {
            if (data.length < (6 + getSpaceOffset())) return false;
            if (address != DatagramUtils.parseLong(data, 2)) return false;
            if (space != getSpaceFromPayload(data)) return false;
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (!super.equals(o)) return false;
            if (!(o instanceof McsAddressedRequestMemo)) return false;
            McsAddressedRequestMemo m = (McsAddressedRequestMemo) o;
            if (this.space != m.space) return false;
            if (this.address != m.address) return false;
            return true;
        }

        @Override
        public int hashCode() { return getRequestCode()+dest.hashCode()+((int)address)+space; }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + ": dest " + dest.toString() + " space 0x" +
                    Integer.toHexString(space) + " address 0x" + Long.toHexString(address);
        }

        @Override
        public void handleResponseDatagram(int[] data) {
            if ((data[1] & SUBCMD_ERROR) != 0) {
                failureCallback.handleFailure(DatagramUtils.parseErrorCode(data, getPayloadOffset(data)));
                return;
            }
            handleSuccessResponse(data);
        }

        /**
         * Called only if the response does not have the failure bit and all parameters are
         * matching.
         * @param data payload of response datagram.
         */
        protected abstract void handleSuccessResponse(int[] data);
    }

    public static int getSpaceFromPayload(int[] data) {
        if ((data[1] & 0x3) != 0) { return 0xFC + (data[1] & 0x3); }
        return data[6];
    }

    /**
     * Computes what the request type would be that caused this response command to arrive.
     * @param subCmd data[1] of an incoming response.
     * @return data[1] (minus the space)
     */
    public static int getRequestTypeFromResponseType(int subCmd) {
        if (subCmd < 0x80) {
            return subCmd & 0xE0;
        } else {
            return subCmd & 0xFC;
        }
    }

    // Holds the memo pointers to all pending operations: datagrams that were sent out and are
    // waiting a response. Must be synchronized(this) for all accesses.
    final Map<Integer, McsRequestMemo> pendingRequests = new HashMap<>(5);
    final Map<Integer, ArrayDeque<McsRequestMemo>> queuedRequests = new HashMap<>(5);


    /**
     * Tests if the given memo is in the top memo in pendingRequests for its request type. If so,
     * pops it.
     * @param memo the memo to test.
     */
    private void checkAndPopMemo(McsRequestMemo memo) {
        int rqCode = memo.getRequestCode();
        synchronized(this) {
            if (pendingRequests.get(rqCode) == memo) {
                pendingRequests.remove(memo.getRequestCode());
                memo = null;
                if (queuedRequests.containsKey(rqCode)) {
                    Queue<McsRequestMemo> l = queuedRequests.get(rqCode);
                    if (!l.isEmpty()) {
                        memo = l.poll();
                        pendingRequests.put(rqCode, memo);
                    }
                }
            } else {
                logger.warning("Error checking the pending request memo for code " + rqCode + " " +
                        "expected " + memo.toString() + " actual " +
                        pendingRequests.get(memo.getRequestCode()));
                memo = null;
            }
        }
        if (memo != null) {
            sendRequest(memo);
        }
    }

    /**
     *
     * @param memo a request memo
     * @return true if this request memo is already sent, but no response or failure has been
     * received, i.e. this memo is blocking the pending queue.
     */
    private boolean isBlockingPendingQueue(McsRequestMemo memo) {
        synchronized (this) {
            return (pendingRequests.get(memo.getRequestCode()) == memo);
        }
    }

    /**
     * Starts a timer and re-tries a request if the timer expired without seeing a response.
     * @param memo request memo with expected response.
     */
    private void delayRetryMemo(final McsRequestMemo memo) {
        if (memo.numTries >= MAX_TRIES) {
            // TODO: add proper error code.
            checkAndPopMemo(memo);
            memo.failureCallback.handleFailure(0x1000);
        }
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                if (memo.foundResponse) return;
                if (!isBlockingPendingQueue(memo)) return;
                sendRequest(memo);
            }
        };
        retryTimer.schedule(tt, timeoutMillis);
    }

    private void sendRequest(final McsRequestMemo memo) {
        ++memo.numTries;
        downstream.sendData(new DatagramService.DatagramServiceTransmitMemo(memo.getDest(), memo.renderTransmitDatagram()) {
            @Override
            public void handleSuccess(int flags) {
                if (memo instanceof RequestWithNoReply &&
                        ((flags & DatagramService.FLAG_REPLY_PENDING) == 0)) {
                    checkAndPopMemo(memo);
                    ((RequestWithNoReply) memo).getNoReturnCallback().handleSuccess();
                    return;
                }
                if (memo instanceof RequestWithReplyDatagram &&
                        ((flags & DatagramService.FLAG_REPLY_PENDING) != 0)) {
                    // Leave the memo in the pending, wait for reply datagram.
                    return;
                }
                // Now: something is fishy.
                if (memo instanceof RequestWithReplyDatagram) {
                    logger.info("Expected reply pending, got zero.");
                    // We will still wait for a reply datagram.
                    return;
                }
                logger.warning("The remote node wants to send a reply but we don't know how to " +
                        "handle it. memo: " + memo.toString());
                checkAndPopMemo(memo);
                ((RequestWithNoReply) memo).getNoReturnCallback().handleSuccess();
            }

            @Override
            public void handleFailure(int errorCode) {
                checkAndPopMemo(memo);
                memo.failureCallback.handleFailure(errorCode);
            }
        });
    }

    public void request(McsRequestMemo memo) {
        synchronized(this) {
            int rqCode = memo.getRequestCode();
            if (pendingRequests.containsKey(rqCode)) {
                if (!queuedRequests.containsKey(rqCode)) {
                    queuedRequests.put(rqCode, new ArrayDeque<>());
                }
                queuedRequests.get(rqCode).add(memo);
                return;
            } else {
                pendingRequests.put(memo.getRequestCode(), memo);
            }
        }
        sendRequest(memo);
    }

    static class McsWriteMemo extends McsAddressedRequestMemo implements
            RequestWithNoReply {
        public McsWriteMemo(NodeID dest, int space, long address, byte[] data, NoReturnCallback
                cb) {
            super(dest, SUBCMD_WRITE, space, address, cb);
            this.data = data;
            this.callback = cb;
        }

        final byte[] data;
        final NoReturnCallback callback;

        @Override
        public boolean equals(Object o) {
            if (!super.equals(o)) return false;
            if (! (o instanceof McsWriteMemo)) return false;
            McsWriteMemo m = (McsWriteMemo) o;
            if (this.data.length != m.data.length) return false;
            for (int i = 0; i < this.data.length; i++)
                if (this.data[i] != m.data[i]) return false;
            return true;
        }

        @Override
        protected int getPayloadLength() {
            return data.length;
        }

        @Override
        protected void fillPayload(int[] data) {
            for (int i = 0; i < this.data.length; ++i) {
                data[getPayloadOffset() + i] = DatagramUtils.byteToInt(this.data[i]);
            }
        }

        @Override
        protected void handleSuccessResponse(int[] data) {
            callback.handleSuccess();
        }

        @Override
        public NoReturnCallback getNoReturnCallback() {
            return callback;
        }
    }

    public interface McsWriteHandler extends NoReturnCallback {}

    public void requestWrite(NodeID dest, int space, long address, byte[] data, McsWriteHandler
            cb) {
        request(new McsWriteMemo(dest, space, address, data, cb));
    }

    public interface McsReadHandler extends FailureCallback {
        /**
         * This function will be called upon successful read.
         * @param dest the node ID from which the read happened
         * @param space the space number from which the read happened
         * @param address address within the space at where the read happened
         * @param data the returned payload. not null, should be at least one byte long (or else
         *             a handleFailure call will be invoked).
         */
        void handleReadData(NodeID dest, int space, long address, byte[] data);
    }

    static class McsReadMemo extends McsAddressedRequestMemo implements
            RequestWithReplyDatagram {
        public McsReadMemo(NodeID dest, int space, long address, int len, McsReadHandler
                cb) {
            super(dest, SUBCMD_READ, space, address, cb);
            this.len = len;
            this.callback = cb;
        }

        final int len;
        final McsReadHandler callback;

        @Override
        public boolean equals(Object o) {
            if (!super.equals(o)) return false;
            if (! (o instanceof McsReadMemo)) return false;
            McsReadMemo m = (McsReadMemo) o;
            if (this.len != m.len) return false;
            return true;
        }

        @Override
        protected int getPayloadLength() {
            return 1;
        }

        @Override
        protected void fillPayload(int[] data) {
            data[getPayloadOffset()] = len;
        }

        @Override
        protected void handleSuccessResponse(int[] data) {
            int payofs = getPayloadOffset(data);
            byte[] response  = new byte[data.length - payofs];
            DatagramUtils.intToByteArray(response, 0, data, payofs, response.length);
            callback.handleReadData(dest, space, address, response);
        }
    }

    public void requestRead(NodeID dest, int space, long address, int len, McsReadHandler
            cb) {
        request(new McsReadMemo(dest, space, address, len, cb));
    }


/*    McsReadMemo readMemo;
    Stack<McsReadMemo> pendingReads = new Stack<>();
    public synchronized void request(McsReadMemo memo) {
        // forward as read Datagram
        if (readMemo != null) {
            pendingReads.add(memo);
            return;
        }
        readMemo = memo;
        sendRead();
    }

    private void sendRead() {
        ReadDatagramMemo dg = new ReadDatagramMemo(readMemo.dest, readMemo.space, readMemo.address, readMemo.count, readMemo);
        downstream.sendData(dg);
    }*/
    
    // dph
    McsWriteStreamMemo writeStreamMemo;
    public void request(McsWriteStreamMemo memo) {
        // forward as write Datagram
                                      //System.out.println("writeStreamMemo: "+memo.dest+","+memo.space+","+memo.address);
                                      // System.out.println("writeStreamMemo: "+memo.dest);
        writeStreamMemo = memo;
        WriteStreamMemo dg = new WriteStreamMemo(memo.dest, memo.space, memo.address, memo.srcStreamId,
                memo);
        downstream.sendData(dg);
    }

    McsConfigMemo configMemo;
    public void request(McsConfigMemo memo) {
        // forward as read Datagram
        configMemo = memo;
        ConfigDatagramMemo dg = new ConfigDatagramMemo(memo.dest, memo);
        downstream.sendData(dg);
    }
    
    McsAddrSpaceMemo addrSpaceMemo;
    public void request(McsAddrSpaceMemo memo) {
        // forward as read Datagram
        addrSpaceMemo = memo;
        AddrSpaceDatagramMemo dg = new AddrSpaceDatagramMemo(memo.dest, memo);
        downstream.sendData(dg);
    }
    
    @Immutable
    @ThreadSafe
    static public abstract class McsWriteStreamMemo {
        public McsWriteStreamMemo(NodeID dest, int space, long address, int srcStreamId) {
            this.address = address;
            this.space = space;
            this.dest = dest;
            this.srcStreamId = srcStreamId;
        }
        
        //final int count;
        final long address;
        final int space;
        final NodeID dest;
        final int srcStreamId;

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (! (o instanceof McsWriteStreamMemo)) return false;
            McsWriteStreamMemo m = (McsWriteStreamMemo) o;
            if (this.dest != m.dest) return false;
            if (this.space != m.space) return false;
            if (this.address != m.address) return false;
            return this.srcStreamId == m.srcStreamId;
        }
        
        @Override
        public String toString() {
            return "McsWriteStreamMemo: "+address;
        }
        
        @Override
        public int hashCode() { return dest.hashCode()+space+((int)address); }

        /**
         * Called if the write stream command is accepted by the destination node.
         */
        public abstract void handleSuccess();
        /**
         * Called when the write stream command is rejected by the destination node. Temporary
         * errors are internally retried.
         * @param where describes which part of the protocol caused the failure
         * @param errorCode non-zero for error reply
         */
        public abstract void handleFailure(String where, int errorCode);
    }

    @Immutable
    @ThreadSafe
    public class WriteStreamMemo extends DatagramService.DatagramServiceTransmitMemo  {
      public WriteStreamMemo(NodeID dest, int space, long address, int
              srcStreamId, McsWriteStreamMemo memo) {
          super(dest);
          boolean spaceByte = false;
          if (space<0xFD) spaceByte = true;
          this.data = new int[6+(spaceByte ? 1 : 0)+1];
          this.data[0] = DATAGRAM_TYPE;
          this.data[1] = 0x20;
          if (space >= 0xFD) this.data[1] |= space&0x3;
          
          this.data[2] = (int)(address>>24)&0xFF;
          this.data[3] = (int)(address>>16)&0xFF;
          this.data[4] = (int)(address>>8 )&0xFF;
          this.data[5] = (int)(address    )&0xFF;

          int ofs = 6;
          if (spaceByte) {
              this.data[ofs++] = space;
          }
          if (srcStreamId < 1 || srcStreamId > 254) {
              throw new IllegalArgumentException("Invalid source stream ID: " + srcStreamId);
          }
          this.data[ofs++] = srcStreamId;
          this.memo = memo;
        }
        McsWriteStreamMemo memo;

        @Override
        public void handleSuccess(int flags) {
            if (0 != (flags & DatagramService.FLAG_REPLY_PENDING)) {
                return;
            }
            writeStreamMemo = null;
            memo.handleSuccess();
        }

        @Override
        public void handleFailure(int errorCode) {
            writeStreamMemo = null;
            memo.handleFailure("TxDatagram", errorCode);
        }
    }
    
    
    @Immutable
    @ThreadSafe    
    static public abstract class McsConfigMemo {
        public McsConfigMemo(NodeID dest) {
            this.dest = dest;
        }

        final NodeID dest;
        
        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (! (o instanceof McsConfigMemo)) return false;
            McsConfigMemo m = (McsConfigMemo) o;
            return this.dest == m.dest;
        } 
    
        @Override
        public String toString() {
            return "McsConfigMemo";
        }
        
        @Override
        public int hashCode() { return dest.hashCode(); }
        
        /**
         * Overload this for notification of failure reply
         * @param code non-zero for error reply
         */
        public abstract void handleFailure(int code);
        
        /**
         * Overload this for notification of data.
         * @param dest         remote node ID that sent us this reply
         * @param commands     supported commands (see standard)
         * @param options      supported options  (see stadnard)
         * @param highSpace    largest supported address space number (not counting 0xFD..0xFF)
         * @param lowSpace     smallest supported address space number (not counting 0xFD..0xFF)
         * @param name         ?? any additional response bytes the node wanted to send to us
         */
        public void handleConfigData(NodeID dest, int commands, int options, int highSpace, int lowSpace, String name) { 
        }

    }

    @Immutable
    @ThreadSafe    
    public class ConfigDatagramMemo extends DatagramService.DatagramServiceTransmitMemo {
        ConfigDatagramMemo(NodeID dest, McsConfigMemo memo) {
            super(dest);
            this.data = new int[2];
            this.data[0] = DATAGRAM_TYPE;
            this.data[1] = 0x80;                
            this.memo = memo;
        }
        McsConfigMemo memo;

        @Override
        public void handleSuccess(int flags) {
            if ((flags & DatagramService.FLAG_REPLY_PENDING) == 0) {
                logger.warning("MemConfig GetConfig datagram returned with no reply_pending bit " +
                        "set.");
            }
            // Wait for answer.
        }

        @Override
        public void handleFailure(int errorCode) {
            checkAndPopConfigMemo(memo);
            memo.handleFailure(errorCode);
        }
    }

    private synchronized void checkAndPopConfigMemo(McsConfigMemo memo) {
        if (memo != this.configMemo) {
            logger.warning("Error checking the configMemo. Expected=" + memo + " actual="+configMemo);
            return;
        }
        configMemo = null;
    }

    private synchronized void checkAndPopAddrspaceMemo(McsAddrSpaceMemo memo) {
        if (memo != this.addrSpaceMemo) {
            logger.warning("Error checking the addrspaceMemo. Expected=" + memo + " " +
                    "actual="+addrSpaceMemo);
            return;
        }
        addrSpaceMemo = null;
    }

    /**
     *
     */
    @Immutable
    @ThreadSafe    
    static public class McsAddrSpaceMemo {
        public McsAddrSpaceMemo(NodeID dest, int space) {
            this.dest = dest;
            this.space = space;
        }

        NodeID dest;
        int space;
        
        public boolean equals(Object o) {
            if (o == null) return false;
            if (! (o instanceof McsAddrSpaceMemo)) return false;
            McsAddrSpaceMemo m = (McsAddrSpaceMemo) o;
            if (this.space != m.space) return false;
            return this.dest == m.dest;
        } 
    
        public String toString() {
            return "McsAddrSpaceMemo "+space;
        }
        
        public int hashCode() { return dest.hashCode()+space; }
        
        /**
         * Overload this for notification of failure reply
         * @param code non-zero for error reply
         */
        public void handleWriteReply(int code) { 
        }
        
        /**
         * Overload this for notification of data.
         * @param dest          node that sent this reply
         * @param space         address space we queried
         * @param hiAddress     largest valid address in this address space
         * @param lowAddress    smallest valid address in this address space
         * @param flags         address space flags (e.g. R/O, see standard)
         * @param desc          string description for this address space
         */
        public void handleAddrSpaceData(NodeID dest, int space, long hiAddress, long lowAddress, int flags, String desc) { 
        }

    }

    @Immutable
    @ThreadSafe    
    static public class AddrSpaceDatagramMemo extends DatagramService.DatagramServiceTransmitMemo {
        AddrSpaceDatagramMemo(NodeID dest, McsAddrSpaceMemo memo) {
            super(dest);
            this.data = new int[3];
            this.data[0] = DATAGRAM_TYPE;
            this.data[1] = 0x84;                
            this.data[2] = memo.space;                
            this.memo = memo;
        }
        McsAddrSpaceMemo memo;

        @Override
        public void handleSuccess(int flags) {

        }

        @Override
        public void handleFailure(int errorCode) {

        }

        public void handleReply(int code) {
            memo.handleWriteReply(code);
        }
        

    }


    public void dispose(){
       retryTimer.cancel();
    }
}
