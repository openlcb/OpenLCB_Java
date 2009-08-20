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
        
        
        // composite GUI
        java.util.List<SingleProducerNode> producers 
            = new ArrayList<SingleProducerNode>();
        producers.add(node1);
        producers.add(node2);
        producers.add(node3);

        java.util.List<SingleConsumerNode> consumers 
            = new ArrayList<SingleConsumerNode>();
        consumers.add(node4);
        consumers.add(node5);
        consumers.add(node6);
        f = new BGnodeFrame("BG simulated node 1", producers, consumers, id1);
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
     class BGnodeFrame extends JFrame {
        public BGnodeFrame(String name, 
                java.util.List<SingleProducerNode> producers,
                java.util.List<SingleConsumerNode> consumers,
                NodeID nid) throws Exception {
            super(name);

            BGnodePanel b = new BGnodePanel(nid);
            getContentPane().add( b );
            getContentPane().setLayout(new FlowLayout());
    
            for (int i = 0; i<producers.size(); i++)
                b.addProducer(producers.get(i), "P"+i);
    
            for (int i = 0; i<consumers.size(); i++)
                b.addConsumer(consumers.get(i), "C"+i, sg);
        }
     }

    /** 
     * Captive class to demonstrate B-G protocol, will 
     * probably need to go elsewhere after seperating 
     * algorithm and Swing display.
     */
     class BGnodePanel extends JPanel {
     
        public BGnodePanel(NodeID nid) {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(consumerPanel);
            this.add(producerPanel);
            this.add(new JSeparator());
            
            // add controls
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());
            this.add(p);
            
            JPanel p1;
            p.setLayout(new FlowLayout());
            
            blueButton = new JButton("Blue");
            p.add(blueButton);
            blueLabel = new JLabel("   ");
            setBlueOn(false);
            p.add(blueLabel);
            
            goldButton = new JButton("Gold");
            p.add(goldButton);
            goldLabel = new JLabel("   ");
            setGoldOn(false);
            p.add(goldLabel);
            
            engine = new BlueGoldEngine(nid, sg) {
                public void setBlueLightOn(boolean f) {
                    setBlueOn(f);
                }
                
                public void setGoldLightOn(boolean f) {
                    setGoldOn(f);
                }
            };
            
            blueButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    engine.blueClick();
                }
            });
           goldButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    engine.goldClick();
                }
            });
        }
        
        JButton blueButton;
        JButton goldButton;
        JLabel blueLabel;
        JLabel goldLabel;
        
        BlueGoldEngine engine;
        
        public void setBlueOn(boolean t) {
            blueLabel.setOpaque(true);
            if (t)
                blueLabel.setBackground(java.awt.Color.blue.brighter());
            else
                blueLabel.setBackground(java.awt.Color.lightGray);
        }
        
        public void setGoldOn(boolean t) {
            goldLabel.setOpaque(true);
            if (t)
                goldLabel.setBackground(java.awt.Color.yellow);
            else
                goldLabel.setBackground(java.awt.Color.lightGray);
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
