package org.openlcb.can.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.openlcb.can.CanFrame;
import org.openlcb.can.CanFrameListener;
import org.openlcb.implementations.DatagramUtils;

/**
 * Parses an input stream according to the GridConnect protocol and forwards a set of CAN frames.
 * <p>
 * Created by bracz on 12/23/15.
 */
public class GridConnectInput {
    private final static Logger logger = Logger.getLogger(GridConnectInput.class.getName());
    private boolean isExtended;
    private int header;
    private boolean isRtr;
    private ArrayList<Byte> data = new ArrayList<>();
    private BufferedReader input;
    private CanFrameListener listener;
    private final Runnable onError;

    /**
     * Creates the gridconnect input parser. Starts the parsing thread.
     *
     * @param input the (buffered) socklet to read from
     * @param listener the parsed CAN frames will be forwarded to this listener
     * @param onError will be called when an IO error happens on the input thread. May be null.
     */
    public GridConnectInput(BufferedReader input, CanFrameListener listener, Runnable onError) {
        this.input = input;
        this.listener = listener;
        this.onError = onError;
        new Thread("openlcb-gc-input") {
            public void run() {
                threadBody();
            }
        }.start();
    }

    private void threadBody() {
        try {
            while (true) {
                // assumption: we are at the end of a frame now.
                while (input.read() != ':') {
                }
                int typeChar = input.read();
                if (typeChar == 'X') {
                    isExtended = true;
                } else if (typeChar == 'S') {
                    isExtended = false;
                } else {
                    // Unknown message type character.
                    logger.fine("Unknown gridconnect type character: " + typeChar);
                }
                header = 0;
                int c;
                while (true) {
                    input.mark(2);
                    c = input.read();
                    int dvalue = Character.digit(c, 16);
                    if (dvalue >= 0) {
                        header <<= 4;
                        header |= dvalue;
                    } else {
                        break;
                    }
                }
                if (c == 'N') {
                    isRtr = false;
                } else if (c == 'R') {
                    isRtr = true;
                } else {
                    // Unknown message format.
                    logger.fine("Unknown gridconnect remote character: " + c);
                    input.reset();
                    continue;
                }
                data.clear();
                while (true) {
                    input.mark(2);
                    byte value = 0;
                    c = input.read();
                    int dvalue = Character.digit(c, 16);
                    if (dvalue < 0) {
                        break;
                    }
                    value = (byte) (dvalue << 4);
                    input.mark(2);
                    c = input.read();
                    dvalue = Character.digit(c, 16);
                    if (dvalue < 0) {
                        break;
                    }
                    value |= (dvalue & 0xf);
                    data.add(value);
                }
                if (c == ';') {
                    CanFrame f = new RawCanFrame(isExtended, header, isRtr, data);
                    listener.send(f);
                } else {
                    logger.fine("unexpected gridconnect payload character: " + c);
                    input.reset();
                    continue;
                }
            }
        } catch (IOException e) {
            logger.info("Error reading from gridconnect port " + e.toString());
            try {
                input.close();
            } catch (IOException e1) {
                logger.fine("Error closing from gridconnect port " + e1.toString());
            }
            if (onError != null) {
                onError.run();
            }
        }
    }

    class RawCanFrame implements CanFrame {

        private boolean isExtended;
        private boolean isRtr;
        private int header;
        private int len;
        private byte[] data;

        public RawCanFrame(boolean isExtended, int header, boolean isRtr, ArrayList<Byte> data) {
            this.isExtended = isExtended;
            this.header = header;
            this.isRtr = isRtr;
            this.len = data.size();
            this.data = new byte[this.len];
            for (int i = 0; i < len; ++i) {
                this.data[i] = data.get(i);
            }
        }

        @Override
        public int getHeader() {
            return header;
        }

        @Override
        public boolean isExtended() {
            return this.isExtended;
        }

        @Override
        public boolean isRtr() {
            return this.isRtr;
        }

        @Override
        public int getNumDataElements() {
            return len;
        }

        @Override
        public int getElement(int n) {
            return DatagramUtils.byteToInt(data[n]);
        }

        @Override
        public long bodyAsLong() {
            long retval = 0;
            for (int i = 0; i < data.length; i++) {
                retval = retval << 8 | (data[i] & 0xFF);
            }
            return retval;
        }

        @Override
        public long dataAsLong() {
            long retval = 0;
            for (int i = 2; i < data.length; i++) {
                retval = retval << 8 | (data[i] & 0xFF);
            }
            return retval;
        }

        @Override
        public byte[] getData() {
            byte[] b = new byte[len];
            System.arraycopy(data, 0, b, 0, len);
            return b;
        }
    }
}
