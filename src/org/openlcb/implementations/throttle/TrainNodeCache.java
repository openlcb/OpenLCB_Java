package org.openlcb.implementations.throttle;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.openlcb.*;

/**
 * Maintain a cache of Train objects on OpenLCB network
 *
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class TrainNodeCache extends AbstractNodeCache {

    public TrainNodeCache() {
        super(new EventID("01.01.00.00.00.00.03.01"));
    }
}
