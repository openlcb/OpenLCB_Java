package org.openlcb;

/**
 * Represents the constants encoded in the OpenLCB Event Identifiers Standard.
 *
 * Created by bracz on 12/29/15.
 */
public class CommonIdentifiers {

    // Well-known automatically routed event IDs.

    public static final EventID EMERGENCY_OFF = new EventID("01.00.00.00.00.00.FF.FF");
    public static final EventID CLEAR_EMERGENCY_OFF = new EventID("01.00.00.00.00.00.FF.FE");
    public static final EventID EMERGENCY_STOP = new EventID("01.00.00.00.00.00.FF.FD");
    public static final EventID CLEAR_EMERGENCY_STOP = new EventID("01.00.00.00.00.00.FF.FC");

    public static final EventID NEW_LOG_ENTRY = new EventID("01.00.00.00.00.00.FF.F8");
    public static final EventID IDENT_BUTTON_PRESSED = new EventID("01.00.00.00.00.00.FE.00");

    public static final EventID LINK_ERROR_1 = new EventID("01.00.00.00.00.00.FD.01");
    public static final EventID LINK_ERROR_2 = new EventID("01.00.00.00.00.00.FD.02");
    public static final EventID LINK_ERROR_3 = new EventID("01.00.00.00.00.00.FD.03");
    public static final EventID LINK_ERROR_4 = new EventID("01.00.00.00.00.00.FD.04");


    // Well-known event IDs.

    public static final EventID DUPLICATE_NODE_ID = new EventID("01.01.00.00.00.00.02.01");
    public static final EventID IS_TRAIN = new EventID("01.01.00.00.00.00.03.03");
    public static final EventID IS_TRACTION_PROXY = new EventID("01.01.00.00.00.00.03.04");

}
