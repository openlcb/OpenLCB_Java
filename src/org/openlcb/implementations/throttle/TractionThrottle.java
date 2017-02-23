package org.openlcb.implementations.throttle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.openlcb.Connection;
import org.openlcb.Message;
import org.openlcb.MessageDecoder;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.implementations.VersionedValue;
import org.openlcb.implementations.VersionedValueListener;
import org.openlcb.messages.TractionControlReplyMessage;
import org.openlcb.messages.TractionControlRequestMessage;

/**
 * Traction protocol based implementation of the throttle. This differs from
 * {@link org.openlcb.implementations.throttle.ThrottleImplementation} in that
 * it uses the {@link org.openlcb.messages.TractionControlRequestMessage} for
 * talking to the train nodes, including proper allocation and deallocation of
 * throttles.
 * <p>
 * Created by bracz on 12/30/15.
 */
public class TractionThrottle extends MessageDecoder {
    public static final int CONSIST_FLAG_REVERSE = TractionControlRequestMessage
            .CONSIST_FLAG_REVERSE;
    public static final int CONSIST_FLAG_FN0 = TractionControlRequestMessage
            .CONSIST_FLAG_FN0;
    public static final int CONSIST_FLAG_FNN = TractionControlRequestMessage
            .CONSIST_FLAG_FNN;

    public static final String UPDATE_PROP_ENABLED = "updateEnabled";
    public static final String UPDATE_PROP_STATUS = "updateStatus";
    public static final String UPDATE_PROP_CONSISTLIST = "updateConsistList";
    private final static Logger logger = Logger.getLogger(TractionThrottle.class.getName());
    private final OlcbInterface iface;
    RemoteTrainNode trainNode;
    boolean assigned = false;
    boolean enabled = false;
    String status;
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    private VersionedValue<Float> speed = new VersionedValue<>(0.0f);
    private VersionedValueListener<Float> speedUpdater = new VersionedValueListener<Float>(speed) {
        @Override
        public void update(Float t) {
            if (!enabled) return;

            Message m = TractionControlRequestMessage.createSetSpeed(iface.getNodeId(),
                    trainNode.getNodeId(), Math.copySign(1.0, t) >= 0, t);
            iface.getOutputConnection().put(m, TractionThrottle.this);

        }
    };
    private Map<Integer, FunctionInfo> functions = new HashMap<>();
    private boolean pendingAssign = false;
    private List<ConsistEntry> consistList = new ArrayList<>();
    private boolean needFetchConsist = false;

    public class ConsistEntry {
        ConsistEntry(NodeID n, int f) {
            node = n;
            flags = f;
        }
        public NodeID node;
        public int flags;
    }

    public TractionThrottle(OlcbInterface iface) {
        this.iface = iface;
    }

    public void start(RemoteTrainNode trainNode) {
        if (assigned && !this.trainNode.getNodeId().equals(trainNode.getNodeId())) {
            release();
        }
        this.trainNode = trainNode;
        assign();
    }

    public @Nullable
    NodeID getNodeId() {
        if (trainNode == null) return null;
        return trainNode.getNodeId();
    }

    public void refresh() {
        if (!getEnabled()) return;
        querySpeed();
        queryConsist();
        for (FunctionInfo f : functions.values()) {
            queryFunction(f.fn);
        }
    }

    /**
     * Releases the throttle from the assigned train node.
     */
    public void release() {
        if (!assigned) return;
        Message m = TractionControlRequestMessage.createReleaseController(iface.getNodeId(),
                trainNode.getNodeId());
        iface.getOutputConnection().put(m, this);
        assigned = false;
        setEnabled(false);
        iface.unRegisterMessageListener(this);
        setStatus("Released node.");
    }

    /**
     * @return the list of nodes in the consist managed by the assgined node. Entries may be
     * null in case the node list is still being fetched.
     */
    public List<ConsistEntry> getConsistList() { return consistList; }

    private void assign() {
        setStatus("Assigning node...");
        iface.registerMessageListener(this);
        pendingAssign = true;
        Message m = TractionControlRequestMessage.createAssignController(iface.getNodeId(),
                trainNode.getNodeId());
        iface.getOutputConnection().put(m, this);
    }

    private void assignComplete() {
        assigned = true;
        setStatus("Enabled.");
        setEnabled(true);
        // Refreshes functions and other settings after getting the definite promise from the node.
        refresh();
    }

    /**
     * Initiates fetching the current speed from the OpenLCB node. When the speed value arrives,
     * the property change listener will be called on the speed object.
     */
    public void querySpeed() {
        Message m = TractionControlRequestMessage.createGetSpeed(iface.getNodeId(), trainNode
                .getNodeId());
        iface.getOutputConnection().put(m, this);
    }

    /**
     * Initiates fetching consist information from the assigned node. Queryies the length of
     * consist and all consist members.
     */
    public void queryConsist() {
        consistList.clear();
        needFetchConsist = true;
        Message m = TractionControlRequestMessage.createConsistLengthQuery(iface.getNodeId(),
                trainNode.getNodeId());
        iface.getOutputConnection().put(m, this);
    }

    /**
     * Initiates fetching one consist member from the remote node.
     * @param index    index into the consist list (0 to consist member count - 1)
     */
    public void queryConsistMember(int index) {
        Message m = TractionControlRequestMessage.createConsistIndexQuery(iface.getNodeId(),
                trainNode.getNodeId(), index);
        iface.getOutputConnection().put(m, this);
    }

    /**
     * Adds a new node to the consist handled by the current assigned node, or updates an
     * existing node's consisting flags.
     * @param newMember    Node ID to add as a consist member
     * @param flags        bitmap of consist flags according to the standard (eg. whether forward or reverse, whether to send Fn buttons etc)
     */
    public void addToConsist(NodeID newMember, int flags) {
        Message m = TractionControlRequestMessage.createConsistAttach(iface.getNodeId(),
                trainNode.getNodeId(), newMember, flags);
        iface.getOutputConnection().put(m, this);
        m = TractionControlRequestMessage.createConsistAttach(iface.getNodeId(),
                newMember, trainNode.getNodeId(), flags);
        iface.getOutputConnection().put(m, this);
    }

    /** Removes a node from the consist handled by the current assigned node.
     * @param member    consist member to remove
     */
    public void removeFromConsist(NodeID member) {
        Message m = TractionControlRequestMessage.createConsistDetach(iface.getNodeId(),
                trainNode.getNodeId(), member);
        iface.getOutputConnection().put(m, this);
        m = TractionControlRequestMessage.createConsistDetach(iface.getNodeId(),
                member, trainNode.getNodeId());
        iface.getOutputConnection().put(m, this);
    }

    /**
     * Initiates fetching the current value of a given function from the OpenLCB node. When the
     * function value arrives, the property change listener will be called on the function object.
     *
     * @param fn the number of the function (address in OLCB land)
     */
    public void queryFunction(int fn) {
        Message m = TractionControlRequestMessage.createGetFn(iface.getNodeId(), trainNode
                .getNodeId(), fn);
        iface.getOutputConnection().put(m, this);
    }

    public VersionedValue<Boolean> getFunction(int fn) {
        return getFunctionInfo(fn).shared;
    }

    /**
     * Creates or returns the FunctionInfo structure for a given function number.
     * @param fn    OpenLCB funciton number to look up
     * @return FunctionInfo object that can be used to set or query the function value.
     */
    private synchronized FunctionInfo getFunctionInfo(int fn) {
        FunctionInfo v = functions.get(fn);
        if (v == null) {
            logger.warning("Creating function " + fn);
            v = new FunctionInfo(fn);
            functions.put(fn, v);
            if (!pendingAssign) {
                queryFunction(fn);
            }
        }
        return v;
    }

    public VersionedValue<Float> getSpeed() {
        return speed;
    }

    @Override
    public void handleTractionControlReply(TractionControlReplyMessage msg, Connection sender) {
        if (trainNode == null) return;
        if (!msg.getSourceNodeID().equals(trainNode.getNodeId())) return;
        if (!msg.getDestNodeID().equals(iface.getNodeId())) return;
        try {
            if (msg.getCmd() == TractionControlReplyMessage.CMD_CONTROLLER &&
                    msg.getSubCmd() == TractionControlReplyMessage.SUBCMD_CONTROLLER_ASSIGN) {
                byte result = msg.getAssignControllerReply();
                pendingAssign = false;
                if (result == 0) {
                    assignComplete();
                } else if ((result & 1) != 0) {
                    setStatus("Assigning controller failed: controller refused.");
                } else if ((result & 2) != 0) {
                    setStatus("Assigning controller failed: train node refused.");
                }
                return;
            }
            if (msg.getCmd() == TractionControlReplyMessage.CMD_GET_SPEED) {
                speedUpdater.setFromOwner(msg.getSetSpeed().getFloat());
                return;
            }
            if (msg.getCmd() == TractionControlReplyMessage.CMD_GET_FN) {
                int fn = msg.getFnNumber();
                int val = msg.getFnVal();
                logger.warning("Function response: train function " + fn + " value " + val);
                getFunctionInfo(fn).fnUpdater.setFromOwner(val != 0);
                return;
            }
            if (msg.getCmd() == TractionControlReplyMessage.CMD_CONSIST &&
                    msg.getSubCmd() == TractionControlReplyMessage.SUBCMD_CONSIST_QUERY) {
                int length = msg.getConsistLength();
                boolean fireChange = false;
                if (length != consistList.size() || needFetchConsist) {
                    consistList.clear();
                    fireChange = true;
                    for (int i = 0; i < length; ++i) {
                        consistList.add(null);
                        queryConsistMember(i);
                    }
                    needFetchConsist = false;
                }
                int index = msg.getConsistIndex();
                if (index >= 0) {
                    NodeID n = msg.getConsistQueryNodeID();
                    int flags = msg.getConsistQueryFlags();
                    consistList.set(index, new ConsistEntry(n, flags));
                    fireChange = true;
                }
                if (fireChange) {
                    firePropertyChange(UPDATE_PROP_CONSISTLIST, null, consistList);
                }
                return;
            }
            if (msg.getCmd() == TractionControlReplyMessage.CMD_CONSIST &&
                    msg.getSubCmd() == TractionControlReplyMessage.SUBCMD_CONSIST_ATTACH) {
                queryConsist();
                return;
            }
            if (msg.getCmd() == TractionControlReplyMessage.CMD_CONSIST &&
                    msg.getSubCmd() == TractionControlReplyMessage.SUBCMD_CONSIST_DETACH) {
                queryConsist();
                return;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Invalid message.
            logger.warning("Invalid traction message " +msg.toString());
            return;
        }
        logger.info("Unhandled traction message " +msg.toString());
    }

    public String getStatus() {
        return status;
    }

    private void setStatus(String status) {
        logger.warning("Throttle status: " + status);
        String oldStatus = this.status;
        this.status = status;
        firePropertyChange(UPDATE_PROP_STATUS, oldStatus, this.status);
    }

    public boolean getEnabled() {
        return enabled;
    }

    private void setEnabled(boolean enabled_) {
        boolean old = enabled;
        enabled = enabled_;
        firePropertyChange(UPDATE_PROP_ENABLED, old, enabled);
    }

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    private class FunctionInfo {
        int fn;
        VersionedValue<Boolean> shared = new VersionedValue<>(false);
        VersionedValueListener<Boolean> fnUpdater = new VersionedValueListener<Boolean>(shared) {
            @Override
            public void update(Boolean aBoolean) {
                if (!enabled) return;
                Message m = TractionControlRequestMessage.createSetFn(iface.getNodeId(),
                        trainNode.getNodeId(), fn, aBoolean ? 1 : 0);
                iface.getOutputConnection().put(m, TractionThrottle.this);
            }
        };

        public FunctionInfo(int num) {
            fn = num;
        }
    }

}
