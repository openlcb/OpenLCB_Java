// CdiMemConfigReader.java

package org.openlcb.cdi.jdom;

import java.util.ArrayList;
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
    ArrayList buf = new ArrayList();

    ReaderAccess retval;
    public void startLoadReader(ReaderAccess retval) {
        this.retval = retval;
        nextAddress = 0;
        buf = new ArrayList();
        nextRequest();
    }

    void nextRequest() {
        if (retval != null) {
            retval.progressNotify(buf.size(), -1);
        }
        MemoryConfigurationService.McsReadHandler memo =
            new MemoryConfigurationService.McsReadHandler() {

                int retryCount = 0;

                @Override
                public void handleFailure(int code) {
                    // code 0x1082 is "read of of bounds", which doesn't need a retry
                    if (code == 0x1082) {
                        done();
                        return;
                    }
                    // see if we can retry
                    retryCount++; // count from 1
                    if (retryCount > 3) {
                        // fail - don't do another request
                        logger.warning("Too many retries, ending");
                        done();
                    } else {
                        // retry this read request
                        logger.warning("Retrying after error reading CDI: " + Integer.toHexString(code));
                        service.requestRead(node, space, nextAddress, LENGTH, this);
                    }
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
                        buf.add(data[i]);
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
            retval.progressNotify(buf.size(), buf.size());
            byte[] byteArray = new byte[buf.size()];
            for (int i = 0 ; i<buf.size(); i++) {
                byteArray[i] = (byte)buf.get(i);
            }
            try {
                String xml = new String(byteArray, "UTF-8");
                logger.log(Level.FINE, "Retrieved XML: \n{0}", xml);
                retval.provideReader(new java.io.StringReader(xml));
            } catch (java.io.UnsupportedEncodingException e) {
                logger.warning("UnsupportedEncodingException while preparing XML data");
            }
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
