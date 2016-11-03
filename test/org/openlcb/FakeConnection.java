package org.openlcb;

import org.openlcb.AbstractConnection;
import org.openlcb.Connection;
import org.openlcb.Message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by bracz on 12/29/15.
 *
 * A Connection class that implements registerStartNotification and forwards all messages to a
 * mock connection passed in at construction time.
 */
public class FakeConnection extends AbstractConnection {
    private final Connection mock;

    public FakeConnection(Connection mock) {
        this.mock = mock;
    }

    @Override
    public synchronized void put(Message msg, Connection sender) {
        mock.put(msg, sender);
    }
}
