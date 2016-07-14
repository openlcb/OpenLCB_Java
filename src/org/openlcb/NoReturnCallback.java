package org.openlcb;

/**
 * Represents the callback for an operation that returns nothing. THis will be shared by a number
 * of different services.
 *
 * Created by bracz on 5/2/16.
 */
public interface NoReturnCallback extends FailureCallback {
    /**
     * Called when the requested operation completes successfully.
     */
    void handleSuccess();
}
