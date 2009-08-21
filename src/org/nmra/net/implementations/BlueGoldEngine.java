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
public class BlueGoldEngine extends MessageDecoder implements Connection {

    boolean isConfigMaster = false;
    boolean isConfigSlave = false;
    int selectedPC = -1;
    
    public void goldClick() {
        // is click making us the master?
        if (!isConfigMaster) {
            sendLearnPendingMessage();
            setGoldLightOn(true);
            isConfigMaster = true;
            System.out.println("enter master");
            // reset blue selection to off end
            selectedPC = -1;
        } 
        // if not, we are already master, leave learn mode
        else {
            if (selectedPC >= 0) {
                // have a selected P or C
                sendLearnEventMessage(getEventID(selectedPC));
                setGoldLightOn(false);
                setBlueLightOn(false);
                selectedPC = -1;
                isConfigMaster = false;
                System.out.println("exit master mode");
                return;
            } else {
                // nothing selected, cancel
                sendLearnCancelMessage();
                setGoldLightOn(false);
                setBlueLightOn(false);
                selectedPC = -1;
                isConfigMaster = false;
                System.out.println("exit master mode");
                return;
            }
        }
    }
    
    public void blueClick() {
        // Are we master or slave?
        if (isConfigMaster || isConfigSlave) {
            // yes, click updates selection
            selectedPC++;
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
        // else not master, ignore for now
        else {
        }
    }
    
    public void handleLearnPending(LearnPendingMessage msg, Connection sender){
        // if not in master mode, enter slave mode
        if (!isConfigMaster && !isConfigSlave) {
            isConfigSlave = true;
            System.out.println("enter slave mode");
        }
    }

    public void handleLearnEvent(LearnEventMessage msg, Connection sender){
        // if in slave mode, learn and exit
        if (isConfigSlave) {
            // learn
            if (selectedPC >= 0) {
                EventID eid = msg.getEventID();
                System.out.println("Set "+selectedPC+" to "+eid);
                setEventID(selectedPC, eid);
            }
            // exit
            isConfigSlave = false;
            System.out.println("exit slave mode");
            setBlueLightOn(false);
            selectedPC = -1;
        }
    }

    public void handleLearnCancel(LearnCancelMessage msg, Connection sender){
        // if in slave mode, exit
        if (isConfigSlave) {
            isConfigSlave = false;
            System.out.println("exit slave mode");
            setBlueLightOn(false);
            selectedPC = -1;
        }
    }
    
    //*************************************************
    //
    // Below here is the class infrastructure
    //
    
    // Methods to be overridden in using classes
    public void setBlueLightOn(boolean f) {
    }
    
    public void setBlueLightBlink(int dwell) {
    }
    
    public void setGoldLightOn(boolean f) {
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
        c.put(new LearnEventMessage(nid, eid), this);
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
