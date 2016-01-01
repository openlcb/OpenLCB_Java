package org.openlcb;

/**
 * Created by bracz on 12/29/15.
 */
public class FakeOlcbInterface extends OlcbInterface {
    public FakeConnection fakeOutputConnection;


    /**
     * Creates a testing interface.
     */
    public FakeOlcbInterface() {
        super(new NodeID(new byte[]{1,2,0,0,1,1}), new FakeConnection());
        fakeOutputConnection = (FakeConnection) outputConnection;
    }
}
