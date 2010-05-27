package org.openlcb.implementations;

import org.openlcb.*;

/**
 * Example of a OpenLCB algorithm for doing configuration with
 * small number of buttons. This an extended form that allows 
 * multiple selection and deselection.
 *<p>
 *<ul>
 *<li>On learners, push blue to choose a C/P, then gold to put that C/P in learn mode 
 * (complete cycle turns off blue light and starts over)
 *<li>On the teaching node, push gold to choose teach mode, then
 * blue to select a C/P, 
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
 * @author  Bob Jacobsen   Copyright 2010
 * @version $Revision$
 */
public class BlueGoldExtendedEngine extends BlueGoldEngine {

    boolean[] isSelectedPC;
    
    public void goldClick() {
        if (getGoldLightOn() && getBlueLightOn()) {
            // in teach mode, send LearnEvent
            if (selectedPC >= 0) {  // redundant, as blue is on
                // have a selected P or C
                sendLearnEventMessage(getEventID(selectedPC));
                setGoldLightOn(false);
                setBlueLightOn(false);
                selectedPC = -1;
                return;
            }
        } else if (!getGoldLightOn() && getBlueLightOn()) {
            // in learn mode, select item
            if (selectedPC >= 0) { // redundant, as blue is on
                isSelectedPC[selectedPC] = true;
                selectedPC = -1;
                setBlueLightOn(false);
            }
        } else if (!getGoldLightOn() && !getBlueLightOn()) {
            // starting up, light gold
            setGoldLightOn(true);
        } else { // gold on, blue off
            // give up
            setGoldLightOn(false);
            setBlueLightOn(false);
            selectedPC = -1;
            for (boolean p : isSelectedPC) p = false;
        }
    }

    public void blueClick() {
        // click updates selection
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
        return;
    }
    
    public void longBluePress() {
        // reset selections, leave gold alone
        for (int i=0; i<isSelectedPC.length; i++) {
            isSelectedPC[i] = false;
        }
        selectedPC = -1;
        setBlueLightOn(false);
    }

    public void handleLearnEvent(LearnEventMessage msg, Connection sender){
        // learn
        for (int i=0; i<isSelectedPC.length; i++) {
            if (isSelectedPC[i]) {
                EventID eid = msg.getEventID();
                System.out.println("Set "+i+" to "+eid);
                setEventID(i, eid);
            }
            isSelectedPC[i] = false;
        }
        // exit
        setBlueLightOn(false);
        setGoldLightOn(false);
        selectedPC = -1;
    }
    
    public BlueGoldExtendedEngine(NodeID nid, ScatterGather sg,
                java.util.List<SingleProducer> producers,
                java.util.List<SingleConsumer> consumers) {
        super(nid, sg, producers, consumers);
        
        isSelectedPC = new boolean[producers.size()+consumers.size()];
        for (boolean p : isSelectedPC) p = false;
    }
}
