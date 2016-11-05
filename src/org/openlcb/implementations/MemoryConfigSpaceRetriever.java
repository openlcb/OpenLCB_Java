package org.openlcb.implementations;

import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;

/**
 * Downloads the entire contents of a Memory Config space. Useful for CDI-style situations.
 * <p>
 * Created by bracz on 1/17/16.
 */
public class MemoryConfigSpaceRetriever {
    final static int LENGTH = 64;

    final NodeID node;
    final MemoryConfigurationService service;
    final int space;
    final Callback cb;
    long nextAddress = 0;
    StringBuffer buf;

    public MemoryConfigSpaceRetriever(NodeID node, OlcbInterface iface, int space, Callback cb) {
        this.node = node;
        this.service = iface.getMemoryConfigurationService();
        this.space = space;
        this.cb = cb;
        nextAddress = 0;
        buf = new StringBuffer();
        nextRequest();
    }

    void nextRequest() {
        MemoryConfigurationService.McsReadHandler memo =
                new MemoryConfigurationService.McsReadHandler() {
                    @Override
                    public void handleFailure(int code) {
                        cb.onFailure(code);
                    }

                    public void handleReadData(NodeID dest, int space, long address, byte[] data) {
                        // handle return data, checking for null in string or zero-length reply
                        if (data.length == 0) {
                            done();
                            return;  // don't do next request
                        }
                        for (int i = 0; i < data.length; i++) {
                            if (data[i] == 0) {
                                done();
                                return;  // don't do next request
                            }
                            buf.append((char) data[i]);
                        }
                        // repeat if not done
                        nextAddress = nextAddress + LENGTH;
                        nextRequest();
                    }
                };
        service.requestRead(node, space, nextAddress, LENGTH, memo);
    }

    private void done() {
        cb.onSuccess(buf.toString());
    }

    public interface Callback {
        void onSuccess(String contents);

        void onFailure(int code);
    }
}
