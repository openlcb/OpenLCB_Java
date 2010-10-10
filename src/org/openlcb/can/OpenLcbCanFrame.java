package org.openlcb.can;

import org.openlcb.*;

/**
 * Carry and work with a CAN frame in OpenLCB format.
 *
 * For definiton, see
 * http://openlcb.sf.net/trunk/documents/can/index.html
 *
 * Bit 0 of the frame is in the 0x10000000 position.
 *
 * @author  Bob Jacobsen   Copyright 2009, 2010
 * @version $Revision$
 */


public class OpenLcbCanFrame implements CanFrame, OpenLcbCan {

    public OpenLcbCanFrame(int nodeAlias) {
        this.nodeAlias = nodeAlias;
        init(nodeAlias);
    }
    
    // Data is stored in completed form as
    // a header and data content; accessors go
    // back and forth to individual fields.
    int id;  // 29 bit header
    int length;
    byte[] data = new byte[8];
    
    int nodeAlias;

    public int getHeader() { return id; }
    
    public boolean isExtended() { return true; }
    
    public boolean isRtr() { return false; }
    
    public int getNumDataElements() { return length; }
    public int getElement(int n) { return data[n]; }

    // bit 1
    static final int MASK_FRAME_TYPE = 0x08000000;
    
    // bit 17-28
    static final int MASK_SRC_ALIAS = 0x00000FFF;
    
    // bit 2-16
    static final int MASK_VARIABLE_FIELD = 0x07FFF000;
    static final int SHIFT_VARIABLE_FIELD = 12;
    
    // bit 2-4, at the top of the variable field
    static final int MASK_OPENLCB_FORMAT = 0x07000;
    static final int SHIFT_OPENLCB_FORMAT = 12;

    void init(int alias) {
        // all bits in header default to 1 except MASK_SRC_ALIAS
        id = 0x1FFFF000 | (alias & MASK_SRC_ALIAS);
    }

  // start of basic message structure

  void setSourceAlias(int a) {
    id &= ~MASK_SRC_ALIAS;
    id = id | (a & MASK_SRC_ALIAS);
  }
  
  int getSourceAlias() {
      return (int)(id & MASK_SRC_ALIAS);
  }

  void setFrameTypeCAN() {
    id &= ~MASK_FRAME_TYPE;     
  }
  
  boolean isFrameTypeCAN() {
    return (id & MASK_FRAME_TYPE) == 0x00000000L;
  }

  void setFrameTypeOpenLcb() {
    id |= MASK_FRAME_TYPE;     
  }
  
  boolean isFrameTypeOpenLcb() {
    return (id & MASK_FRAME_TYPE) == MASK_FRAME_TYPE;
  }

  void setVariableField(int f) {
    id &= ~MASK_VARIABLE_FIELD;
    int temp = f;  // ensure 32 bit arithmetic
    id |=  ((temp << SHIFT_VARIABLE_FIELD) & MASK_VARIABLE_FIELD);
  }

  int getVariableField() {
    return (int)(id & MASK_VARIABLE_FIELD) >> SHIFT_VARIABLE_FIELD;
  }
  
  // end of basic message structure
  
  // start of CAN-level messages
 
  static final int RIM_VAR_FIELD = 0x0700;

  void setCIM(int i, int testval, int alias) {
    init(alias);
    setFrameTypeCAN();
    int var =  (( (0x7-i) & 7) << 12) | (testval & 0xFFF); 
    setVariableField(var);
    length=0;
  }

  boolean isCIM() {
    return isFrameTypeCAN() && (getVariableField()&0x7000) >= 0x4000;
  }

  void setRIM(int alias) {
    init(alias);
    setFrameTypeCAN();
    setVariableField(RIM_VAR_FIELD);
    length=0;
  }

  boolean isRIM() {
      return isFrameTypeCAN() && getVariableField() == RIM_VAR_FIELD;
  }


  // end of CAN-level messages
  
  // start of OpenLCB format support
  
  int getOpenLcbFormat() {
      return (getVariableField() & MASK_OPENLCB_FORMAT) >> SHIFT_OPENLCB_FORMAT;
  }

  void setOpenLcbFormat(int i) {
      int now = getVariableField() & ~MASK_OPENLCB_FORMAT;
      setVariableField( ((i << SHIFT_OPENLCB_FORMAT) & MASK_OPENLCB_FORMAT) | now);
  }

  // is the variable field a destID?
  boolean isOpenLcDestIdFormat() {
      return ( getOpenLcbFormat() == MTI_FORMAT_ADDRESSED_NON_DATAGRAM);
  }
  
  // is the variable field a stream ID?
  boolean isOpenLcbStreamIdFormat() {
      return ( getOpenLcbFormat() == MTI_FORMAT_STREAM_CODE);
  }
  
  void setOpenLcbMTI(int fmt, int mtiHeaderByte) {
        setFrameTypeOpenLcb();
        setVariableField(mtiHeaderByte);
        setOpenLcbFormat(fmt);  // order matters here
  }
  
  boolean isOpenLcbMTI(int fmt, int mtiHeaderByte) {
      return isFrameTypeOpenLcb() 
                && ( getOpenLcbFormat() == fmt )
                && ( (getVariableField()&~MASK_OPENLCB_FORMAT) == mtiHeaderByte );
  }

  // end of OpenLCB format and decode support
  
  // start of OpenLCB messages
  
  void setPCEventReport(EventID eid) {
    init(nodeAlias);
    setOpenLcbMTI(MTI_FORMAT_SIMPLE_MTI,MTI_PC_EVENT_REPORT);
    length=8;
    loadFromEid(eid);
  }
  
  boolean isPCEventReport() {
      return isOpenLcbMTI(MTI_FORMAT_SIMPLE_MTI, MTI_PC_EVENT_REPORT);
  }

  void setLearnEvent(EventID eid) {
    init(nodeAlias);
    setOpenLcbMTI(MTI_FORMAT_SIMPLE_MTI,MTI_LEARN_EVENT);
    length=8;
    loadFromEid(eid);
  }

  boolean isLearnEvent() {
      return isOpenLcbMTI(MTI_FORMAT_SIMPLE_MTI, MTI_LEARN_EVENT);
  }

  void setInitializationComplete(int alias, NodeID nid) {
    nodeAlias = alias;
    init(nodeAlias);
    setOpenLcbMTI(MTI_FORMAT_COMPLEX_MTI,MTI_INITIALIZATION_COMPLETE);
    length=6;
    byte[] val = nid.getContents();
    data[0] = val[0];
    data[1] = val[1];
    data[2] = val[2];
    data[3] = val[3];
    data[4] = val[4];
    data[5] = val[5];
  }
  
  boolean isInitializationComplete() {
      return isOpenLcbMTI(MTI_FORMAT_COMPLEX_MTI, MTI_INITIALIZATION_COMPLETE);
  }
  
  EventID getEventID() {
    return new EventID(data);
  }
  
  NodeID getNodeID() {
    return new NodeID(data);
  }
  
  boolean isVerifyNID() {
      return isOpenLcbMTI(MTI_FORMAT_SIMPLE_MTI, MTI_VERIFY_NID);
  }

  void setVerifyNID(NodeID nid) {
    init(nodeAlias);
    setOpenLcbMTI(MTI_FORMAT_COMPLEX_MTI,MTI_VERIFY_NID);
    length=0;
  }

  void setVerifiedNID(NodeID nid) {
    init(nodeAlias);
    setOpenLcbMTI(MTI_FORMAT_COMPLEX_MTI,MTI_VERIFIED_NID);
    length=6;
    byte[] val = nid.getContents();
    data[0] = val[0];
    data[1] = val[1];
    data[2] = val[2];
    data[3] = val[3];
    data[4] = val[4];
    data[5] = val[5];
  }

  boolean isIdentifyConsumers() {
      return isOpenLcbMTI(MTI_FORMAT_SIMPLE_MTI, MTI_IDENTIFY_CONSUMERS);
  }

  void setConsumerIdentified(EventID eid) {
    init(nodeAlias);
    setOpenLcbMTI(MTI_FORMAT_COMPLEX_MTI,MTI_CONSUMER_IDENTIFIED);
    length=8;
    loadFromEid(eid);
  }

  void setConsumerIdentifyRange(EventID eid, EventID mask) {
    // does send a message, but not complete yet - RGJ 2009-06-14
    init(nodeAlias);
    setOpenLcbMTI(MTI_FORMAT_COMPLEX_MTI,MTI_IDENTIFY_CONSUMERS_RANGE);
    length=8;
    loadFromEid(eid);
  }

  boolean isIdentifyProducers() {
      return isOpenLcbMTI(MTI_FORMAT_SIMPLE_MTI, MTI_IDENTIFY_PRODUCERS);
  }

  void setProducerIdentified(EventID eid) {
    init(nodeAlias);
    setOpenLcbMTI(MTI_FORMAT_COMPLEX_MTI,MTI_PRODUCER_IDENTIFIED);
    length=8;
    loadFromEid(eid);
  }

  void setProducerIdentifyRange(EventID eid, EventID mask) {
    // does send a message, but not complete yet - RGJ 2009-06-14
    init(nodeAlias);
    setOpenLcbMTI(MTI_FORMAT_COMPLEX_MTI,MTI_IDENTIFY_PRODUCERS_RANGE);
    length=8;
    loadFromEid(eid);
  }

  boolean isIdentifyEvents() {
      return isOpenLcbMTI(MTI_FORMAT_SIMPLE_MTI, MTI_IDENTIFY_EVENTS);
  }

  void loadFromEid(EventID eid) {
    byte[] val = eid.getContents();
    data[0] = val[0];
    data[1] = val[1];
    data[2] = val[2];
    data[3] = val[3];
    data[4] = val[4];
    data[5] = val[5];
    data[6] = val[6];
    data[7] = val[7];
  }
  
  // general, but not efficient
  boolean isDatagram() {
      return isFrameTypeOpenLcb() 
                && ( (getOpenLcbFormat() == MTI_FORMAT_ADDRESSED_DATAGRAM)
                        || (getOpenLcbFormat() == MTI_FORMAT_ADDRESSED_DATAGRAM_LAST))
                && (nodeAlias == getVariableField() );
  }
  // just checks 1st, assumes datagram already checked.
  boolean isLastDatagram() {
      return (getOpenLcbFormat() == MTI_FORMAT_ADDRESSED_DATAGRAM_LAST);
  }

    /**
     * create a single datagram frame
     */
  void setDatagram(int[] content, int destAlias, boolean last) {
    init(nodeAlias);
    if (!last) {
        setOpenLcbMTI(MTI_FORMAT_ADDRESSED_DATAGRAM,destAlias);
    } else {
        setOpenLcbMTI(MTI_FORMAT_ADDRESSED_DATAGRAM_LAST,destAlias);
    }
    length=content.length;
    for (int i = 0; i< content.length; i++) {
        data[i] = (byte)(content[i]&0xFF);
    }
  }

    public boolean equals(Object other) {
        // try to cast, else not equal
        try {
            OpenLcbCanFrame c = (OpenLcbCanFrame) other;
            if (this.id != c.id) return false;
            if (this.data == null && c.data == null) return true;
            if (this.data == null && c.data != null) return false;
            if (this.data != null && c.data == null) return false;
            if (this.data.length != c.data.length) return false;
            for (int i = 0; i < this.data.length; i++) {
                if (this.data[i] != c.data[i]) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public String toString() {
        String retval = "["+id+"(d)]";
        for (int i = 0; i<length; i++) {
            retval += " "+data[i];
        }
        return retval;
    }
}
