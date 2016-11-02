package org.openlcb;

import static org.mockito.Mockito.*;

/**
 * Created by bracz on 12/29/15.
 */
public class FakeOlcbInterface extends OlcbInterface {
    public Connection mockOutputConnection;

    /**
     * Creates a testing interface.
     */
    public FakeOlcbInterface() {
        super(new NodeID(new byte[]{1,2,0,0,1,1}), mock(Connection.class));
        mockOutputConnection = internalOutputConnection;
    }
}
