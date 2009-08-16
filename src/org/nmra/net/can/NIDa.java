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
     * Algorithm to produce seed from the six bytes
     * of a node ID
     */
    protected void loadSeed(NodeID nid) {
        this.nid = nid;
        byte[] id = this.nid.getContents();

        // algorithm, expressed on bytes
        reg = id[0]+id[1]+id[2]+id[3]+id[4]+id[5];
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
     * 16-bit shift register PRNG algorithm from
     * <a href="http://en.wikipedia.org/wiki/Linear_feedback_shift_register">Wikipedia Linear Feedback Shift Register</a> page.
     */
    protected void stepGenerator() {
       bit = (reg & 0x0001) ^
            ((reg & 0x0004) >> 2) ^
            ((reg & 0x0008) >> 3) ^
            ((reg & 0x0020) >> 5);
       reg = (reg >> 1) | (bit << 15);
    }
    /**
     * Reduce the current generator value to an alias
     */
    protected int computeAliasFromGenerator() {
       return (int) (reg & 0xFFFF);
    }

    // Generator state register
    long reg = 0xFFFFL;  // unsigned
    long bit;

}
