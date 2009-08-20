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

        // simplest starting point
        //reg = id[0]+id[1]+id[2]+id[3]+id[4]+id[5];

        // prototype v1
        reg = id[0] ^ id[1] <<5 ^ id[2] <<10 ^ id[3] <<15 ^ id[4] << 20 ^ id[5] << 24;
        if (reg == 0)
            reg = ( id[0] << 23)+(id[1] << 19)+(id[2] << 15)+(id[3] << 11)+(id[4] << 7)+id[5];
        if (reg == 0)
            reg = 0xAC01;

        // prototype v2
        reg = id[0] ^ (id[1] <<5) ^ (id[2] <<10) ^ (id[3] <<15) 
                    ^ (id[4] << 20) ^ (id[5] << 24) 
                    ^ ((id[3]^id[4]^id[5])<<8);

    }
    
    /**
     * Advance the sequence generator by one step.
     */
    protected void stepGenerator() {
        
        // 16-bit shift register PRNG algorithm from e.g.
        // <a href="http://en.wikipedia.org/wiki/Linear_feedback_shift_register">Wikipedia Linear Feedback Shift Register</a> page.
        //long bit = (reg & 0x0001) ^
        //    ((reg & 0x0004) >> 2) ^
        //    ((reg & 0x0008) >> 3) ^
        //    ((reg & 0x0020) >> 5);
        //reg = (reg >> 1) | (bit << 15);

        // prototype V1
        // see <http://en.wikipedia.org/wiki/Linear_feedback_shift_register> example 2
        reg = (reg >> 1) ^ (-(reg & 1) & 0xd0000001); 
 }

    /**
     * Reduce the current generator value to an alias value.
     */
    protected int computeAliasFromGenerator() {
        int retval;
        
        // just return low bits, but force non-zero
        //retval = (int) (reg & 0xFFFF);
        //if (retval == 0) retval = 1;
        
        // merge 32 bits to just 16
        retval = (int) ( (reg ^ (reg >>16) ) & 0xFFFF);
        if (retval == 0) retval = 1;
        
        return retval;
    }

    // Generator state register
    long reg = 0xFFFFL;  // unsigned

}
