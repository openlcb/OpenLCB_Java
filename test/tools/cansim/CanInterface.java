package tools.cansim;

import tools.*;

/**
 * CanInterface simulates a single device on a CAN segment
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */


public interface CanInterface extends Timed {

    public void tick(long time);

    /**
     * A send operation has completed
     */
    public void done();    
    
    /**
     * A frame from some other interface has been recieved
     */
    public void receive(CanFrame f);
    
}
