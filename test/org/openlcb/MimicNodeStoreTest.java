package org.openlcb;

import org.junit.*;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.beans.PropertyChangeListener;

/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class MimicNodeStoreTest {
    MimicNodeStore store = null;
    
    NodeID nid1 = new NodeID(new byte[]{1,3,3,4,5,6});
    NodeID nid2 = new NodeID(new byte[]{2,3,3,4,5,6});
    
    ProducerIdentifiedMessage pim1 = new ProducerIdentifiedMessage(nid1, 
                                                new EventID(new byte[]{1,0,0,0,0,0,1,0}), EventState.Unknown);
                                                
    ProducerIdentifiedMessage pim2 = new ProducerIdentifiedMessage(nid2, 
                                                new EventID(new byte[]{1,0,0,0,0,0,1,0}), EventState.Unknown);
    
    SimpleNodeIdentInfoReplyMessage snii1 = new SimpleNodeIdentInfoReplyMessage(nid1, nid2,
                                                new byte[]{1,'a','b','c'});
    
    PropertyChangeListener listener;
    boolean listenerFired;
    Message lastMessage;
    Connection connection = new AbstractConnection() {
        public void put(Message msg, Connection sender) {
            lastMessage = msg;
        }
    };
    
    NodeID src = new NodeID(new byte[]{1,2,3,4,5,6});

    @Before    
    public void setUp() {
        store = new MimicNodeStore(connection, src);
        lastMessage = null;
        
        listener = new PropertyChangeListener(){
            public void propertyChange(java.beans.PropertyChangeEvent e) { listenerFired = true; }
        };
        listenerFired = false;
    }

    @After
    public void tearDown() {
       store.dispose();
       store = null;
       listener = null;
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull(store);
    }
    
    @Test
    public void testListExists() {
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        Assert.assertNotNull(list);        
    }

    @Test
    public void testListInitiallyEmpty() {
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        Assert.assertTrue(list.size()==0);        
    }
    
    @Test
    public void testAcceptsMessage() {
        store.put(pim1,null);
    }
    
    @Test
    public void testMessageAddsToList() {
        store.put(pim1,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        Assert.assertTrue(list.size()==1);
    }
    
    @Test
    public void testMessageMemoKnowsNodeID() {
        store.put(pim1,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        Assert.assertEquals(list.iterator().next().getNodeID(), nid1);
    }
    
    @Test
    public void testHandleMultipleNodes() {
        store.put(pim1,null);
        store.put(pim2,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        Assert.assertTrue(list.size()==2);
    }
    
    @Test
    public void testHandleMultipleMessagesFromNode() {
        store.put(pim1,null);
        store.put(pim1,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        Assert.assertTrue(list.size()==1);
    }
    
    @Test
    public void testNoDefaultProtocolInfo() {
        store.put(pim1,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        MimicNodeStore.NodeMemo memo = list.iterator().next();

        Assert.assertNotNull(memo.getProtocolIdentification());
    }

    @Test
    public void testProtocolInfoAvailableFromNode() {
        store.put(pim1,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        MimicNodeStore.NodeMemo memo = list.iterator().next();
        store.put(new ProtocolIdentificationReplyMessage(nid1, nid2, 0xF00000000000L), null);

        Assert.assertNotNull(memo.getProtocolIdentification());
    }
    
    @Test
    public void testForNotificationOfNode() {
        store.addPropertyChangeListener(
            new PropertyChangeListener(){
            public void propertyChange(java.beans.PropertyChangeEvent e) { 
                MimicNodeStore.NodeMemo memo = (MimicNodeStore.NodeMemo) e.getNewValue();
                memo.addPropertyChangeListener(listener);
            }
        });
        store.put(pim1,null);
        Assert.assertFalse(listenerFired);

        store.put(new ProtocolIdentificationReplyMessage(nid1, nid2, 0xF00000000000L), null);
        Assert.assertTrue(listenerFired);
        
    }

    @Test
    public void testForNotificationOfOnlyOneNode() {
        store.put(pim1,null);

        store.addPropertyChangeListener(
            new PropertyChangeListener(){
            public void propertyChange(java.beans.PropertyChangeEvent e) { 
                MimicNodeStore.NodeMemo memo = (MimicNodeStore.NodeMemo) e.getNewValue();
                memo.addPropertyChangeListener(listener);
            }
        });
        store.put(pim2,null);
        Assert.assertFalse(listenerFired);

        store.put(new ProtocolIdentificationReplyMessage(nid1, nid2, 0xF00000000000L), null);
        Assert.assertFalse(listenerFired);

        store.put(new ProtocolIdentificationReplyMessage(nid2, nid2, 0xF00000000000L), null);
        Assert.assertTrue(listenerFired);
        
    }

    @Test
    public void testForNotificationOfProtocolIdent() {
        store.addPropertyChangeListener(listener);
        store.put(pim1,null);
        
        Assert.assertTrue(listenerFired);
    }
    
    @Test
    public void testNoDefaultSimpleInfo() {
        store.put(pim1,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        MimicNodeStore.NodeMemo memo = list.iterator().next();

        Assert.assertNotNull(memo.getSimpleNodeIdent());
    }

    @Test
    public void testSimpleInfoAvailableFromNode() {
        store.put(pim1,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        MimicNodeStore.NodeMemo memo = list.iterator().next();
        store.put(snii1, null);

        Assert.assertNotNull(memo.getSimpleNodeIdent());
        
        Assert.assertEquals("abc", memo.getSimpleNodeIdent().getMfgName());
    }

    @Test
    public void testSimpleInfoRetry() {
        store.put(pim1,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        MimicNodeStore.NodeMemo memo = list.iterator().next();

        Assert.assertNull(lastMessage);
        store.put(new OptionalIntRejectedMessage(nid1,src,0x0DE8,0x1000), null);
        Assert.assertNotNull(lastMessage);
        
        Assert.assertEquals(lastMessage, new SimpleNodeIdentInfoRequestMessage(src, nid1) );
    }
    
    @Test
    public void testFindNodeNotPresent() {
        MimicNodeStore.NodeMemo retval = store.findNode(nid1);

        Assert.assertTrue(retval == null);
        Assert.assertTrue(lastMessage.equals(new VerifyNodeIDNumberGlobalMessage(src, nid1)));
        
    }
    
    @Test
    public void testFindNodePresent() {
        store.put(pim1,null);

        MimicNodeStore.NodeMemo  retval = store.findNode(nid1);
        
        Assert.assertTrue(retval != null);
        Assert.assertTrue(lastMessage == null);
        
    }

    @Test
    public void testRefresh() {
        store.put(pim1,null);
        store.put(pim2,null);
        Assert.assertNotNull(store.findNode(nid1));
        Assert.assertNotNull(store.findNode(nid2));

        // Adds a dummy listener to test callbacks.
        class MyListener implements PropertyChangeListener {
            public PropertyChangeEvent lastEvent;

            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                lastEvent = propertyChangeEvent;
            }
        };
        MyListener l = new MyListener();
        store.addPropertyChangeListener(l);
        store.refresh();

        // There is a side effect of a callback to clear everything.
        Assert.assertNotNull(l.lastEvent);
        Assert.assertEquals(MimicNodeStore.CLEAR_ALL_NODES,l.lastEvent.getPropertyName());

        // And a verify node ID message going out.
        Assert.assertNotNull(lastMessage);
        Assert.assertTrue(lastMessage instanceof VerifyNodeIDNumberGlobalMessage);

        // As well as the node tree being clear now.
        Assert.assertEquals(0, store.getNodeMemos().size());
        Assert.assertNull(store.findNode(nid1));
        Assert.assertNull(store.findNode(nid2));
    }

}
