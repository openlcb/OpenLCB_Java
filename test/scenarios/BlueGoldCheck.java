package scenarios;

import org.nmra.net.*;
import org.nmra.net.implementations.*;
import org.nmra.net.swing.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Simulate 6 nodes interacting on a single gather/scatter
 * for testing blue/gold programming.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class BlueGoldCheck extends TestCase {

    NodeID id1 = new NodeID(new byte[]{0,0,0,0,0,1});
    NodeID id2 = new NodeID(new byte[]{0,0,0,0,0,2});
    NodeID id3 = new NodeID(new byte[]{0,0,0,0,0,3});
    NodeID id4 = new NodeID(new byte[]{0,0,0,0,0,4});
    NodeID id5 = new NodeID(new byte[]{0,0,0,0,0,5});
    NodeID id6 = new NodeID(new byte[]{0,0,0,0,0,6});

    EventID event0 = new EventID(new byte[]{0,0,0,0,0,0,0,0});
    
    SingleProducerNode node1;
    SingleProducerNode node2;
    SingleProducerNode node3;
    SingleConsumerNode node4;
    SingleConsumerNode node5;
    SingleConsumerNode node6;
    
    ScatterGather sg;
    
    public void setUp() throws Exception {
        // add a monitor frame
        JFrame f = new JFrame();
        f.setTitle("Blue-Gold Check");
        MonPane m = new MonPane();
        f.add( m );
        m.initComponents();
        f.pack();
        f.setVisible(true);
 
        sg = new ScatterGather();

        sg.register(m.getConnection());

        // create and connect the nodes
        node1 = new SingleProducerNode(id1, sg.getConnection(), event0);
        sg.register(node1);
        
        node2 = new SingleProducerNode(id2, sg.getConnection(), event0);
        sg.register(node2);
        
        node3 = new SingleProducerNode(id3, sg.getConnection(), event0);
        sg.register(node3);
        
        
        node4 = new SingleConsumerNode(id4, sg.getConnection(), event0);
        sg.register(node4);
        
        node5 = new SingleConsumerNode(id5, sg.getConnection(), event0);
        sg.register(node5);
        
        node6 = new SingleConsumerNode(id6, sg.getConnection(), event0);
        sg.register(node6);
        
        node1.initialize();
        node2.initialize();
        node3.initialize();
        node4.initialize();
        node5.initialize();
        node6.initialize();

        // and hook up the GUI
        f = new JFrame();
        f.setTitle("Blue-Gold Check");
        JPanel p = new JPanel();
        f.getContentPane().add( p );
        f.getContentPane().setLayout(new FlowLayout());
        
        p.setLayout(new GridLayout(2,3));
        
        ConsumerPane cp;
        cp = new ConsumerPane("C1");
        sg.register(cp.getConnection());
        p.add(cp);
        cp = new ConsumerPane("C2");
        sg.register(cp.getConnection());
        p.add(cp);
        cp = new ConsumerPane("C3");
        sg.register(cp.getConnection());
        p.add(cp);

        ProducerPane pp;
        pp = new ProducerPane("P1", node1);
        p.add(pp);
        pp = new ProducerPane("P2", node2);
        p.add(pp);
        pp = new ProducerPane("P3", node3);
        p.add(pp);
        
        f.pack();
        f.setVisible(true);

        // composite GUI
        f = new JFrame();
        f.setTitle("Blue-Gold Check test node");
        f.setTitle("BG Node Simulation");
        BGnodePanel b = new BGnodePanel();
        f.getContentPane().add( b );
        f.getContentPane().setLayout(new FlowLayout());

        b.addProducer(node1, "P1");
        b.addProducer(node2, "P2");
        b.addProducer(node3, "P3");

        b.addConsumer(node4, "C1", sg);
        b.addConsumer(node5, "C2", sg);
        b.addConsumer(node6, "C3", sg);

        f.pack();
        f.setVisible(true);
    }
    
    public void tearDown() {}
    
    public void testSetup() {
        // just run the setup to make sure it works
    }
    
    // from here down is testing infrastructure
    
    public BlueGoldCheck(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {BlueGoldCheck.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(BlueGoldCheck.class);
        return suite;
    }

    /** 
     * Captive class to demonstrate B-G protocol, will 
     * probably need to go elsewhere after seperating 
     * algorithm and Swing display.
     */
     class BGnodePanel extends JPanel {
     
        public BGnodePanel() {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(consumerPanel);
            this.add(producerPanel);
            this.add(new JSeparator());
            
            // add controls
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());
            this.add(p);
            
            JButton b;
            b = new JButton("Blue");
            p.add(b);
            b = new JButton("Gold");
            p.add(b);
            
        }
        
        ArrayList<SingleProducerNode> producers = new ArrayList<SingleProducerNode>();
        JPanel producerPanel = new JPanel();
        
        /**
         * Add a producer to node.
         * Note this should be a working producer, 
         * already registered, etc
         */
         public void addProducer(SingleProducerNode n, String name) throws Exception {
            producers.add(n);
            producerPanel.add(new ProducerPane(name, n));
         }

        ArrayList<SingleConsumerNode> consumers = new ArrayList<SingleConsumerNode>();
        JPanel consumerPanel = new JPanel();
        
        /**
         * Add a consumer to node.
         * Note this should be a working consumer, 
         * already registered, etc
         */
         public void addConsumer(SingleConsumerNode n, String name, ScatterGather sg) throws Exception {
            consumers.add(n);
            ConsumerPane cp = new ConsumerPane(name);
            sg.register(cp.getConnection());
            consumerPanel.add(cp);
         }
     }
     
}
