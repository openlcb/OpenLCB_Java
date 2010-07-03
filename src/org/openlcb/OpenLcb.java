package org.openlcb;

public interface OpenLcb {

    
    /**
     * MTI definitions for OpenLCB.
     */
     
    static final int MTI_INITIALIZATION_COMPLETE     = 0x3080;
    
    static final int MTI_VERIFY_NID                  = 0x30A4;
    static final int MTI_VERIFIED_NID                = 0x30B0; // also 30B2 
    
    static final int MTI_IDENTIFY_CONSUMERS          = 0x3242;
    static final int MTI_IDENTIFY_CONSUMERS_RANGE    = 0x3252;
    static final int MTI_CONSUMER_IDENTIFIED         = 0x3263;
    
    static final int MTI_IDENTIFY_PRODUCERS          = 0x3282;
    static final int MTI_IDENTIFY_PRODUCERS_RANGE    = 0x3292;
    static final int MTI_PRODUCER_IDENTIFIED         = 0x32A3;
    
    static final int MTI_IDENTIFY_EVENTS             = 0x32B0; // also 32B2
    
    static final int MTI_LEARN_EVENT                 = 0x32C2;
    static final int MTI_PC_EVENT_REPORT             = 0x32D2;
    
    static final int MTI_DATAGRAM                    = 0x3404;
    static final int MTI_DATAGRAM_RCV_OK             = 0x34C4;
    static final int MTI_DATAGRAM_REJECTED           = 0x34D4;

    static final int MTI_STREAM_INIT_REQUEST         = 0x34E4;
    static final int MTI_STREAM_INIT_REPLY           = 0x34F4;
    static final int MTI_STREAM_DATA_SEND            = 0x3694;
    static final int MTI_STREAM_DATA_PROCEED         = 0x36A4;
    static final int MTI_STREAM_DATA_COMPLETE        = 0x36B4;

}