package org.openlcb.implementations;

import org.openlcb.*;
import org.openlcb.implementations.throttle.Float16;

import java.util.*;
import net.jcip.annotations.*;

/**
 * A set of utility functions and classes for 
 * working with Location Services messages
 *
 * @author Bob Jacobsen   Copyright (C) 2025
 */
 
class LocationServiceUtils {

    static public Content parse(Message inputMessage) {
    
        // first, check the message type
        if (! (inputMessage instanceof ProducerConsumerEventReportMessage)) return null;
        
        ProducerConsumerEventReportMessage msg = (ProducerConsumerEventReportMessage) inputMessage;
        byte[] payload = msg.getPayloadArray();
        
        // check for minimal message length
        if (payload.length < 8) return null;
        
        // check for right event ID
        byte[] eid = msg.getEventID().getContents();
        if (eid[0] != 0x01 || eid[1] != 0x02) return null;
        
        // This is Location Services EWP, process it
        
        // process first section
        int overallFlags = payload[0]<<8+payload[1];
        NodeID scannerReporting = new NodeID(new byte[]{
                    eid[2], eid[3], eid[4], eid[5], eid[6], eid[7]
                        });
        NodeID scannedDevice = new NodeID(new byte[]{
                    payload[2], payload[3], payload[4], payload[5], payload[6], payload[7]
                        });
        
        List<Block> blocks = parseBlock(payload, 8, new ArrayList<Block>());
        
        Content retval = new Content(scannerReporting, scannedDevice, overallFlags, blocks);
        return retval;
    }

    static private List<Block> parseBlock(byte[] payload, int offset, ArrayList<Block> list) {
        
        if (offset >= payload.length) return list;
        
        int length = payload[offset];
        
        if (length == 0) {
            list.add(new Block(Block.Type.RESERVED, new byte[0]));
        } else {
            // here we parse the block into something useful
            Block.Type type = Block.Type.get((int) payload[offset+1]);
            byte[] content = Arrays.copyOfRange(payload, offset+1, offset+1+length);
            switch (type) {
                case ANALOG :
                    list.add(new AnalogBlock(content));
                    break;
                
                default:
                    list.add(new Block(type, content));
            }
        }
        
        return parseBlock(payload, offset+length+1, list);
    }
    
    /**
     * Accessors for the parse contents
     */
    @Immutable 
    static public class Content {
        // The nodeID of the scanner making the report
        NodeID scannerReporting;
        public NodeID getScannerReporting() { return scannerReporting; }
        
        // The nodeID of the scanned device
        NodeID scannedDevice;
        public NodeID getScannedDevice() { return scannedDevice; }
        
        // The overall flags
        int overallFlags;
        public int getOverallFlags() {return overallFlags; }
        
        // The blocks of content
        List<Block> blocks;
        public List<Block> getBlocks() { return blocks; }
        
        public Content(NodeID scannerReporting, NodeID scannedDevice, int overallFlags, List<Block> blocks) {
            this.scannerReporting = scannerReporting;
            this.scannedDevice = scannedDevice;
            this.overallFlags = overallFlags;
            this.blocks = blocks;
        }    
    
    }
    
    /**
     * Generic accessor for the contents of a particular Block
     */
    @Immutable
    static public class Block {
        enum Type {
            RESERVED(0, "Reserved"),
            READABLE(1, "Readable"),
            RFID(2, "RFID"),
            QR(3, "QR"),
            RAILCOM(4, "RailCom"),
            TRANSPONDING(5, "Transponding"),
            POSITION(6, "Position"),
            DCCADDRESS(7, "DccAddress"),
            SETSPEED(8, "Set Speed"),
            COMMANDEDSPEED(9, "Commanded Speed"),
            ACTUALSPEED(10, "Actual Speed"),
            ANALOG(11, "Analog");
            
            Type(int code, String name) {
                this.code = code;
                this.name = name;
                
                getMap().put(code, this);
            }

            int code;
            String name;

            public String toString() {
                return name;
            }

            public static Type get(Integer type) {
                return mapping.get(type);
            }

            private static Map<Integer, Type> mapping;
            private static Map<Integer, Type> getMap() {
                if (mapping == null)
                    mapping = new java.util.HashMap<Integer, Type>();
                return mapping;
            }
        }
        
        Block(Type type, byte[] content) {
            this.length = content.length;
            this.type = type;
            this.content = content;
         }
        
        int length;
        int getLength() { return length; }
        Type type;
        Type getType() { return type; }
        byte[] content;
        byte[] getContent() { return content; }
    }

    /**
     * Accessor for the contents of a particular Block with Analog contents
     */
    @Immutable
    static public class AnalogBlock extends Block {
        enum Unit {
            UNKNOWN(0, "Unknown"),
            VOLTS(1, "Volts"),
            AMPERES(2, "Amperes"),
            WATTS(3, "Watts"),
            OHMS(4, "OHMS"),
            DEGREESC(5, "Degrees C"),
            SECONDS(6, "Seconds"),
            METERS(7, "Meters"),
            METERS2(8, "Meters^2"), 
            METERS3(9, "Meters^3"),
            METERSPERSECOND(10, "Meters/Second"),
            METERSPERSECOND2(11, "Meters/Second^2"),
            KILOGRAMS(12, "Kilograms"),
            NEWTONS(13, "Newtons");
            
            Unit(int code, String name) {
                this.code = code;
                this.name = name;
                
                getMap().put(code, this);
            }
            
            int code;
            String name;

            public String toString() {
                return name;
            }

            public static Unit get(Integer unit) {
                return mapping.get(unit);
            }

            private static Map<Integer, Unit> mapping;
            private static Map<Integer, Unit> getMap() {
                if (mapping == null)
                    mapping = new java.util.HashMap<Integer, Unit>();
                return mapping;
            }

        }
        
        AnalogBlock(byte[] content) {
            super(Block.Type.ANALOG, content);
            
            // decode contents of this block
            
            // [1], [2] are the Float16 value
            value = new Float16(content[1], content[2]).getFloat();
            // [3] is the unit
            unit = Unit.get((int)content[3]);

            // [4] through end are the user string (UTF8)
            try {
                text = new String(Arrays.copyOfRange(content, 4, content.length),"UTF-8");
            } catch (java.io.UnsupportedEncodingException ex) {
                text = "<UTF8 Error>";
            }
        }
        
        Unit unit;
        Unit getUnit() { return unit; }
        double value;
        double getValue() { return value; }
        String text;
        String getText() { return text; }
    }
     
}