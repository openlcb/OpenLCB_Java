package org.openlcb.can;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by bracz on 1/8/16.
 */
public class GridConnect {
    private static Logger logger = Logger.getLogger(new Object() {
    }.getClass().getSuperclass()
            .getName());

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

    public static List<CanFrame> parse(String data) {
        final List<CanFrame> l = new ArrayList<>();
        Input parser = new Input() {
            @Override
            public void onFrame(CanFrame f) {
                l.add(f);
            }
        };
        parser.send(data);
        return l;
    }

    enum InputState {
        NOPACKET,
        EXTENDED,
        HEADER,
        REMOTE,
        BODYHI_OR_END,
        BODYLO
    }

    public static abstract class Input {
        private boolean isExtended;
        private int header;
        private boolean isRtr;
        private InputState state = InputState.NOPACKET;
        private ArrayList<Byte> data = new ArrayList<>(8);

        private byte currData;

        public void send(String data) {
            for (int i = 0; i < data.length(); ++i) {
                send(data.charAt(i));
            }
        }

        public void send(char c) {
            while (true) {
                switch (state) {
                    case NOPACKET: {
                        if (c == ':') {
                            state = InputState.EXTENDED;
                        }
                        return;
                    }
                    case EXTENDED: {
                        if (c == 'X') {
                            isExtended = true;
                        } else if (c == 'S') {
                            isExtended = false;
                        } else {
                            // Unknown message type character.
                            logger.fine("Unknown gridconnect type character: " + c);
                            state = InputState.NOPACKET;
                            return;
                        }
                        state = InputState.HEADER;
                        header = 0;
                        return;
                    }
                    case HEADER: {
                        int dvalue = Character.digit(c, 16);
                        if (dvalue >= 0) {
                            header <<= 4;
                            header |= dvalue;
                            return;
                        } else {
                            state = InputState.REMOTE;
                            continue;
                        }
                    }
                    case REMOTE: {
                        if (c == 'N') {
                            isRtr = false;
                        } else if (c == 'R') {
                            isRtr = true;
                        } else {
                            // Unknown message format.
                            logger.fine("Unknown gridconnect remote character: " + c);
                            state = InputState.NOPACKET;
                            continue;
                        }
                        state = InputState.BODYHI_OR_END;
                        data.clear();
                        return;
                    }
                    case BODYHI_OR_END: {
                        if (c == ';') {
                            // End of frame.
                            onFrame(new RawCanFrame(isExtended, header, isRtr, data));
                            state = InputState.NOPACKET;
                            return;
                        }
                        int dvalue = Character.digit(c, 16);
                        if (dvalue < 0) {
                            logger.fine("Unknown gridconnect data character: " + c);
                            state = InputState.NOPACKET;
                            continue;
                        }
                        currData = (byte) (dvalue << 4);
                        state = InputState.BODYLO;
                        return;
                    }
                    case BODYLO: {
                        int dvalue = Character.digit(c, 16);
                        if (dvalue < 0) {
                            logger.fine("Unknown gridconnect data character: " + c);
                            state = InputState.NOPACKET;
                            continue;
                        }
                        currData |= (dvalue & 0xf);
                        data.add(currData);
                        state = InputState.BODYHI_OR_END;
                        return;
                    }
                }
            }
        }

        public abstract void onFrame(CanFrame f);
    }

    static class RawCanFrame implements CanFrame {

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
            return data[n];
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
