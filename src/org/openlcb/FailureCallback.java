package org.openlcb;

/**
 * Created by bracz on 5/2/16.
 */
public interface FailureCallback {
    /**
     * Called when the requested operation encounters an error. Temporary errors are usually
     * internally re-tried.
     * @param errorCode OpenLCB error code (16-bit)
     */
    void handleFailure(int errorCode);
}
