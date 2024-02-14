package org.openlcb.can;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.openlcb.NodeID;

/**
 * Implementation of Node ID Alias assignment computation.
 * Provides and processes frames, but other code must move them
 * to and from the actual interface.
 * It also requires subclassing to provide a timer function.
 *
 * @author  Bob Jacobsen   Copyright 2009, 2010, 2023
 */
public class NIDaAlgorithm implements CanFrameListener {
    /// Callback to invoke when the alias was successfully reserved.
    private Runnable done;
    private CanFrameListener sendInterface;
    private static Timer timer;
    private TimerTask task;
    private final static Logger logger = Logger.getLogger(NIDaAlgorithm.class.getName());

    private synchronized void scheduleTimer(long delay) {
        task = new TimerTask() {
            @Override
            public void run() {
                timerExpired();
            }
        };

        try {
           timer.schedule(task, delay);
        } catch (IllegalStateException | NullPointerException ise){
           // the timer was canceled, dispose occurred before the task was scheduled.
        }
    }

    public NIDaAlgorithm(NodeID n) {
        nid = n;
        nida = new NIDa(n);
    }

    public NIDaAlgorithm(NodeID n, CanFrameListener sendInterface) {
        this(n);

        this.sendInterface = sendInterface;

        synchronized(NIDaAlgorithm.class) {
            if (timer == null ) {
                timer = new Timer("OpenLCB NIDaAlgorithm Timer");
            }
        }
    }

    public void start(Runnable done) {
        this.done = done;
        scheduleTimer(100);
    }

    public OpenLcbCanFrame nextFrame() {
        OpenLcbCanFrame f;
        if (index < 4) {
            f = new OpenLcbCanFrame(nida.getNIDa());
            long id = nid.toLong();
            int varfield = (int)((id >> ((3 - index) * 12)) & 0xfff);
            logger.fine(String.format("Sending CID frame, id %x, varfield %x", id, varfield));
            f.setCIM(index, varfield, nida.getNIDa());
        } else if (index == 4) {
            f = new OpenLcbCanFrame(nida.getNIDa());
            f.setRIM(nida.getNIDa());
        } else if (index == 5) {
            f = new OpenLcbCanFrame(nida.getNIDa());
            f.setAMD(nida.getNIDa(), nid);
            complete = true;
        } else {
            // send nothing
            f = null;
        }
        index++;
        return f;
    }

    public int getNIDa() {
        return nida.getNIDa();
    }

    /**
     * @return True if frame matches current NodeID
     */
    boolean compareDataAndNodeID(OpenLcbCanFrame f) {
        // TODO: check for empty data or matching NodeID
        return new NodeID(f.getData()).equals(nid);
    }

    public void processFrame(OpenLcbCanFrame f) {
        if (f == null) {
            return; // as a convenience, ignore
        }

        if (f.isAliasMapEnquiry()) {
            // complete == true is (mostly) Permitted state
            if (complete) {
                if (f.data.length == 0 || compareDataAndNodeID(f)) {
                    // AME for us, reply with AMD
                    OpenLcbCanFrame frame = new OpenLcbCanFrame(nida.getNIDa());
                    frame.setAMD(nida.getNIDa(), nid);
                    sendInterface.send(frame);
                    return;
                }
            }
        }

        if (f.isAliasMapDefinition()) {
            // complete == true is (mostly) Permitted state
            if (complete) {
                if (compareDataAndNodeID(f)) {
                    // AMD for us, reply with AMR and restart
                    OpenLcbCanFrame frame = new OpenLcbCanFrame(nida.getNIDa());
                    frame.setAMR(nida.getNIDa(), nid);
                    sendInterface.send(frame);
                    return;
                }
            }
        }


        if (f.getSourceAlias() != nida.getNIDa()) {
            return;  // not us
        }
        if (f.isCIM() && complete) {
            // CIM with our alias: send RIM
            // if not complete, start over 
            if (complete) {
                OpenLcbCanFrame frame = new OpenLcbCanFrame(nida.getNIDa());
                frame.setRIM(nida.getNIDa());
                sendInterface.send(frame);
            } else {
                index = 4; // send RIM on next cycle
                cancelTimer();
            }
        } else {
            // other frame with our alias: reset and start over
            index = 0;
            complete = false;
            nida.nextAlias();
            cancelTimer();
        }
    }

    public boolean isComplete() {
        return complete;
    }

    protected void cancelTimer() {
        if (timer == null) {
            return; // Probably running from a unit test.
        }
        if (task == null || task.cancel()) {
            // Task was not yet run.
            scheduleTimer(0);
        }
    }

    protected void timerExpired() {
        if (index == 0) {
            while (index < 4) {
                sendInterface.send(nextFrame());
            }
            scheduleTimer(200);
        } else if (index == 4) {
            sendInterface.send(nextFrame());
            sendInterface.send(nextFrame());
            if (done != null) {
                done.run();
                done = null;
            }
        }
    }

    int index = 0;

    // NodeID of this node (node for which alias algorithm running)
    NodeID nid;

    NIDa nida;
    boolean complete = false;

    @Override
    public void send(CanFrame frame) {
        processFrame(new OpenLcbCanFrame(frame));
    }

    public void dispose(){
       cancelTimer();  // dispose of the timer task

       synchronized(NIDaAlgorithm.class) {
           timer.cancel();
           timer = null;
       }

       done = null;
       complete = true;
    }
}
