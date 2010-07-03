package org.openlcb.can;

import org.openlcb.*;

/**
 * Implementation of Node ID Alias assignment computation.
 *
 * This class merely does computations, and doesn't control
 * sending and receiving of CAN frames.
 *
 * 
 * @author  Bob Jacobsen   Copyright 2009, 2010
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
    protected void forceSeedValue(long seed1, long seed2) {
        lfsr1 = seed1;
        lfsr2 = seed2;
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
        byte[] val = this.nid.getContents();

        lfsr1 = (((long)val[0]) << 16) | (((long)val[1]) << 8) | ((long)val[2]);
        lfsr2 = (((long)val[3]) << 16) | (((long)val[4]) << 8) | ((long)val[5]);
    }
    
    /**
     * Advance the sequence generator by one step.
     */
    protected void stepGenerator() {   
       // step the PRNG
       // First, form 2^9*val
       long temp1 = ((lfsr1<<9) | ((lfsr2>>15)&0x1FF)) & 0xFFFFFF;
       long temp2 = (lfsr2<<9) & 0xFFFFFF;
       
       // add
       lfsr2 = lfsr2 + temp2 + 0x7A4BA9l;
       lfsr1 = lfsr1 + temp1 + 0x1B0CA3l;
       
       // carry
       lfsr1 = (lfsr1 & 0xFFFFFF) | ((lfsr2&0xFF000000) >> 24);
       lfsr2 = lfsr2 & 0xFFFFFF;
     }

    /**
     * Reduce the current generator value to an alias value.
     */
    protected int computeAliasFromGenerator() {
        return (int)(lfsr1 ^ lfsr2 ^ (lfsr1>>12) ^ (lfsr2>>12) ) & 0xFFF;
    }

    // Generator state register
    long lfsr1 = 0; // long to act as unsigned
    long lfsr2 = 0;

}
