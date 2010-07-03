package org.openlcb.can;

public interface OpenLcbCan {

    /**
     * OpenLCB CAN MTI format bits
     */
    static final int MTI_FORMAT_SIMPLE_MTI               = 0; 
    static final int MTI_FORMAT_COMPLEX_MTI              = 1;
    //
    //
    static final int MTI_FORMAT_ADDRESSED_DATAGRAM       = 4;
    static final int MTI_FORMAT_ADDRESSED_DATAGRAM_LAST  = 5;
    static final int MTI_FORMAT_ADDRESSED_NON_DATAGRAM   = 6;
    static final int MTI_FORMAT_STREAM_CODE              = 7;
    
    
    /**
     * Basic header MTI definitions for OpenLCB on CAN.
     * Low bits 0x00F on all of these automatically
     */
     
    static final int MTI_INITIALIZATION_COMPLETE     = 0x08F;
    
    static final int MTI_VERIFY_NID                  = 0x0AF;
    static final int MTI_VERIFIED_NID                = 0x0BF;
    
    static final int MTI_IDENTIFY_CONSUMERS          = 0x24F;
    static final int MTI_IDENTIFY_CONSUMERS_RANGE    = 0x25F;
    static final int MTI_CONSUMER_IDENTIFIED         = 0x26F;
    
    static final int MTI_IDENTIFY_PRODUCERS          = 0x28F;
    static final int MTI_IDENTIFY_PRODUCERS_RANGE    = 0x29F;
    static final int MTI_PRODUCER_IDENTIFIED         = 0x2AF;
    
    static final int MTI_IDENTIFY_EVENTS             = 0x2BF;
    
    static final int MTI_LEARN_EVENT                 = 0x2CF;
    static final int MTI_PC_EVENT_REPORT             = 0x2DF;
    
    static final int MTI_DATAGRAM_RCV_OK             = 0x4CF;
    static final int MTI_DATAGRAM_REJECTED           = 0x4DF;

}