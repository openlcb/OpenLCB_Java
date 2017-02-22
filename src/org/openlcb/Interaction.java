package org.openlcb;

/**
 * Created by bracz on 12/7/16.
 */
public abstract class Interaction {

    /**
     * Timeout after each sendRequest for the interaction to be completed before
     * the onTimeout is called.
     */
    int deadlineMsec = 700;

    /**
     * Set to true by the system when a cancel/complete call arrives for this
     * interaction.
     */
    boolean isComplete = false;

    /**
     * Called by the system when it is time to send this interaction.
     *
     * @param downstream the connection over which to send the request
     */
    abstract void sendRequest(Connection downstream);

    /**
     * @return the destination node to which this interaction is enqueued.
     */
    abstract NodeID dstNode();

    /**
     * Called by the system if there is no answer from the destination node
     * within the timeout.
     */
    abstract void onTimeout();
}
