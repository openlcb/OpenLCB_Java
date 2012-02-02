package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Collection;

/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class MimicNodeStoreTest extends TestCase {
    MimicNodeStore store = null;
    
    NodeID nid1 = new NodeID(new byte[]{1,3,3,4,5,6});
    NodeID nid2 = new NodeID(new byte[]{2,3,3,4,5,6});
    
    ProducerIdentifiedMessage pim1 = new ProducerIdentifiedMessage(nid1, 
                                                new EventID(new byte[]{1,0,0,0,0,0,1,0}));
                                                
    ProducerIdentifiedMessage pim2 = new ProducerIdentifiedMessage(nid2, 
                                                new EventID(new byte[]{1,0,0,0,0,0,1,0}));
                                                
    public void setUp() {
        store = new MimicNodeStore();
    }

    public void testCtor() {
        Assert.assertNotNull(store);
    }
    
    public void testListExists() {
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        Assert.assertNotNull(list);        
    }

    public void testListInitiallyEmpty() {
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        Assert.assertTrue(list.size()==0);        
    }
    
    public void testAcceptsMessage() {
        store.put(pim1,null);
    }
    
    public void testMessageAddsToList() {
        store.put(pim1,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        Assert.assertTrue(list.size()==1);
    }
    
    public void testMessageMemoKnowsNodeID() {
        store.put(pim1,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        Assert.assertEquals(list.iterator().next().getNodeID(), nid1);
    }
    
    public void testHandleMultipleNodes() {
        store.put(pim1,null);
        store.put(pim2,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        Assert.assertTrue(list.size()==2);
    }
    
    public void testHandleMultipleMessagesFromNode() {
        store.put(pim1,null);
        store.put(pim1,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        Assert.assertTrue(list.size()==1);
    }
    
    
    // from here down is testing infrastructure
    
    public MimicNodeStoreTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MimicNodeStoreTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MimicNodeStoreTest.class);
        return suite;
    }
}
