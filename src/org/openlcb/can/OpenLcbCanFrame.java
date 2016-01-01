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


public class OpenLcbCanFrame implements CanFrame {

    public OpenLcbCanFrame(int nodeAlias) {
        this.nodeAlias = nodeAlias;
        init(nodeAlias);
    }

    public OpenLcbCanFrame(CanFrame frame) {
        this.id = frame.getHeader();
        this.nodeAlias = getSourceAlias();
        setData(frame.getData());
    }

    // Data is stored in completed form as
    // a header and data content; accessors go
    // back and forth to individual fields.
    int id;  // 29 bit header
    int length;
    byte[] data = new byte[8];
    
    int nodeAlias;

    public int getHeader() { return id; }
    public void setHeader(int id) { this.id = id; }
    
    public byte[] getData() { 
        // return a copy of appropriate length
        byte[] copy = new byte[length];
        System.arraycopy(data,0,copy,0,length);
        return copy; 
    }
    public void setData(byte[] b) { data = b; length = b.length;}
    public long bodyAsLong() {
        long retval = 0;
        for (int i = 0 ; i<data.length; i++) {
            retval = retval << 8 | (data[i]&0xFF);
        }
        return retval;
    }
    public long dataAsLong() {
        long retval = 0;
        for (int i = 2 ; i<data.length; i++) {
            retval = retval << 8 | (data[i]&0xFF);
        }
        return retval;
    }
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

  void setDestAlias(int a) {
    data[0] = (byte)((a>>8)&0xF);
    data[1] = (byte)(a&0xFF);
    length = (length<2) ? 2 : length;
  }

    /** Sets the continuation bits for an addressed frame. */
  void setContinuation(boolean first, boolean last) {
      data[0] &= CONTINUATION_BITS_MASK;
      if (!first) data[0] |= CONTINUATION_BITS_NOT_FIRST_FRAME;
      if (!last) data[0] |= CONTINUATION_BITS_NOT_LAST_FRAME;
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
    id |=  ((f << SHIFT_VARIABLE_FIELD) & MASK_VARIABLE_FIELD);
  }

  int getVariableField() {
    return (int)(id & MASK_VARIABLE_FIELD) >> SHIFT_VARIABLE_FIELD;
  }
  
  // end of basic message structure
  
  // start of CAN-level messages
 
  static final int RIM_VAR_FIELD = 0x0700;
  static final int AMD_VAR_FIELD = 0x0701;
  static final int AME_VAR_FIELD = 0x0702;
  static final int AMR_VAR_FIELD = 0x0703;

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

  boolean isAliasMapDefinition() {
      return isFrameTypeCAN() && getVariableField() == AMD_VAR_FIELD;
  }
  boolean isAliasMapEnquiry() {
      return isFrameTypeCAN() && getVariableField() == AME_VAR_FIELD;
  }
  boolean isAliasMapReset() {
      return isFrameTypeCAN() && getVariableField() == AMR_VAR_FIELD;
  }

  void setAMR(int alias, NodeID nid) {
    init(alias);
    setFrameTypeCAN();
    setVariableField(AMR_VAR_FIELD);
    length=6;
    byte[] val = nid.getContents();
    data[0] = val[0];
    data[1] = val[1];
    data[2] = val[2];
    data[3] = val[3];
    data[4] = val[4];
    data[5] = val[5];
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
  
  void setOpenLcbMTI(int mti) {
        setFrameTypeOpenLcb();
        setVariableField(mti | (FRAME_FORMAT_MTI << 12) );
  }
  
  boolean isOpenLcbMTI(int mti) {
      return isFrameTypeOpenLcb() 
                && ( getOpenLcbFormat() == FRAME_FORMAT_MTI )
                && ( (getVariableField()&~MASK_OPENLCB_FORMAT) == mti );
  }

  // end of OpenLCB format and decode support
  
  // start of OpenLCB messages
  
  void setPCEventReport(EventID eid) {
    init(nodeAlias);
    setOpenLcbMTI(MessageTypeIdentifier.ProducerConsumerEventReport.mti());
    length=8;
    loadFromEid(eid);
  }
  
  boolean isPCEventReport() {
      return isOpenLcbMTI(MessageTypeIdentifier.ProducerConsumerEventReport.mti());
  }

  void setLearnEvent(EventID eid) {
    init(nodeAlias);
    setOpenLcbMTI(MessageTypeIdentifier.LearnEvent.mti());
    length=8;
    loadFromEid(eid);
  }

  boolean isLearnEvent() {
      return isOpenLcbMTI(MessageTypeIdentifier.LearnEvent.mti());
  }

  void setInitializationComplete(int alias, NodeID nid) {
    nodeAlias = alias;
    init(nodeAlias);
    setOpenLcbMTI(MessageTypeIdentifier.InitializationComplete.mti());
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
      return isOpenLcbMTI(MessageTypeIdentifier.InitializationComplete.mti());
  }
  
  EventID getEventID() {
    return new EventID(data);
  }
  
  NodeID getNodeID() {
    return new NodeID(data);
  }
  
  boolean isVerifyNID() {
      return isOpenLcbMTI(MessageTypeIdentifier.VerifyNodeIdGlobal.mti());
  }

  void setVerifyNID(NodeID nid) {
    init(nodeAlias);
    setOpenLcbMTI(MessageTypeIdentifier.VerifyNodeIdGlobal.mti());
    length=0;
  }

  boolean isVerifiedNID() {
      return isOpenLcbMTI(MessageTypeIdentifier.VerifiedNodeId.mti());
  }

  void setVerifiedNID(NodeID nid) {
    init(nodeAlias);
    setOpenLcbMTI(MessageTypeIdentifier.VerifiedNodeId.mti());
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
      return isOpenLcbMTI(MessageTypeIdentifier.IdentifyConsumer.mti());
  }

  void setConsumerIdentified(EventID eid) {
    init(nodeAlias);
    setOpenLcbMTI(MessageTypeIdentifier.IdentifyConsumer.mti());
    length=8;
    loadFromEid(eid);
  }

  void setConsumerIdentifyRange(EventID eid, EventID mask) {
    // does send a message, but not complete yet - RGJ 2009-06-14
    init(nodeAlias);
    setOpenLcbMTI(MessageTypeIdentifier.ConsumerIdentifyRange.mti());
    length=8;
    loadFromEid(eid);
  }

  boolean isIdentifyProducers() {
      return isOpenLcbMTI(MessageTypeIdentifier.IdentifyProducer.mti());
  }

  void setProducerIdentified(EventID eid) {
    init(nodeAlias);
    setOpenLcbMTI(MessageTypeIdentifier.ProducerIdentifiedUnknown.mti());
    length=8;
    loadFromEid(eid);
  }

  void setProducerIdentifyRange(EventID eid, EventID mask) {
    // does send a message, but not complete yet - RGJ 2009-06-14
    init(nodeAlias);
    setOpenLcbMTI(MessageTypeIdentifier.ProducerIdentifyRange.mti());
    length=8;
    loadFromEid(eid);
  }

  boolean isIdentifyEventsGlobal() {
      return isOpenLcbMTI(MessageTypeIdentifier.IdentifyEventsGlobal.mti());
  }

  void loadFromEid(EventID eid) {
    length = 8;
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
                && ( (getOpenLcbFormat() == FRAME_FORMAT_ADDRESSED_DATAGRAM_ALL)
                        || (getOpenLcbFormat() == FRAME_FORMAT_ADDRESSED_DATAGRAM_FIRST)
                        || (getOpenLcbFormat() == FRAME_FORMAT_ADDRESSED_DATAGRAM_MID)
                        || (getOpenLcbFormat() == FRAME_FORMAT_ADDRESSED_DATAGRAM_LAST) )
                && (nodeAlias == getVariableField() );
  }
  // just checks 1st, assumes datagram already checked.
  boolean isLastDatagram() {
      return (getOpenLcbFormat() == FRAME_FORMAT_ADDRESSED_DATAGRAM_LAST);
  }

    /**
     * create a single datagram frame
     */
  void setDatagram(int[] content, int destAlias, boolean first, boolean last) {
    init(nodeAlias);
    if (last) {
        if (first)
            setVariableField((FRAME_FORMAT_ADDRESSED_DATAGRAM_ALL << 12 ) | destAlias);
        else
            setVariableField((FRAME_FORMAT_ADDRESSED_DATAGRAM_LAST << 12 ) | destAlias);
    } else {
        if (first)
            setVariableField((FRAME_FORMAT_ADDRESSED_DATAGRAM_FIRST << 12 ) | destAlias);
        else
            setVariableField((FRAME_FORMAT_ADDRESSED_DATAGRAM_MID << 12 ) | destAlias);
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
        String retval = "[0x"+Integer.toHexString(id)+"]";
        for (int i = 0; i<length; i++) {
            retval += " "+data[i];
        }
        return retval;
    }

    // the following constants really shouldn't leak out of this class

    /**
     * OpenLCB CAN MTI format bits
     */
    static final int FRAME_FORMAT_MTI                      = 1; 
    //
    //
    static final int FRAME_FORMAT_ADDRESSED_DATAGRAM_ALL   = 2;
    static final int FRAME_FORMAT_ADDRESSED_DATAGRAM_FIRST = 3;
    static final int FRAME_FORMAT_ADDRESSED_DATAGRAM_MID   = 4;
    static final int FRAME_FORMAT_ADDRESSED_DATAGRAM_LAST  = 5;
    static final int FRAME_FORMAT_ADDRESSED_NON_DATAGRAM   = 6;
    static final int FRAME_FORMAT_STREAM_CODE              = 7;

    // Continuation bits
    static final byte CONTINUATION_BITS_MASK = (byte) 0xCF;
    static final byte CONTINUATION_BITS_NOT_FIRST_FRAME = (byte) 0x20;
    static final byte CONTINUATION_BITS_NOT_LAST_FRAME = (byte) 0x10;

}
