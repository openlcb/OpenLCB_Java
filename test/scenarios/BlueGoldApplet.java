package scenarios;

import org.openlcb.*;
import org.openlcb.implementations.*;
import org.openlcb.swing.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Simulate 6 nodes interacting on a single gather/scatter
 * for testing blue/gold programming.
 *
 * Applet for web demonstration of BlueGold.
 * Largely a duplicate of BlueGoldCheck app,
 * modified for single-frame applet use.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class BlueGoldApplet extends JApplet {
    
    public BlueGoldApplet() {}
    
    /**
     * Applet starts here
     */
    public void start() {
        JPanel p = new JPanel();
        add(p);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        startButton = new JButton("Blue-Gold algorithm demonstration");
        p.add(startButton);
        startButton.addActionListener(
                    new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            startDemo();
                       }
                    });     
        
    }
    
    JButton startButton;
    
    void startDemo() {
        startButton.setEnabled(false);
        
        sg = new ScatterGather();

        // show a monitor frame
        JFrame f = new JFrame();
        f.setTitle("Blue-Gold Check");
        MonPane m = new MonPane();
        f.add( m );
        m.initComponents();
        f.pack();
        f.setVisible(true);
        sg.register(m.getConnection());

        createSampleNode(1);
        createSampleNode(2);
        createSampleNode(3);
        createSampleNode(4);
    }
    
    ScatterGather sg;

    void createSampleNode(int index) {
        NodeID id;
        SingleProducer producer11;
        SingleProducer producer12;
        SingleProducer producer13;
        SingleConsumer consumer11;
        SingleConsumer consumer12;
        SingleConsumer consumer13;

        id = new NodeID(new byte[]{0,0,0,0,0,(byte)index});
        
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
                ScatterGather sg) {
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
            this.nid = nid;
            
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
            
            engine = new BlueGoldExtendedEngine(nid, sg, producers, consumers) {
                public void setBlueLightOn(boolean f) {
                    setBlueOn(f);
                }
                public boolean getBlueLightOn() {
                    return blueOn;
                }
                
                public void setBlueLightBlink(int dwell) {
                    setBlueBlink(dwell);
                }

                public void setGoldLightOn(boolean f) {
                    setGoldOn(f);
                }
                public boolean getGoldLightOn() {
                    return goldOn;
                }

                public void setGoldLightBlink(int dwell) {
                    setGoldBlink(dwell);
                }

            };
            
            sg.register(engine);
            
            blueButton.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mousePressed(java.awt.event.MouseEvent e) {
                    blueTime = System.currentTimeMillis();
                }
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    if (System.currentTimeMillis()-blueTime < 2000)
                        engine.blueClick();
                    else
                        longBluePress();
                }
            });
            
            goldButton.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mousePressed(java.awt.event.MouseEvent e) {
                    goldTime = System.currentTimeMillis();
                }
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    if (System.currentTimeMillis()-goldTime < 2000)
                        engine.goldClick();
                    else
                        longGoldPress();
                }
            });
        }
        
        NodeID nid;
        
        /**
         * Handle a long (greater than 2 second) press on
         * the blue button
         */
        void longBluePress() {
            // reset selections
            System.out.println("reset selections");
            engine.longBluePress();
        }
        
        /**
         * Handle a long (greater than 2 second) press on
         * the gold button
         */
        void longGoldPress() {
            // reset the device
            System.out.println("reset device");
            producers.get(0).setEventID(new EventID(nid, 1, 1));
            producers.get(1).setEventID(new EventID(nid, 1, 2));
            producers.get(2).setEventID(new EventID(nid, 1, 3));
            consumers.get(0).setEventID(new EventID(nid, 0, 1));
            consumers.get(1).setEventID(new EventID(nid, 0, 2));
            consumers.get(2).setEventID(new EventID(nid, 0, 3));            
        }
        
        long blueTime;
        long goldTime;
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
        java.util.List<SingleProducer> producers = new ArrayList<SingleProducer>();
        JPanel producerPanel = new JPanel();
        
        /**
         * Add a producer to node.
         * Note this should be a working producer, 
         * already registered, etc
         */
         public void addProducer(SingleProducer n, String name) {
            producers.add(n);
            buttons.add(new ProducerPane(name, n));
         }

        java.util.List<SingleConsumer> consumers = new ArrayList<SingleConsumer>();
        JPanel consumerPanel = new JPanel();
        
        /**
         * Add a consumer to node.
         * Note this should be a working consumer, 
         * already registered, etc
         */
         public void addConsumer(SingleConsumer n, String name) {
            consumers.add(n);
            ConsumerPane cp = new ConsumerPane(name, n);
            buttons.add(cp);
         }
     }
     
}
