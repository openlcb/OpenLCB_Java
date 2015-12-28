package org.openlcb.can;

/**
 * Abstract connection on the CAN frame level. Receives calls from an external party for sending
 * CAN frames.
 *
 * Created by bracz on 12/23/15.
 */
public interface CanFrameListener {
    void send(CanFrame frame);
}
