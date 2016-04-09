package org.openlcb.cdi.cmd;

import org.openlcb.NodeID;
import org.openlcb.PropertyListenerSupport;
import org.openlcb.Utilities;
import org.openlcb.can.impl.OlcbConnection;
import org.openlcb.cdi.impl.ConfigRepresentation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by bracz on 4/9/16.
 */
public class SaveConfig {


    static void waitForPropertyChange(PropertyListenerSupport tgt, final String propertyName) {
        final Object o = new Object();
        PropertyChangeListener l = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (propertyChangeEvent.getPropertyName().equals(propertyName)) {
                    synchronized(o) {
                        o.notify();
                    }
                }
            }
        };
        tgt.addPropertyChangeListener(l);
        try {
            synchronized(o) {
                o.wait();
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted while waiting for property notification " +
                    propertyName + " on object of class " + tgt.getClass().getName());
        }
        tgt.removePropertyChangeListener(l);
    }

    static public OlcbConnection connect(final NodeID localNode, final String host, final int
            port) {
        class Success {
            private boolean ok = false;

            public synchronized void set(boolean isOk) {
                ok = isOk;
                this.notify();
            }

            public synchronized boolean get() {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return ok;
            }
        }
        final Success s = new Success();

        OlcbConnection.ConnectionListener l = new OlcbConnection
                .ConnectionListener() {
            @Override
            public void onConnect() {
                System.out.println("Connected to " + host + ":" + port);
                s.set(true);
            }

            @Override
            public void onDisconnect() {
                System.out.println("Disconnected.");
                s.set(false);
            }

            @Override
            public void onStatusChange(String status) {
                System.out.println(status);
            }

            @Override
            public void onConnectionPending() {
                System.out.println("Connecting.");
            }
        };

        OlcbConnection connection = new OlcbConnection(localNode, host, port, l);
        if (s.get()) {
            return connection;
        } else {
            System.exit(1);
            return null;
        }
    }

    static public String escapeString(String myString) {
        StringBuilder newString = new StringBuilder(myString.length() + 5);
        for (int offset = 0; offset < myString.length();)
        {
            int codePoint = myString.codePointAt(offset);
            offset += Character.charCount(codePoint);
            boolean mustEscape = false;
            if (codePoint == "=".codePointAt(0)) {
                mustEscape = true;
            }
            // Replace invisible control characters and unused code points
            switch (Character.getType(codePoint))
            {
                case Character.CONTROL:     // \p{Cc}
                case Character.FORMAT:      // \p{Cf}
                case Character.PRIVATE_USE: // \p{Co}
                case Character.SURROGATE:   // \p{Cs}
                case Character.UNASSIGNED:  // \p{Cn}
                    newString.append("\\x");
                    newString.append(String.format("%04x", codePoint));
                    break;
                default:
                    if (mustEscape) {
                        newString.append("\\x");
                        newString.append(String.format("%04x", codePoint));
                    } else {
                        newString.append(Character.toChars(codePoint));
                    }
                    break;
            }
        }
        return newString.toString();
    }

    static public void writeEntry(BufferedWriter outFile, String key, String value) {
        try {
            outFile.write(escapeString(key));
            outFile.write('=');
            outFile.write(escapeString(value));
            outFile.write('\n');
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(1);
        }
    }

    // Main entry point
    static public void main(String[] args) {
        if (args.length != 5) {
            usage();
            return;
        }
        System.out.println("arg0: " + args[0]);
        NodeID localNode = new NodeID(args[0]);
        final String host = args[1];
        final int port = Integer.parseInt(args[2]);
        final NodeID remoteNode = new NodeID(args[3]);
        final String dstFile = args[4];

        final OlcbConnection connection = connect(localNode, host, port);
        System.out.println("Fetching CDI.");
        ConfigRepresentation repr = connection.getConfigForNode(remoteNode);
        waitForPropertyChange(repr, ConfigRepresentation.UPDATE_REP);
        System.out.println("CDI fetch done. Waiting for caches.");
        waitForPropertyChange(repr, ConfigRepresentation.UPDATE_CACHE_COMPLETE);
        System.out.println("Caches complete.");
        BufferedWriter outFile = null;

        try {
            outFile = Files.newBufferedWriter(Paths.get(dstFile), Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.err.println("Failed to create output file: " + e.toString());
            System.exit(1);
        }
        final BufferedWriter finalOutFile = outFile;
        System.out.println("Writing variables.");
        repr.visit(new ConfigRepresentation.Visitor() {
                       @Override
                       public void visitString(ConfigRepresentation.StringEntry e) {
                           writeEntry(finalOutFile, e.key, e.getValue());
                       }

                       @Override
                       public void visitInt(ConfigRepresentation.IntegerEntry e) {
                           writeEntry(finalOutFile, e.key, Long.toString(e.getValue()));
                       }

                       @Override
                       public void visitEvent(ConfigRepresentation.EventEntry e) {
                           writeEntry(finalOutFile, e.key, Utilities.toHexDotsString(e.getValue
                                   ().getContents()));
                       }
                   }
        );
        try {
            outFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Done.");
        System.exit(0);
    }

    private static void usage() {
        String usageString = "usage: saveconfig local_node_id hub_host hub_port dst_node_id " +
                "dst_filename\n";
        System.err.print(usageString);
    }

}
