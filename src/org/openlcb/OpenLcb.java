package org.openlcb;

public interface OpenLcb {

    
    /**
     * MTI definitions for OpenLCB.
     */
     
    static final int MTI_INITIALIZATION_COMPLETE     = 0x3080;
    
    static final int MTI_VERIFY_NID                  = 0x10A4;
    static final int MTI_VERIFIED_NID                = 0x10B0;
    
    static final int MTI_IDENTIFY_CONSUMERS          = 0x1242;
    static final int MTI_IDENTIFY_CONSUMERS_RANGE    = 0x3252;
    static final int MTI_CONSUMER_IDENTIFIED         = 0x3263;
    
    static final int MTI_IDENTIFY_PRODUCERS          = 0x1282;
    static final int MTI_IDENTIFY_PRODUCERS_RANGE    = 0x3292;
    static final int MTI_PRODUCER_IDENTIFIED         = 0x32A3;
    
    static final int MTI_IDENTIFY_EVENTS             = 0x12B4;
    
    static final int MTI_LEARN_EVENT                 = 0x12C2;
    static final int MTI_PC_EVENT_REPORT             = 0x12D2;
    
    static final int MTI_DATAGRAM                    = 0x1404;
    static final int MTI_DATAGRAM_RCV_OK             = 0x14C4;
    static final int MTI_DATAGRAM_REJECTED           = 0x14D4;

    static final int MTI_STREAM_INIT_REQUEST         = 0x14E4;
    static final int MTI_STREAM_INIT_REPLY           = 0x14F4;
    static final int MTI_STREAM_DATA_SEND            = 0x1694;
    static final int MTI_STREAM_DATA_PROCEED         = 0x16A4;
    static final int MTI_STREAM_DATA_COMPLETE        = 0x16B4;

    static final int MTI_PROTOCOL_IDENT_REQUEST      = 0x12E4;
    static final int MTI_PROTOCOL_IDENT_REPLY        = 0x12F4;

    static final int MTI_SIMPLE_NODE_IDENT_REQUEST   = 0x3520;
    static final int MTI_SIMPLE_NODE_IDENT_REPLY     = 0x3530;

}