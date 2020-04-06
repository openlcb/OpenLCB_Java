package org.openlcb.swing.networktree;

import java.awt.GraphicsEnvironment;
import java.util.Collection;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlcb.AbstractConnection;
import org.openlcb.Connection;
import org.openlcb.EventID;
import org.openlcb.EventState;
import org.openlcb.Message;
import org.openlcb.MimicNodeStore;
import org.openlcb.NodeID;
import org.openlcb.ProducerIdentifiedMessage;

/**
 * @author Paul Bender Copyright (C) 2017	
 */
public class NodeTreeRepTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        MimicNodeStore store = null;
        NodeID nid1 = new NodeID(new byte[]{1,3,3,4,5,6});
    
        ProducerIdentifiedMessage pim1 = new ProducerIdentifiedMessage(nid1, 
                                         new EventID(new byte[]{1,0,0,0,0,0,1,0}), EventState.Unknown);
        Connection connection = new AbstractConnection() {
            @Override
            public void put(Message msg, Connection sender) {
            }
        };
        store = new MimicNodeStore(connection, nid1);
        store.put(pim1,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();

        DefaultMutableTreeNode nodes = new DefaultMutableTreeNode("OpenLCB Network");
        DefaultTreeModel treeModel = new DefaultTreeModel(nodes);
        NodeTreeRep.SelectionKeyLoader loader = new NodeTreeRep.SelectionKeyLoader() {
                    @Override
                    public NodeTreeRep.SelectionKey cdiKey(String name, NodeID node) {
                        return new NodeTreeRep.SelectionKey(name, node) {
                            @Override
                            public void select(DefaultMutableTreeNode rep) {
                                System.out.println("Making special fuss over: "+rep+" for "+name+" on "+node);
                            }
                        };
                    }
                };

        NodeTreeRep t = new NodeTreeRep((MimicNodeStore.NodeMemo)list.toArray()[0],store,treeModel,loader);
        Assert.assertNotNull("exists",t);
        store.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
}
