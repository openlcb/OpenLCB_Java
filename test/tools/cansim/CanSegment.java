package tools.cansim;

import tools.*;

/**
 * CanSegment does a discrete-time simulation of a CAN link
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */

public class CanSegment implements Timed {
    java.util.List<CanInterface> interfaces = new java.util.ArrayList<CanInterface>();
    long time = 0;
        
    public void add(CanInterface item) {
        interfaces.add(item);
    }
        
    public void tick(long time) {
    }
    
    public void send(CanFrame f, CanInterface sender) {
        // simple implementation doesn't arbitrate yet
        sender.done();
        for (CanInterface i : interfaces) {
            if (i != sender)
                i.receive(f);
        }
    }
    
}
