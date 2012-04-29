// MessageTypeIdentifier.java

package org.openlcb;

import java.util.ArrayList;
import java.util.List;

/**
 * Message Type Identifiers
 *
 * Central place to carry the (only) numerical values for 
 * Message Type Identifiers
 *
 * @see             http://www.openlcb.org/trunk/specs/drafts/
 * @author			Bob Jacobsen   Copyright (C) 2012
 * @version			$Revision: 18542 $
 *
 */
public enum MessageTypeIdentifier {

        // Arguments are addressed, 
        InitializationComplete( false, 0x1080, 0x18017, 0x00, "InitializationComplete"), 
        VerifyNodeIdAddressed ( true,  0x1080, 0x18017, 0x00, "VerifyNodeIdAddressed"), 
        VerifyNodeIdGlobal    ( false, 0x1080, 0x18017, 0x00, "VerifyNodeIdGlobal"),
        VerifiedNodeId        ( false, 0x1080, 0x18017, 0x00, "VerifyNodeIdGlobal"),
        OptionalIntRejected   ( false, 0x1080, 0x18017, 0x00, "VerifyNodeIdGlobal"),
        TerminateDueToError   ( false, 0x1080, 0x18017, 0x00, "VerifyNodeIdGlobal");
       
        MessageTypeIdentifier(boolean addressed, long genMTI, int a, int b, String name) {
            this.genMTI = genMTI;
            this.name = name;
        }
        long genMTI;
        String name;
        
        public String toString() {
            return name;
        }
}
