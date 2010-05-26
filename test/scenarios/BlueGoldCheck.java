package scenarios;

import org.openlcb.*;
import org.openlcb.implementations.*;
import org.openlcb.swing.*;

//import junit.framework.Assert;
//import junit.framework.Test;
//import junit.framework.TestCase;
//import junit.framework.TestSuite;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Simulate 6 nodes interacting on a single gather/scatter
 * for testing blue/gold programming.
 *
 * Not JUnit, so can be run standalone easily.
 
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class BlueGoldCheck /* extends TestCase */ {
    
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

        createSampleNode(1);
        createSampleNode(2);
        createSampleNode(3);
        createSampleNode(4);
    }
    
    void createSampleNode(int index) throws Exception {
        NodeID id = new NodeID(new byte[]{0,0,0,0,0,(byte)index});

        SingleProducer producer11;
        SingleProducer producer12;
        SingleProducer producer13;
        SingleConsumer consumer11;
        SingleConsumer consumer12;
        SingleConsumer consumer13;
        
        // create and connect the nodes
        producer11 = new SingleProducer(id, sg.getConnection(), 
                                        new EventID(id, 1, 1));
        sg.register(producer11);
        
        producer12 = new SingleProducer(id, sg.getConnection(), 
                                        new EventID(id, 1, 2));
        sg.register(producer12);
        
        producer13 = new SingleProducer(id, sg.getConnection(), 
                                        new EventID(id, 1, 3));
        sg.register(producer13);
        
        
        consumer11 = new SingleConsumer(id, sg.getConnection(), 
                                        new EventID(id, 0, 1));
        sg.register(consumer11);
        
        consumer12 = new SingleConsumer(id, sg.getConnection(), 
                                        new EventID(id, 0, 2));
        sg.register(consumer12);
        
        consumer13 = new SingleConsumer(id, sg.getConnection(), 
                                        new EventID(id, 0, 3));
        sg.register(consumer13);
                
        // composite GUI
        java.util.List<SingleProducer> producers 
            = new ArrayList<SingleProducer>();
        producers.add(producer11);
        producers.add(producer12);
        producers.add(producer13);

        java.util.List<SingleConsumer> consumers 
            = new ArrayList<SingleConsumer>();
        consumers.add(consumer11);
        consumers.add(consumer12);
        consumers.add(consumer13);
        JFrame f = new BGnodeFrame("BG simulated node "+index, producers, consumers, id, sg);
        f.pack();
        f.setVisible(true);
    }
    
    public void testSetup() {}
    public void tearDown() {}
        
    // from here down is testing infrastructure
  
    public BlueGoldCheck(String s) {
        //super(s);
    }

    // Main entry point
    static public void main(String[] args) throws Exception {
        String[] testCaseName = {BlueGoldCheck.class.getName()};
        //junit.swingui.TestRunner.main(testCaseName);
        
        runTest();
    }
    
    static public void runTest() throws Exception {
        // run manually
        BlueGoldCheck g = new BlueGoldCheck("standalone");
        g.setUp();
        g.testSetup();
        g.tearDown();
        
    }

    // test suite from all defined tests
    //public static Test suite() {
    //    TestSuite suite = new TestSuite(BlueGoldCheck.class);
    //    return suite;
    //}

    // frame starting positions
    int hPos = 500;
    int vPos = 0;
    
    /** 
     * Captive class to demonstrate B-G protocol, will 
     * probably need to go elsewhere after seperating 
     * algorithm and Swing display.
     */
     class BGnodeFrame extends JFrame {
        public BGnodeFrame(String name,
                java.util.List<SingleProducer> producers,
                java.util.List<SingleConsumer> consumers,
                NodeID nid,
                ScatterGather sg) throws Exception {
            super(name);

            BGnodePanel b = new BGnodePanel(nid, producers, consumers, sg);
            getContentPane().add( b );
            getContentPane().setLayout(new FlowLayout());
    
            for (int i = 0; i<consumers.size(); i++)
                b.addConsumer(consumers.get(i), null);  // null means autolabel
            for (int i = 0; i<producers.size(); i++)
                b.addProducer(producers.get(i), null);  // null means autolabel
    
            this.setLocation(hPos, vPos);
            vPos+= 200;
        }
     }

    /** 
     * Captive class to demonstrate B-G protocol, will 
     * probably need to go elsewhere after seperating 
     * algorithm and Swing display.
     */
     class BGnodePanel extends JPanel {
     
        public BGnodePanel(NodeID nid,
                        java.util.List<SingleProducer> producers,
                        java.util.List<SingleConsumer> consumers,
                        ScatterGather sg) {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            buttons = new JPanel();
            buttons.setLayout(new GridLayout(2, Math.max(consumers.size(), producers.size())));
            this.add(buttons);
            this.add(new JSeparator());
                        
            JPanel p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            this.add (p1);
            
            blueButton = new JButton("Blue");
            p1.add(blueButton);
            blueLabel = new JLabel("   ");
            setBlueOn(false);
            p1.add(blueLabel);
            
            goldButton = new JButton("Gold");
            p1.add(goldButton);
            goldLabel = new JLabel("   ");
            setGoldOn(false);
            p1.add(goldLabel);
            
            engine = new BlueGoldEngine(nid, sg, producers, consumers) {
                public void setBlueLightOn(boolean f) {
                    setBlueOn(f);
                }
                
                public void setBlueLightBlink(int dwell) {
                    setBlueBlink(dwell);
                }

                public void setGoldLightOn(boolean f) {
                    setGoldOn(f);
                }

                public void setGoldLightBlink(int dwell) {
                    setGoldBlink(dwell);
                }

            };
            
            sg.register(engine);
            
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
        
        JPanel buttons;
        JButton blueButton;
        JButton goldButton;
        JLabel blueLabel;
        JLabel goldLabel;
        boolean blueOn;
        boolean blueBlink;
        javax.swing.Timer blueTimer = new javax.swing.Timer(500,
                    new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            blueOn = ! blueOn;
                            if (blueOn)
                                colorBlueOn();
                            else
                                colorBlueOff();
                       }
                    });     
                
        boolean goldOn;
        boolean goldBlink;
        javax.swing.Timer goldTimer = new javax.swing.Timer(500,
                    new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            goldOn = ! goldOn;
                            if (goldOn)
                                colorGoldOn();
                            else
                                colorGoldOff();
                       }
                    });     
                
        BlueGoldEngine engine;
        
        public void setBlueOn(boolean t) {
            blueBlink = false;
            blueOn = t;
            blueTimer.stop();
            blueLabel.setOpaque(true);
            if (t)
                colorBlueOn();
            else
                colorBlueOff();
        }
        
        public void setBlueBlink(int dwell) {
            blueBlink = true;
            blueOn = true;
            blueTimer.stop();
            blueTimer.setInitialDelay(dwell);
            blueTimer.setDelay(dwell);
            blueTimer.start();
            blueLabel.setOpaque(true);
            colorBlueOn();
        }
        void colorBlueOff() {
                blueLabel.setBackground(java.awt.Color.lightGray);
        }
        void colorBlueOn() {
                blueLabel.setBackground(java.awt.Color.blue.brighter().brighter());
        }

        public void setGoldOn(boolean t) {
            goldBlink = false;
            goldOn = t;
            goldTimer.stop();
            goldLabel.setOpaque(true);
            if (t)
                colorGoldOn();
            else
                colorGoldOff();
        }

        public void setGoldBlink(int dwell) {
            goldBlink = true;
            goldOn = true;
            goldTimer.stop();
            goldTimer.setInitialDelay(dwell);
            goldTimer.setDelay(dwell);
            goldTimer.start();
            goldLabel.setOpaque(true);
            colorGoldOn();
        }

        void colorGoldOff() {
                goldLabel.setBackground(java.awt.Color.lightGray);
        }
        void colorGoldOn() {
                goldLabel.setBackground(java.awt.Color.yellow.brighter().brighter());
        }
        ArrayList<SingleProducer> producers = new ArrayList<SingleProducer>();
        JPanel producerPanel = new JPanel();
        
        /**
         * Add a producer to node.
         * Note this should be a working producer, 
         * already registered, etc
         */
         public void addProducer(SingleProducer n, String name) throws Exception {
            producers.add(n);
            buttons.add(new ProducerPane(name, n));
         }

        ArrayList<SingleConsumer> consumers = new ArrayList<SingleConsumer>();
        JPanel consumerPanel = new JPanel();
        
        /**
         * Add a consumer to node.
         * Note this should be a working consumer, 
         * already registered, etc
         */
         public void addConsumer(SingleConsumer n, String name) throws Exception {
            consumers.add(n);
            ConsumerPane cp = new ConsumerPane(name, n);
            buttons.add(cp);
         }
     }
     
}
