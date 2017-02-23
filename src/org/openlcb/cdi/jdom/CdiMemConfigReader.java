// CdiMemConfigReader.java

package org.openlcb.cdi.jdom;

import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import org.openlcb.*;
import org.openlcb.implementations.*;

/**
 * Provide a Reader to the OpenLCB CDI in a node.
 *
 * This first implementation reads the entire data before providing the Reader
 * by call back.
 *
 * @author	Bob Jacobsen   Copyright (C) 2012
 * @version	$Revision$
 */
public class CdiMemConfigReader  {
    private final static Logger logger = getLogger(CdiMemConfigReader.class.getName());

    final static int LENGTH = 64;

    NodeID node;
    MimicNodeStore store;
    MemoryConfigurationService service;
    final int space;
        
    public CdiMemConfigReader(NodeID node, MimicNodeStore store, MemoryConfigurationService service) {
        this.node = node;
        this.store = store;
        this.service = service;
        this.space = MemoryConfigurationService.SPACE_CDI;
    }

    public CdiMemConfigReader(NodeID node, OlcbInterface iface, int space) {
        this.node = node;
        this.store = iface.getNodeStore();
        this.service = iface.getMemoryConfigurationService();
        this.space = space;
    }


    long nextAddress = 0;
    StringBuffer buf;
    
    ReaderAccess retval;
    public void startLoadReader(ReaderAccess retval) {
        this.retval = retval;
        nextAddress = 0;
        buf = new StringBuffer();
        nextRequest();
    }
    
    void nextRequest() {
        if (retval != null) {
            retval.progressNotify(buf.length(), -1);
        }
        MemoryConfigurationService.McsReadHandler memo =
            new MemoryConfigurationService.McsReadHandler() {
                @Override
                public void handleFailure(int code) {
                    logger.warning("Error reading CDI: " + Integer.toHexString(code));
                    done();
                    // TODO: 5/2/16 proxy error messages to the caller.
                    // don't do next request
                }

                public void handleReadData(NodeID dest, int space, long address, byte[] data) {
                    // handle return data, checking for null in string or zero-length reply
                    if (data.length == 0) {
                        done();
                        return;  // don't do next request
                    }
                    for (int i = 0; i<data.length; i++) {
                        if (data[i] == 0) {
                            done();
                            return;  // don't do next request
                        }
                        buf.append((char)data[i]);
                    }
                    // repeat if not done
                    nextAddress = nextAddress + LENGTH;
                    nextRequest();
                }
            };
        service.requestRead(node, space, nextAddress, LENGTH, memo);
    }
    
    private void done() {
        // done, pass back a reader based on the current buffer contents
        if (retval != null) {
            retval.progressNotify(buf.length(), buf.length());
            logger.log(Level.FINE, "Retrieved XML: \n{0}", buf);
            retval.provideReader(new java.io.StringReader(new String(buf)));
        }
    }
    
    public interface ReaderAccess {
        /**
         *
         * @param bytesRead how many bytes we have fetched so far from the server
         * @param totalBytes the total number of bytes to read, or -1 if not known
         */
        public void progressNotify(long bytesRead, long totalBytes);
        public void provideReader(java.io.Reader r);
    }
}
