package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Initialization Complete message implementation
 * with the SImple Protocol modifier set
 *
 * @author  Bob Jacobsen   Copyright 2024
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class InitializationCompleteSimpleMessage extends InitializationCompleteMessage {
    
    public InitializationCompleteSimpleMessage(NodeID source) {
        super(source);
    }

    public String toString() {
        return super.toString()
                +" with Simple Protocol";    
    }

    public int getMTI() { return MTI_INITIALIZATION_COMPLETE_SIMPLE; }
}
