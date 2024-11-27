// ProtocolIdentification.java

package org.openlcb;

import java.util.ArrayList;
import java.util.List;

/**
 * Protocol Identification Protocol 
 *
 * @see             "http://www.openlcb.org/trunk/specs/drafts/GenProtocolIdS.pdf"
 * @author			Bob Jacobsen   Copyright (C) 2010
 * @version			$Revision: 18542 $
 *
 */
public class ProtocolIdentification {

    public enum Protocol {
        ProtocolIdentification(  0x800000000000L,"SimpleProtocolSubset"), 
        Datagram(                0x400000000000L,"Datagram"),
        Stream(                  0x200000000000L,"Stream"), 
        Configuration(           0x100000000000L,"Configuration"),
        Reservation(             0x080000000000L,"Reservation"),
        ProducerConsumer(        0x040000000000L,"ProducerConsumer"),
        Identification(          0x020000000000L,"Identification"),
        TeachingLearningConfiguration(0x010000000000L,"TeachingLearningConfiguration"),
        RemoteButton(            0x008000000000L,"RemoteButton"),
        AbbreviatedDefaultCDI(   0x004000000000L,"AbbreviatedDefaultCDI"),
        Display(                 0x002000000000L,"Display"),
        SimpleNodeID(            0x001000000000L,"SNII"),
        ConfigurationDescription(0x000800000000L,"CDI"),
        TractionControl(         0x000400000000L,"TractionControl"),
        FunctionDescription(     0x000200000000L,"FDI"),
        DccCommandStation(       0x000100000000L,"DccCommandStation"),
        SimpleTrainNodeInfo(     0x000080000000L,"SimpleTrainNodeInfo"),
        FunctionConfiguration(   0x000040000000L,"FunctionConfiguration"),
        FirmwareUpgrade(         0x000020000000L,"FirmwareUpgrade"),
        FirmwareUpgradeActive(   0x000010000000L,"FirmwareUpgradeActive");

        Protocol(long value, String name) {
            this.value = value;
            this.name = name;
        }
        long value;
        String name;
        
        boolean supports(long r) {
            return ( (this.value & r) != 0 );
        }
        public String getName() { return name; }
        
        static List<String> decodeNames(long r) {
            ArrayList<String> retval = new ArrayList<String>();
            for (Protocol t : Protocol.values()) {
                if ( t.supports(r) ) retval.add(t.name);
            }
            return retval;
        }
        static List<Protocol> decode(long r) {
            ArrayList<Protocol> retval = new ArrayList<Protocol>();
            for (Protocol t : Protocol.values()) {
                if ( t.supports(r) ) retval.add(t);
            }
            return retval;
        }
    }
       
    long value = 0;  // multiple bits, e.g. from a node
    NodeID source;
    NodeID dest;

    public ProtocolIdentification(NodeID me, ProtocolIdentificationReplyMessage msg) {
        this.source = me;
        this.dest = msg.getSourceNodeID();
        value = msg.getValue();
    }
    public ProtocolIdentification(NodeID source, NodeID dest) {
        this.source = source;
        this.dest = dest;
        value = 0;
    }

    void start(Connection connection) {
        if (dest == null) {
            throw new AssertionError("PIP dest==null");
        }
        if (source == null) {
            throw new AssertionError("PIP src = null");
        }
        connection.put(new ProtocolIdentificationRequestMessage(source, dest), null);
    }
    
    public long getValue() {
        return value;
    }    
    public List<Protocol> getProtocols() {
        return Protocol.decode(value);
    }
    public List<String> getProtocolNames() {
        return Protocol.decodeNames(value);
    }

    /**
     * Checks if the PIP response from the constructor claims to support the given protocol.
     * @param protocol enum representing the protocol bit to test
     * @return true if protocol is supported, false otherwise.
     */
    public boolean hasProtocol(Protocol protocol) {
        return protocol.supports(value);
    }
}
