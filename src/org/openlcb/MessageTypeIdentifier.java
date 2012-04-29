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

        ProtocolIdentification( 0x800000000000L,"ProtocolIdentification"), 
        Datagram(               0x400000000000L,"Datagram"),
        Stream(                 0x200000000000L,"Stream"), 
        Configuration(          0x100000000000L,"Configuration"),
        Reservation(            0x080000000000L,"Reservation"),
        ProducerConsumer(       0x040000000000L,"ProducerConsumer"),
        Identification(         0x020000000000L,"Identification"),
        TeachingLearningConfiguration(0x010000000000L,"TeachingLearningConfiguration"),
        RemoteButton(           0x008000000000L,"RemoteButton"),
        AbbreviatedDefaultCDI(  0x004000000000L,"AbbreviatedDefaultCDI"),
        Display(                0x002000000000L,"Display");
       
        MessageTypeIdentifier(long value, String name) {
            this.value = value;
            this.name = name;
        }
        long value;
        String name;
        
        boolean supports(long r) {
            return ( (this.value & r) != 0 );
        }
        
        static List<String> decode(long r) {
            ArrayList<String> retval = new ArrayList<String>();
            for (MessageTypeIdentifier t : MessageTypeIdentifier.values()) {
                if ( t.supports(r) ) retval.add(t.name);
            }
            return retval;
        }
           
}
