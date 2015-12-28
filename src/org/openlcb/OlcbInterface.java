package org.openlcb;

/**
 * Collects all objects necessary to run an OpenLCB standards-compatible interface.
 * <p/>
 * Created by bracz on 12/27/15.
 */
public class OlcbInterface {

    /// Object for sending messages to the network.
    private final Connection outputConnection;
    Connection inputConnection;

    public OlcbInterface(Connection outputConnection) {
        this.outputConnection = outputConnection;

    }

    public Connection inputConnection() {
        return inputConnection;
    }

    class MessageDispatcher implements Connection {
        @Override
        public void put(Message msg, Connection sender) {

        }

        @Override
        public void registerStartNotification(ConnectionListener c) {

        }
    }

}
