package org.openlcb.can;

import org.openlcb.*;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2010
 */
public class AliasMapTest  {
 
    @Test	
    public void testEmptyStart() {
        AliasMap map = new AliasMap();
        
        Assert.assertEquals("get Alias", -1, map.getAlias(new NodeID(new byte[]{0,1,2,3,4,5})));
        Assert.assertEquals("get NodeID", new NodeID(), map.getNodeID(0));
    }
   
    @Test 
    public void testAfterFrame() {
        AliasMap map = new AliasMap();
        
        OpenLcbCanFrame f = new OpenLcbCanFrame(0x123);
        f.setInitializationComplete(0x123, new NodeID(new byte[]{0,1,2,3,4,5}), false);
        map.processFrame(f);
        Assert.assertEquals("check NodeID", new NodeID(new byte[]{0,1,2,3,4,5}), map.getNodeID(0x123));
        Assert.assertEquals("check alias", 0x123, map.getAlias(new NodeID(new byte[]{0,1,2,3,4,5})));
    }
    
    @Test 
    public void testAfterInsert() {
        AliasMap map = new AliasMap();
        
        map.insert(0x123, new NodeID(new byte[]{0,1,2,3,4,5}));
        Assert.assertEquals("check NodeID", new NodeID(new byte[]{0,1,2,3,4,5}), map.getNodeID(0x123));
        Assert.assertEquals("check alias", 0x123, map.getAlias(new NodeID(new byte[]{0,1,2,3,4,5})));
    }
    
    @Test 
    public void testAfterAMR() {
        AliasMap map = new AliasMap();
        
        map.insert(0x123, new NodeID(new byte[]{0,1,2,3,4,5}));
        
        // remove with AMR
        OpenLcbCanFrame f = new OpenLcbCanFrame(0x123);
        f.setAMR(0x123, new NodeID(new byte[]{0,1,2,3,4,5}));
        map.processFrame(f);

        Assert.assertEquals("get Alias", -1, map.getAlias(new NodeID(new byte[]{0,1,2,3,4,5})));
        Assert.assertEquals("get NodeID", new NodeID(), map.getNodeID(0));
    }

    @Test
    public void testWatcher() {
        AliasMap map = new AliasMap();

        NodeID nid = new NodeID(new byte[]{1,2,3,4,5,6});
        int a = 432;

        final boolean[] found = {false};
        map.addWatcher(new AliasMap.Watcher() {
            @Override
            public void aliasAdded(NodeID id, int alias) {
                found[0] = true;
                Assert.assertEquals(nid, id);
                Assert.assertEquals(a, alias);
            }
        });

        map.insert(a, nid);
        Assert.assertTrue(found[0]);

        found[0] = false;

        OpenLcbCanFrame f = new OpenLcbCanFrame(a);
        f.setInitializationComplete(a, nid, false);
        map.processFrame(f);

        Assert.assertTrue(found[0]);
    }

}
