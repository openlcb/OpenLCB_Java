package org.openlcb.implementations;

import org.openlcb.*;

/**
 * Example of a OpenLCB algorithm for doing configuration with
 * small number of buttons.
 *<p>
 *<ul>
 *<li>On learners, push blue to choose a C/P and put that C/P in learn mode 
 * (complete cycle turns off blue light and starts over)
 *<li>On the teaching node, push blue to choose a C/P, 
 * push Gold to send the TeachEvent message
 *</ul>
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
public class BlueGoldEngine extends MessageDecoder implements Connection {

    int selectedPC = -1;
    
    public void goldClick() {
        if (selectedPC >= 0) {
            // have a selected P or C
            sendLearnEventMessage(getEventID(selectedPC));
            setGoldLightOn(false);
            setBlueLightOn(false);
            selectedPC = -1;
            System.out.println("send learn event");
            return;
        }
    }

    public void blueClick() {
        // click updates selection
        selectedPC++;
        setBlueLightOn(true);
        //check if wrapping
        if (selectedPC >= producers.size()+consumers.size()) {
            // yes, turn off light until next time
            selectedPC = -1;
            setBlueLightOn(false);
        } else {
            // no, make sure light is on
            setBlueLightOn(true);
        }
        // and print for now (no GUI for this, add LEDs?)
        System.out.println("incremented selectedPC to "+selectedPC);
        return;
    }
    
    public void handleLearnEvent(LearnEventMessage msg, Connection sender){
        // learn
        if (selectedPC >= 0) {
            EventID eid = msg.getEventID();
            System.out.println("Set "+selectedPC+" to "+eid);
            setEventID(selectedPC, eid);
        }
        // exit
        setBlueLightOn(false);
        setGoldLightOn(false);
        selectedPC = -1;
    }
    
    //*************************************************
    //
    // Below here is the class infrastructure
    //
    
    // Methods to be overridden in using classes
    public void setBlueLightOn(boolean f) {
    }
    public boolean getBlueLightOn() {
        return false;
    }
    
    public void setBlueLightBlink(int dwell) {
    }
    
    public void setGoldLightOn(boolean f) {
    }
    public boolean getGoldLightOn() {
        return false;
    }

    public void setGoldLightBlink(int dwell) {
    }

    // ctors, local code and variables, etc
    public BlueGoldEngine(NodeID nid, ScatterGather sg,
                java.util.List<SingleProducer> producers,
                java.util.List<SingleConsumer> consumers) {
        this.nid = nid;
        this.sg = sg;
        this.c = sg.getConnection();
        this.consumers = consumers;
        this.producers = producers;
        
        sg.register(this);
    }
    
    NodeID nid;
    ScatterGather sg;
    Connection c;
    java.util.List<SingleProducer> producers;
    java.util.List<SingleConsumer> consumers;
    
    public void put(Message msg, Connection sender) {
        msg.applyTo(this, sender);
    }
    
    protected void sendLearnPendingMessage() {
        c.put(new LearnPendingMessage(nid), this);
    }
    protected void sendLearnCancelMessage() {
        c.put(new LearnCancelMessage(nid), this);
    }
    
    protected void sendLearnEventMessage(EventID eid) {
        LearnEventMessage msg = new LearnEventMessage(nid, eid);
        c.put(msg, this);
        // also process here
        handleLearnEvent(msg, null);
    }
    
    EventID getEventID(int n) {
        // check whether producer (first) or consumer (2nd)
        if (n < producers.size()) {
            return producers.get(n).getEventID();
        } else {
            return consumers.get(n-consumers.size()).getEventID();
        }
    }
    
    void setEventID(int n, EventID eid) {
        // check whether producer (first) or consumer (2nd)
        if (n < 0) return; // nothing to do
        else if (n < producers.size()) {
            producers.get(n).setEventID(eid);
        } else {
            consumers.get(n-consumers.size()).setEventID(eid);
        }
    }
}
