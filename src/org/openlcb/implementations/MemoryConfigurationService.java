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
            public int handleData(int[] data) { 
                System.out.println("Received datagram "+data);
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

    public void request(McsReadMemo memo) {
        // forward as read Datagram
        ReadDatagramMemo dg = new ReadDatagramMemo(memo.dest, memo.space, memo.address, memo.count, memo);
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
         * @param non-zero for error reply
         */
        public void handleWriteReply(int code) { 
        }
        
        /**
         * Overload this for notification of data.
         */
        public void handleReadData(int[] data) { 
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
    
}
