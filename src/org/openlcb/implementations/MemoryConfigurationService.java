package org.openlcb.implementations;

import org.openlcb.*;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Service for reading and writing via the Memory Configuration protocol
 * 
 * Meant to shield the using code from all the details of that
 * process via read and write primitives.
 *
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision: -1 $
 */
public class MemoryConfigurationService {

    private static final int DATAGRAM_TYPE = 0x20;
    /**
     * @param downstream Connection in the direction of the layout
     */
    public MemoryConfigurationService(NodeID here, DatagramService downstream) {
        this.here = here;
        this.downstream = downstream;   
        
        // connect to be notified of config service
        downstream.registerForReceive(new DatagramService.DatagramServiceReceiveMemo(DATAGRAM_TYPE){
            // does not allow for overlapping operations, either to a single node nor or multiple types
            // nor to multiple nodes
            //
            // doesn't check for match of reply to memo, but eventually should.
            public int handleData(NodeID dest, int[] data) { 
                if (readMemo != null) {
                    byte[] content = new byte[data.length-6];
                    for (int i = 0; i<content.length; i++) content[i] = (byte)data[i+6];
                    McsReadMemo memo = readMemo;
                    readMemo = null;
                    memo.handleReadData(dest, memo.space, memo.address, content);
                }    
                if (configMemo != null) {
                    // doesn't handle decode of name string, but should
                    int commands = (data[2]<<8)+data[3];
                    int options = data[4];
                    int highSpace = data[5];
                    int lowSpace = data[6];
                    McsConfigMemo memo = configMemo;
                    configMemo = null;
                    memo.handleConfigData(dest, commands, options, highSpace, lowSpace,"");
                }    
                if (addrSpaceMemo != null) {
                    // doesn't handle decode of desc string, but should
                    int space = data[2];
                    long highAddress = (data[3]<<24)+(data[4]<<16)+(data[5]<<8)+data[6];
                    int flags = data[7];
                    long lowAddress = 0;  // doesn't handle optional value
                    
                    McsAddrSpaceMemo memo = addrSpaceMemo;
                    addrSpaceMemo = null;
                    memo.handleConfigData(dest, space, highAddress, lowAddress, flags, "");
                }    
                return 0;
            }
        });
    }
    
    NodeID here;
    DatagramService downstream;
    
    
    public void request(McsWriteMemo memo) {
        // forward as write Datagram
        WriteDatagramMemo dg = new WriteDatagramMemo(memo.dest, memo.space, memo.address, memo.data, memo);
        downstream.sendData(dg);
    }

    McsReadMemo readMemo;
    public void request(McsReadMemo memo) {
        // forward as read Datagram
        readMemo = memo;
        ReadDatagramMemo dg = new ReadDatagramMemo(memo.dest, memo.space, memo.address, memo.count, memo);
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
    static public class McsReadMemo {
        public McsReadMemo(NodeID dest, int space, long address, int count) {
            this.count = count;
            this.address = address;
            this.space = space;
            this.dest = dest;
        }

        int count;
        long address;
        int space;
        NodeID dest;
        
        public boolean equals(Object o) {
            if (o == null) return false;
            if (! (o instanceof McsReadMemo)) return false;
            McsReadMemo m = (McsReadMemo) o;
            if (this.dest != m.dest) return false;
            if (this.space != m.space) return false;
            if (this.address != m.address) return false;
            return this.count == m.count;
        } 
    
        public String toString() {
            return "McsReadMemo: "+address;
        }
        
        public int hashCode() { return dest.hashCode()+space+((int)address)+count; }
        
        /**
         * Overload this for notification of failure reply
         * @param code non-zero for error reply
         */
        public void handleWriteReply(int code) { 
        }
        
        /**
         * Overload this for notification of data.
         */
        public void handleReadData(NodeID dest, int space, long address, byte[] data) { 
        }

    }

    @Immutable
    @ThreadSafe    
    static public class ReadDatagramMemo extends DatagramService.DatagramServiceTransmitMemo {
        ReadDatagramMemo(NodeID dest, int space, long address, int count, McsReadMemo memo) {
            super(dest);
            boolean spaceByte = false;
            if (space<0xFD) spaceByte = true;
            this.data = new int[6+(spaceByte ? 1 : 0)+1];
            this.data[0] = DATAGRAM_TYPE;
            this.data[1] = 0x40;
            if (space >= 0xFD) this.data[1] |= space&0x3;
            
            this.data[2] = (int)(address>>24)&0xFF;
            this.data[3] = (int)(address>>16)&0xFF;
            this.data[4] = (int)(address>>8 )&0xFF;
            this.data[5] = (int)(address    )&0xFF;

            if (spaceByte) this.data[6] = space;
            
            this.data[6+(spaceByte ? 1 : 0)] = count;
                
            this.memo = memo;
        }
        McsReadMemo memo;
        public void handleReply(int code) { 
            memo.handleWriteReply(code);
        }
    }

    @Immutable
    @ThreadSafe    
    static public class McsWriteMemo {
        public McsWriteMemo(NodeID dest, int space, long address, byte[] data) {
            this.data = data;
            this.address = address;
            this.space = space;
            this.dest = dest;
        }

        byte[] data;
        long address;
        int space;
        NodeID dest;
        
        public boolean equals(Object o) {
            if (o == null) return false;
            if (! (o instanceof McsWriteMemo)) return false;
            McsWriteMemo m = (McsWriteMemo) o;
            if (this.dest != m.dest) return false;
            if (this.space != m.space) return false;
            if (this.address != m.address) return false;
            if (this.data.length != m.data.length) return false;
            for (int i = 0; i < this.data.length; i++)
                if (this.data[i] != m.data[i]) return false;
            return true;
        } 
    
        public String toString() {
            return "McsWriteMemo: "+address;
        }
        
        public int hashCode() { return this.data.length+this.data[0]+dest.hashCode()+((int)address)+space; }
        
        /**
         * Overload this for notification of response
         * @param code 0 for OK, non-zero for error reply
         */
        public void handleWriteReply(int code) { 
        }

    }
    
    @Immutable
    @ThreadSafe    
    static public class WriteDatagramMemo extends DatagramService.DatagramServiceTransmitMemo {
        WriteDatagramMemo(NodeID dest, int space, long address, byte[] content, McsWriteMemo memo) {
            super(dest);
            boolean spaceByte = false;
            if (space<0xFD) spaceByte = true;
            this.data = new int[6+(spaceByte ? 1 : 0)+content.length];
            this.data[0] = DATAGRAM_TYPE;
            this.data[1] = 0x00;
            if (space >= 0xFD) this.data[1] |= space&0x3;
            
            this.data[2] = (int)(address>>24)&0xFF;
            this.data[3] = (int)(address>>16)&0xFF;
            this.data[4] = (int)(address>>8 )&0xFF;
            this.data[5] = (int)(address    )&0xFF;

            if (spaceByte) this.data[6] = space;
            
            for (int i = 0; i < content.length; i++) 
                this.data[6+(spaceByte ? 1 : 0)+i] = content[i];
                
            this.memo = memo;
        }
        McsWriteMemo memo;
        public void handleReply(int code) { 
            memo.handleWriteReply(code);
        }
        

    }
    
    @Immutable
    @ThreadSafe    
    static public class McsConfigMemo {
        public McsConfigMemo(NodeID dest) {
            this.dest = dest;
        }

        NodeID dest;
        
        public boolean equals(Object o) {
            if (o == null) return false;
            if (! (o instanceof McsConfigMemo)) return false;
            McsConfigMemo m = (McsConfigMemo) o;
            return this.dest == m.dest;
        } 
    
        public String toString() {
            return "McsConfigMemo";
        }
        
        public int hashCode() { return dest.hashCode(); }
        
        /**
         * Overload this for notification of failure reply
         * @param code non-zero for error reply
         */
        public void handleWriteReply(int code) { 
        }
        
        /**
         * Overload this for notification of data.
         */
        public void handleConfigData(NodeID dest, int commands, int options, int highSpace, int lowSpace, String name) { 
        }

    }

    @Immutable
    @ThreadSafe    
    static public class ConfigDatagramMemo extends DatagramService.DatagramServiceTransmitMemo {
        ConfigDatagramMemo(NodeID dest, McsConfigMemo memo) {
            super(dest);
            this.data = new int[2];
            this.data[0] = DATAGRAM_TYPE;
            this.data[1] = 0x80;                
            this.memo = memo;
        }
        McsConfigMemo memo;
        public void handleReply(int code) { 
            memo.handleWriteReply(code);
        }
        

    }

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
         */
        public void handleConfigData(NodeID dest, int space, long hiAddress, long lowAddress, int flags, String desc) { 
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
        public void handleReply(int code) { 
            memo.handleWriteReply(code);
        }
        

    }
}
