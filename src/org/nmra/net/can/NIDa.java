package org.nmra.net.can;

import org.nmra.net.*;

/**
 * Implementation of Node ID Alias assignment computation.
 *
 * This class merely does computations, and doesn't control
 * sending and receiving of CAN frames.
 *
 * 
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class NIDa {

    NodeID nid;
    
    public NIDa(NodeID nid) {
        // initialize seed from NodeID
        loadSeed(nid);
        
        // step the generator once and use result
        nextAlias();
}

    public int getNIDa() { return nida; }

    /**
     * Update to the next alias in the series
     */
    public void nextAlias() { 
        stepGenerator();
        nida = computeAliasFromGenerator();
    }
    
    // temporary storage of current alias value
    int nida = 0;
    
    /**
     * For use in testing subclasses only,
     * this forces the current seed to a specific
     * value. In the process, it updates the alias
     * to the value implied by this seed.
     */
    protected void forceSeedValue(long seed) {
        reg = seed;
        nida = computeAliasFromGenerator();
    }

    /**
     * For use in testing subclasses only,
     * this forces the current alias to a specific
     * value. It does not change the seed.
     */
    protected void forceAliasValue(int alias) {
        nida = alias;
    }
   
    /**
     * Algorithm to produce seed from the six bytes
     * of a node ID.
     * <p>Generally, it's easiest to work with
     * the bytes of the ID.  The MSB is id[0].
     */
    protected void loadSeed(NodeID nid) {
        this.nid = nid;
        byte[] id = this.nid.getContents();

        reg =   ((id[0] & 0xff) << 40)
              | ((id[1] & 0xff) << 32)
              | ((id[2] & 0xff) << 24)
              | ((id[3] & 0xff) << 16)
              | ((id[4] & 0xff) <<  8)
              | ((id[5] & 0xff) <<  0);
    }
    
    /**
     * Advance the sequence generator by one step.
     */
    protected void stepGenerator() {
        
        // See H G Kuehn, CACM 8/1961
        reg = ( (512+1)*reg + 0x1B0CA37A4BA9L)
                & 0xFFFFFFFFFFFFL;
 }

    /**
     * Reduce the current generator value to an alias value.
     */
    protected int computeAliasFromGenerator() {
        
        int s1 = (int)reg&0xFFFFFF;
        int s2 = (int)(reg>>12)&0xFFFFFF;
        int s3 = (int)(reg>>24)&0xFFFFFF;
        int s4 = (int)(reg>>36)&0xFFFFFF;
        
        return s1^s2^s3^s4;
    }

    // Generator state register
    long reg = 0xFFFFL;  // unsigned

}
