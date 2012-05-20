package scenarios;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.*;
import org.openlcb.cdi.swing.CdiPanel;

/**
 * Demonstrate basic configuration from an XML file.
 *
 * @author  Bob Jacobsen   Copyright 2011
 * @version $Revision$
 */
public class ConfigDemoApplet extends JApplet {
    
    public ConfigDemoApplet() {}
    
    /**
     * Applet starts here
     */
    @Override
    public void start() {
        JPanel p = new JPanel();
        add(p);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        startButton = new JButton("Start Configuration Demonstration");
        p.add(startButton);
        startButton.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            startDemo();
                       }
                    });     
        
    }
    
    JButton startButton;
    
    void startDemo() {
        startButton.setEnabled(false);
        
        JFrame f = new JFrame();
        f.setTitle("Configuration Demonstration");
        CdiPanel m = new CdiPanel();
                
        m.initComponents(new CdiPanel.ReadWriteAccess(){
            @Override
            public void doWrite(long address, int space, byte[] data) {
                    System.out.println(data.length);
                    System.out.println("write "+address+" "+space+": "+org.openlcb.Utilities.toHexDotsString(data));
                }
            @Override
            public void doRead(long address, int space, int length, CdiPanel.ReadReturn handler) {
                    handler.returnData(new byte[]{1,2,3,4,5,6,7,8});
                    System.out.println("read "+address+" "+space);
                }            
        });
        m.loadCDI(
            new org.openlcb.cdi.jdom.JdomCdiRep(
                org.openlcb.cdi.jdom.SampleFactory.getBasicSample()
            )
        );
        
        f.add( m );

        // show
        f.pack();
        f.setVisible(true);
    }
    

    // frame starting positions
    int hPos = 500;
    int vPos = 0;

    // load content
    void loadSample(JTextArea t) {
        // get content
        try {
            // Create a URL for the desired page
            URL url = new URL(this.getParameter("URL"));

            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                // str is one line of text; readLine() strips the newline character(s)
                t.append(str);
            }
            in.close();
            
        } catch (MalformedURLException e) {
            System.err.println("MalformedURLException: "+e);
        } catch (IOException e) {
            System.err.println("IOException: "+e);
        }
    }
    
    // For running as a JUnit test
    static public void runTest() {
        ConfigDemoApplet app = new ConfigDemoApplet();
        app.start();
        app.startDemo();
    }
    // Main entry point for standalone run
    static public void main(String[] args) {
        runTest();
    }

}
