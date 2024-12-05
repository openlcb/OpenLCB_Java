package org.openlcb;

// For annotations
import net.jcip.annotations.*;
import edu.umd.cs.findbugs.annotations.*;

/**
 * Common EventID implementation.
 * <p>
 * EventID objects are immutable once created.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class EventID {

    static final int BYTECOUNT = 8;

    @CheckReturnValue
    public EventID(@NonNull NodeID node, int b7, int b8) {
        this.contents = new byte[BYTECOUNT];
        System.arraycopy(node.contents, 0, this.contents, 0, BYTECOUNT-2);

        this.contents[6] = (byte)b7;
        this.contents[7] = (byte)b8;
    }

    @CheckReturnValue
    public EventID(@NonNull byte[] contents) {
        if (contents == null)
            throw new java.lang.IllegalArgumentException("null argument invalid");
        if (contents.length != BYTECOUNT)
            throw new java.lang.IllegalArgumentException("Wrong EventID length: "+contents.length);
        this.contents = new byte[BYTECOUNT];
        System.arraycopy(contents, 0, this.contents, 0, BYTECOUNT);
    }

    @CheckReturnValue
    public EventID(@NonNull String value) {
        if (value == null)
            throw new java.lang.IllegalArgumentException("null argument invalid");
        byte[] data = org.openlcb.Utilities.bytesFromHexString(value);
        if (data.length != BYTECOUNT)
            throw new java.lang.IllegalArgumentException("Wrong EventID length: "+data.length);
        this.contents = new byte[BYTECOUNT];
        System.arraycopy(data, 0, this.contents, 0, BYTECOUNT);
    }

    // note long's 64th bit is a sign
    @CheckReturnValue
    public EventID(long value) {
        this.contents = new byte[BYTECOUNT];
        for (int index = 0; index < 8; index++) {
            contents[index] = (byte)((value>>(8*(7-index))) & 0xFF);
        }
    }

    byte[] contents;

    @CheckReturnValue
    @NonNull
    public byte[] getContents() {
        // copy to ensure immutable
        byte[] retval = new byte[BYTECOUNT];
        System.arraycopy(contents, 0, retval, 0, BYTECOUNT);
        return retval;
    }

    @CheckReturnValue
    @Override
    public boolean equals(Object o){
        // try to cast, else not equal
        try {
            EventID other = (EventID) o;
            for (int i = 0; i<BYTECOUNT; i++)
                if (other.contents[i] != this.contents[i]) return false;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /// Checks whether a given Event ID comes from a given Node ID's space.
    public boolean startsWith(NodeID id) {
        for (int i = 0; i < 6; ++i) {
            if (contents[i] != id.contents[i]) return false;
        }
        return true;
    }

    @CheckReturnValue
    @Override
    public int hashCode() {
        return (contents[0]<<21)
            +(contents[1]<<18)
            +(contents[2]<<15)
            +(contents[3]<<12)
            +(contents[4]<<9)
            +(contents[5]<<6)
            +(contents[6]<<3)
            +(contents[7]);
    }

    @CheckReturnValue
    @NonNull
    @Override
    public String toString() {
        return "EventID:"
                +Utilities.toHexDotsString(contents);
    }

    @CheckReturnValue
    @NonNull
    public String toShortString() {
        return Utilities.toHexDotsString(contents);
    }

    public long toLong() {
        long ret = 0;
        for (int i = 0; i < 8; ++i) {
            ret <<= 8;
            int e = contents[i];
            ret |= (e & 0xff);
        }
        return ret;
    }

    /**
     * Take the eventID from a range, and return
     * the lower flag bytes as a hex dotted string.
     */
    public long rangeSuffix() {
        // find the mask value
        long eid = this.toLong();
        long sampleBit = eid & 0x01;
        long mask = 0L;
        while ( (eid &0x01L) == sampleBit) {
            mask = (mask <<1) | 0x01;
            eid = eid >> 1;
        }
        return mask;
    }


    /**
     * Decode well-known and specifically defined event IDs
     * @return "" if nothing interesting about the event    
     */    
    public String parse() {
        String eid = this.toShortString().substring(0, 2);
        switch (eid) {
            case "00":
                return reserved();
            case "01": 
                return wellKnown();
            case "09": 
                if (this.toShortString().startsWith("09.00.99.FF")) {
                    return trainSearch();
                } 
                // deliberately falling through
                //$FALL-THROUGH$
            default:
                return "";
        }
    }
    
    protected String reserved() {
        return "Reserved "+this.toShortString();
    }
    
    protected String wellKnown() {
        String eid = this.toShortString();
        switch (eid) {
            case "01.00.00.00.00.00.FF.FF":
                return "Emergency off";
            case "01.00.00.00.00.00.FF.FE":
                return "Clear Emergency Off";
            case "01.00.00.00.00.00.FF.FD":
                return "Emergency stop of all operations";
            case "01.00.00.00.00.00.FF.FC":
                return "Clear emergency stop of all operations";
            case "01.00.00.00.00.00.FF.F8":
                return "Node recorded a new log entry";
            case "01.00.00.00.00.00.FF.F1":
                return "Power supply brownout detected below minimum required by node";
            case "01.00.00.00.00.00.FF.F0":
                return "Power supply brownout detected below minimum required by standard";
            case "01.00.00.00.00.00.FE.00":
                return "Ident button combination pressed";
            case "01.00.00.00.00.00.FD.01":
                return "Link error code 1 – the specific meaning is link wire protocol specific";
            case "01.00.00.00.00.00.FD.02":
                return "Link error code 2";
            case "01.00.00.00.00.00.FD.03":
                return "Link error code 3";
            case "01.00.00.00.00.00.FD.04":
                return "Link error code 4";

            case "01.01.00.00.00.00.02.01":
                return "Duplicate Node ID Detected";
            case "01.01.00.00.00.00.03.03":
                return "This node is a Train";
            case "01.01.00.00.00.00.03.04":
                return "This node is a Train Control Proxy";
            case "01.01.00.00.00.00.06.01":
                return "Firmware Corrupted";
            case "01.01.00.00.00.00.06.02":
                return "Firmware Upgrade Request by Hardware Switch";

            default:
                // check for fastclock and DCC ranges
                if (eid.startsWith("01.01.00.00.01")) {
                    return fastClock();
                } else if (eid.startsWith("01.01.02")) {
                    return dccRange();
                } else {
                    return "Well-Known "+eid;
                }
        }
    }
    
    protected String fastClock() {
        String clockNum = this.toShortString().substring(16, 17);
        byte[] contents = this.getContents();
        int lowByte = contents[7]&0xFF;
        int highByte = contents[6]&0xFF;
        int highByteMasked = 0x7F&highByte;
        int bothBytes = highByte*256+lowByte;
        String function = "";
        
        String set = ((0x80 & highByte) == 0x80) ? "Set " : "";

        if ((highByte & 0xF0) == 0xC0) {  // set rate
            int rate = (highByte&0xF)*256+lowByte;
            function = "Set rate "+(rate/4.);
        } else if (bothBytes == 0xF000) {  //
            function = "Query";
        } else if (bothBytes == 0xF001) {  //
            function = "Stop";
        } else if (bothBytes == 0xF002) {  //
            function = "Start";
        } else if (bothBytes == 0xF003) {  //
            function = "Date Rollover";
        } else if (highByteMasked < 24) {  // time
            String lowString = "00"+Integer.toString(lowByte);
            lowString = lowString.substring(lowString.length()-2);
            function = set+"time "+highByteMasked+":"+lowString;
        } else if (highByteMasked <= 0x2C) {  // date
            String lowString = "00"+Integer.toString(lowByte);
            lowString = lowString.substring(lowString.length()-2);
            function = set+"date "+(highByteMasked-0x20)+"/"+lowString;
        } else if (highByteMasked < 0x40) {  // year
            int year = (highByteMasked*256+lowByte)-0x3000;
            function = set+"year "+year;
        } else {
            function = "reserved";
        }
        
        return "Fast Clock "+clockNum+" "+function;
    }

    protected String dccRange() {
        String eid = this.toShortString();
        if (eid.startsWith("01.01.02.00.00.FF")) {
            return "DCC Basic Acc Addr Activate "+(this.toLong()&0x7FFL);
        } else if (eid.startsWith("01.01.02.00.00.FE")) {
            return "DCC Basic Acc Addr Deactivate "+(this.toLong()&0x7FFL);
        } else if (eid.startsWith("01.01.02.00.00.FD")) {
            return "DCC Turnout Feedback On "+(this.toLong()&0x7FFL);
        } else if (eid.startsWith("01.01.02.00.00.FC")) {
            return "DCC Turnout Feedback Off "+(this.toLong()&0x7FFL);
        } else if (eid.startsWith("01.01.02.00.00.FB")) {
            return "DCC Sensor On "+(this.toLong()&0xFFFL);
        } else if (eid.startsWith("01.01.02.00.00.FA")) {
            return "DCC Sensor Off "+(this.toLong()&0xFFFL);
        } else if (eid.startsWith("01.01.02.00.01")) {
            return "DCC Extended Accessory "
                +((this.toLong()>>8)&0x7FFL)
                +" "+(this.toLong()&0xFF);
        } else {
            return "DCC Well-Known "+this;
        }
    }
    
    protected String trainSearch() {
        return "Train Search";
    }
   
}
