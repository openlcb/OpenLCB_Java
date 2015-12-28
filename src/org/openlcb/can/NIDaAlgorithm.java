package org.openlcb.can;

import org.openlcb.*;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

/**
 * Implementation of Node ID Alias assignment computation.
 * Provides and processes frames, but other code must move them 
 * to and from the actual interface.
 * It also requires subclassing to provide a timer function.
 * 
 * @author  Bob Jacobsen   Copyright 2009, 2010
 * @version $Revision$
 */
public class NIDaAlgorithm implements CanFrameListener {

    /// Callback to invoke when the alias was successfully reserved.
    private Runnable done;
    private CanFrameListener sendInterface;
    private Timer timer;
    private TimerTask task;
    private static Logger logger = Logger.getLogger(new Object(){}.getClass().getSuperclass()
            .getName());

    private synchronized void scheduleTimer(long delay) {
        task = new TimerTask() {
        @Override
        public void run() {
            timerExpired();
        }
        };
        timer.schedule(task, delay);
    }

    public NIDaAlgorithm(NodeID n) {
        nid = n;
        nida = new NIDa(n);
    }

    public NIDaAlgorithm(NodeID n, CanFrameListener sendInterface) {
        this(n);
        this.sendInterface = sendInterface;
        timer = new Timer();
    }

    public void start(Runnable done) {
        this.done = done;
        scheduleTimer(100);
    }

    public OpenLcbCanFrame nextFrame() {
        OpenLcbCanFrame f;
        if (index<4) {
            f = new OpenLcbCanFrame(nida.getNIDa());
            long id = nid.toLong();
            int varfield = (int)((id >> ((3-index) * 12)) & 0xfff);
            logger.fine(String.format("Sending CID frame, id %x, varfield %x", id, varfield));
            f.setCIM(index, varfield, nida.getNIDa());
        } else if (index == 4) {
            f = new OpenLcbCanFrame(nida.getNIDa());
            f.setRIM(nida.getNIDa());
            complete = true;
        } else {
            // send nothing
            f = null;
        }
        index++;
        return f;
    }
    
    public int getNIDa() { return nida.getNIDa(); }

    public void processFrame(OpenLcbCanFrame f) {
        if (f == null) return; // as a convenience, ignore

        // System.out.println("process "+Integer.toHexString(f.getNodeIDa())
        //                    +" vs our "+Integer.toHexString(nida.getNIDa()));

        if (f.getSourceAlias() != nida.getNIDa()) return;  // not us
        if (f.isCIM() && complete) {
            // CIM with our alias: send RIM
            index = 4;
            cancelTimer();
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
        if (timer == null) return; // Probably running from a unit test.
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
            if (done != null) {
                done.run();
                done = null;
            }
        }
    }
    
    int index = 0;
    NodeID nid;
    NIDa nida;
    boolean complete = false;

    @Override
    public void send(CanFrame frame) {
        processFrame(new OpenLcbCanFrame(frame));
    }
}
