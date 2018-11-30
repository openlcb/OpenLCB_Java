package org.openlcb.can.impl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;
import org.openlcb.can.CanFrame;
import org.openlcb.can.CanFrameListener;

/**
 * Converts the sent CAN framesto gridconnect protocol and writes them ot an output stream.
 * Performs internal buffering.
 * <p>
 * Created by bracz on 12/23/15.
 */
public class GridConnectOutput implements CanFrameListener {
    private final static Logger logger = Logger.getLogger(GridConnectOutput.class.getName());

    private BufferedOutputStream output;
    private final Runnable onError;

    /**
     * Creates the object ussed for rendering CAN frames to GridConnect format.
     * @param output the (raw) output socket to send the gridconnect data to.
     * @param onError will be called when the output experiences an IO error. May be null.
     */
    public GridConnectOutput(OutputStream output, Runnable onError) {
        this.output = new BufferedOutputStream(output);
        this.onError = onError;
    }

    public static String format(CanFrame frame) {
        StringBuilder b = new StringBuilder();
        if (frame.isExtended()) {
            b.append(String.format(":X%08X", frame.getHeader()));
        } else {
            b.append(String.format(":S%03X", frame.getHeader()));
        }
        if (frame.isRtr()) {
            b.append('R');
        } else {
            b.append('N');
        }
        if (frame.getNumDataElements() > 8) {
            logger.warning("Output frame with too many data elements: " + Integer.toString(frame
                    .getNumDataElements()));
        }
        for (int i = 0; i < frame.getNumDataElements(); ++i) {
            b.append(String.format("%02X", frame.getElement(i) & 0xff));
        }
        b.append(';');
        return b.toString();
    }

    @Override
    public synchronized void send(CanFrame frame) {
        try {
            output.write(format(frame).getBytes("ISO-8859-1"));
            output.write('\n');
            output.flush();
        } catch (IOException e) {
            logger.warning("Error writing to gridconnect output: " + e.toString());
            try {
                output.close();
            } catch (IOException e1) {
                logger.fine("Error closing gridconnect output: " + e1.toString());
            }
            if (onError != null) {
                onError.run();
            }
        }
    }

}
