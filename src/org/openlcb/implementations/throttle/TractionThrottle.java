package org.openlcb.implementations.throttle;

import org.openlcb.Connection;
import org.openlcb.Message;
import org.openlcb.MessageDecoder;
import org.openlcb.OlcbInterface;
import org.openlcb.implementations.VersionedValue;
import org.openlcb.implementations.VersionedValueListener;
import org.openlcb.messages.TractionControlReplyMessage;
import org.openlcb.messages.TractionControlRequestMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Traction protocol based implementation of the throttle. This differs from {@ref
 * ThrottleImplementation} in that it uses the {@ref TractionControlRequest} messages for talking
 * to the train nodes, including proper allocation and deallocation of throttles.
 * <p/>
 * Created by bracz on 12/30/15.
 */
public class TractionThrottle extends MessageDecoder {
    public static final String UPDATE_PROP_ENABLED = "updateEnabled";
    public static final String UPDATE_PROP_STATUS = "updateStatus";
    private static Logger logger = Logger.getLogger(new Object() {
    }.getClass().getSuperclass()
            .getName());
    private final OlcbInterface iface;
    RemoteTrainNode trainNode;
    boolean assigned = false;
    boolean enabled = false;
    String status;
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    private VersionedValue<Float> speed;
    private VersionedValueListener<Float> speedUpdater = new VersionedValueListener<Float>(speed) {
        @Override
        public void update(Float t) {
            if (!enabled) return;

            Message m = TractionControlRequestMessage.createSetSpeed(iface.getNodeId(),
                    trainNode.getNodeId(), t >= 0, t);
            iface.getOutputConnection().put(m, TractionThrottle.this);

        }
    };
    private Map<Integer, FunctionInfo> functions = new HashMap<>();

    public TractionThrottle(OlcbInterface iface) {
        this.iface = iface;
    }

    public void start(RemoteTrainNode trainNode) {
        if (assigned && !trainNode.getNodeId().equals(trainNode.getNodeId())) {
            release();
        }
        this.trainNode = trainNode;
        assign();
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
        setStatus("Released node.");
    }

    private void assign() {
        setStatus("Assigning node...");
        Message m = TractionControlRequestMessage.createAssignController(iface.getNodeId(),
                trainNode.getNodeId());
        iface.getOutputConnection().put(m, this);
    }

    private void assignComplete() {
        assigned = true;
        setStatus("Enabled.");
        setEnabled(true);
        querySpeed();
        // Ensures that whenever the function objects are requested they will be queriedfrom the
        // backend anew.
        functions.clear();
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
     */
    private synchronized FunctionInfo getFunctionInfo(int fn) {
        FunctionInfo v = functions.get(fn);
        if (v == null) {
            v = new FunctionInfo(fn);
            functions.put(fn, v);
            queryFunction(fn);
        }
        return v;
    }

    public VersionedValue<Float> getSpeed() {
        return speed;
    }

    private void internalSetSpeed(Float16 newSpeed) {

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
                getFunctionInfo(msg.getFnNumber()).fnUpdater.setFromOwner(msg.getFnVal() != 0);
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
        VersionedValue<Boolean> shared;
        VersionedValueListener<Boolean> fnUpdater = new VersionedValueListener<Boolean>(shared) {
            @Override
            public void update(Boolean aBoolean) {
                if (!enabled) return;
                Message m = TractionControlRequestMessage.createSetFn(iface.getNodeId(),
                        trainNode.getNodeId(), fn, aBoolean.booleanValue() ? 1 : 0);
                iface.getOutputConnection().put(m, TractionThrottle.this);
            }
        };

        public FunctionInfo(int num) {
            fn = num;
        }
    }

}
