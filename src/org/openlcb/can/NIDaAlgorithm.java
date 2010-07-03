package org.openlcb.can;

import org.openlcb.*;

/**
 * Implementation of Node ID Alias assignment computation.
 * Provides and processes frames, but other code must move them 
 * to and from the actual interface.
 * It also requires subclassing to provide a timer function.
 * 
 * @author  Bob Jacobsen   Copyright 2009, 2010
 * @version $Revision$
 */
public class NIDaAlgorithm {

    public NIDaAlgorithm(NodeID n) {
        nid = n;
        nida = new NIDa(n);
    }
    
    public OpenLcbCanFrame nextFrame() {
        OpenLcbCanFrame f;
        if (index<4) {
            f = new OpenLcbCanFrame(nida.getNIDa());
            f.setCIM(index, 0, nida.getNIDa());
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
    
    public long getNIDa() { return nida.getNIDa(); }
    
    public void processFrame(OpenLcbCanFrame f) {
        if (f == null) return; // as a convenience, ignore

        // System.out.println("process "+Integer.toHexString(f.getNodeIDa())
        //                    +" vs our "+Integer.toHexString(nida.getNIDa()));

        if (f.getSourceAlias() != nida.getNIDa()) return;  // not us
        if (f.isCIM() || f.isRIM()) {
            // CIM or RIM with our alias
            if (complete) {
                // complete, so alias is ours;: send RIM
                index = 4;
            } else {
                // reset and start over
                index = 0;
                cancelTimer();
            }    
            nida.nextAlias();
        } else {
            // other frame with our alias: send RIM
            index = 4;
        }
    }
    
    public boolean isComplete() {
        return complete;
    }

    /**
     * Override this in an implementing subclass
     * to invoke timerDone() after the specified interval.
     */
    protected void setTimer() {
    }
    
    /**
     * Override this in an implementing subclass
     * to step any running timer.
     */
    protected void cancelTimer() {
    }
    
    protected void timerExpired() {
    }
    
    int index = 0;
    NodeID nid;
    NIDa nida;
    boolean complete = false;
}
