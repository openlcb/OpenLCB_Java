package org.openlcb;

import org.openlcb.AbstractConnection;
import org.openlcb.Connection;
import org.openlcb.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bracz on 12/29/15.
 */
public class FakeConnection extends AbstractConnection {
    public List<Message> history = new ArrayList<>();

    @Override
    public void put(Message msg, Connection sender) {
        history.add(msg);
    }
}
