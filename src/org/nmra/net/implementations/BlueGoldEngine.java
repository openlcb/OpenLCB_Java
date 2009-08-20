package org.nmra.net.implementations;

import org.nmra.net.*;

/**
 * Example of a NMRAnet algorithm for doing configuration with
 * small number of buttons.
 *<p>
 * This handles both "configuring" and "being configured"
 * cases. It also handles both consumers and producers
 * via working with the
 * {@link SingleConsumerNode} and {@link SingleProducerNode}
 * example classes.
 * <p>
 * For button inputs, it currently has only "click" operations.
 * Perhaps "down" and "up" will be needed eventually.
 * Similarly, it only does "on" and "off" for the two lights.
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class BlueGoldEngine implements Connection {

    public void blueClick() {
        blue = !blue;
        setBlueLightOn(blue);
    }
    
    public void goldClick() {
        gold = !gold;
        if (gold) sendLearnPendingMessage();
        setGoldLightOn(gold);
    }
    
    boolean blue = false;
    boolean gold = false;

    //
    // Below here is the class infrastructure
    //
    
    // Methods to be overridden in using classes
    public void setBlueLightOn(boolean f) {
    }
    
    public void setGoldLightOn(boolean f) {
    }

    // ctors, local code and variables, etc
    public BlueGoldEngine(NodeID nid, ScatterGather sg) {
        this.nid = nid;
        this.sg = sg;
        this.c = sg.getConnection();
        sg.register(this);
    }
    
    NodeID nid;
    ScatterGather sg;
    Connection c;
    
    public void put(Message msg, Connection sender) {
    }
    
    protected void sendLearnPendingMessage() {
        c.put(new LearnPendingMessage(nid), this);
    }
    
}
