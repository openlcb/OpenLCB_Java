package org.openlcb.cdi.cmd;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openlcb.NodeID;
import org.openlcb.PropertyListenerSupport;
import org.openlcb.can.impl.OlcbConnection;

/**
 * Created by bracz on 4/9/16.
 */
public class Util {
    
    private final static Logger logger = Logger.getLogger(Util.class.getName());
    
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
            logger.log(Level.SEVERE, "Interrupted while waiting for property notification {0} on object of class {1}", new Object[]{propertyName, tgt.getClass().getName()});
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
                logger.log(Level.FINE, "Connected to {0}:{1}", new Object[]{host, port});
                s.set(true);
            }

            @Override
            public void onDisconnect() {
                logger.fine("Disconnected.");
                s.set(false);
            }

            @Override
            public void onStatusChange(String status) {
                logger.fine(status);
            }

            @Override
            public void onConnectionPending() {
                logger.fine("Connecting.");
            }
        };

        OlcbConnection connection = new OlcbConnection(localNode, host, port, l);
        connection.startConnect();
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
            if (codePoint == "\\".codePointAt(0)) {
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

    static public String unescapeString(String input) {
        StringBuffer o = new StringBuffer(input.length());
        int pos = 0;
        while (pos < input.length()) {
            if (input.charAt(pos) == '\\' && (pos + 5) < input.length() && input.charAt(pos+1)
                    == 'x') {
                int codePoint = Integer.parseInt(input.substring(pos+2, pos+6), 16);
                o.append(Character.toChars(codePoint));
                pos += 6;
            } else {
                o.append(input.charAt(pos));
                ++pos;
            }
        }
        return o.toString();
    }
}
