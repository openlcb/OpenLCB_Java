package org.openlcb;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class NodeTest  {

    @Test
    public void testCtor() {
        new Node(new NodeID(new byte[]{0,1,2,3,4,5}));
    }
}
