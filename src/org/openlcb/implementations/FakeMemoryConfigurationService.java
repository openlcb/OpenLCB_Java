package org.openlcb.implementations;

import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by bracz on 11/9/16.
 *
 * Test class for users of the MemoryConfigurationService. This is not in the test JAR so that
 * clients of the OpenLCB library can also test without the network.
 */

public class FakeMemoryConfigurationService extends MemoryConfigurationService {
    class SpaceKey {
        NodeID remoteNode;
        int space;

        SpaceKey() {}
        SpaceKey(NodeID nid, int sp) {
            remoteNode = nid;
            space = sp;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SpaceKey)) return false;
            SpaceKey p = (SpaceKey) o;
            return remoteNode.equals(p.remoteNode) && space == p.space;
        }

        @Override
        public int hashCode() {
            return remoteNode.hashCode() | space;
        }
    }

    class SpaceData {
        byte[] payload;
        boolean writeEnabled;
    }

    private Map<SpaceKey, SpaceData> knownSpaces = new HashMap<>();

    public FakeMemoryConfigurationService(OlcbInterface iface) {
        super(iface.getNodeId(), iface.getDatagramService());
        iface.injectMemoryConfigurationService(this);
    }

    public class ActualWrite {
        public int space;
        public long address;
        public byte[] data;
    }
    /// Records every write that happened through this fake.
    public List<ActualWrite> actualWriteList = new ArrayList<>();

    public class ActualRead {
        public int space;
        public long address;
        public int size;
    }
    /// Records every read that happened through this fake.
    public List<ActualRead> actualReadList = new ArrayList<>();

    public void addSpace(NodeID remoteNode, int space, byte[] payload, boolean writeEnabled) {
        SpaceKey k = new SpaceKey();
        k.remoteNode = remoteNode;
        k.space = space;
        SpaceData d = new SpaceData();
        d.payload = payload;
        d.writeEnabled = writeEnabled;
        knownSpaces.put(k, d);
    }

    private SpaceData findSpace(NodeID dest, int space) {
        return knownSpaces.get(new SpaceKey(dest, space));
    }

    @Override
    public void requestWrite(NodeID dest, int space, long address, byte[] data, McsWriteHandler
            cb) {
        // Makes a record of what is happening.
        ActualWrite aw = new ActualWrite();
        aw.space = space;
        aw.address = address;
        aw.data = data;
        actualWriteList.add(aw);

        SpaceData d = findSpace(dest, space);
        if (d == null || d.payload == null) {
            cb.handleFailure(0x1000);
            return;
        }
        if (d.payload.length < address) {
            cb.handleFailure(0x1001); // TODO: use proper error code
            return;
        }
        if (!d.writeEnabled) {
            cb.handleFailure(0x1002); // TODO: use proper error code
            return;
        }
        if (d.payload.length < (address + data.length)) {
            cb.handleFailure(0x1003); // TODO: use proper error code
            return;
        }
        System.arraycopy(data, 0, d.payload, (int)address, data.length);
        cb.handleSuccess();
    }

    @Override
    public void requestRead(NodeID dest, int space, long address, int len, McsReadHandler cb) {
        // Makes a record of what is happening.
        ActualRead ar = new ActualRead();
        ar.space = space;
        ar.address = address;
        ar.size = len;
        actualReadList.add(ar);

        SpaceData d = findSpace(dest, space);
        if (d == null || d.payload == null) {
            cb.handleFailure(0x1000);
            return;
        }
        if (address >= d.payload.length) {
            logger.warning("Reading from space " + space + " address " + address +
                    " but payload max is " + d.payload.length);
            cb.handleFailure(0x1001); // TODO: use proper error code
            return;
        }
        int count = len;
        if (address+count > d.payload.length) {
            count = d.payload.length - (int)address;
        }
        byte[] ret = new byte[count];
        System.arraycopy(d.payload, (int) address, ret, 0, count);
        cb.handleReadData(dest, space, address, ret);
    }

    private static final Logger logger = Logger.getLogger(FakeMemoryConfigurationService.class.getName());

}
