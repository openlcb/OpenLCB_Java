package simulations;

/**
 * Simple class for doing calculations/simulations
 * on NodeID number collisions. 
 *
 * Not prototype code.
 *
 * @author  Bob Jacobsen   Copyright 2008, 2009
 * @version $Revision$
 */
 
public class NodeIDCollisions {

  static final int LEN = 500000;
  
  static long randomNbits(int n) { 
    long m = 1l<<n;
    return (long)(Math.random()*m);
  }
  static long random12() { return randomNbits(12); }
  static long random16() { return randomNbits(16); }
  static long random48() { return randomNbits(48); }
  
  /** 
   * Seed generator from a 48-bit value, and
   * return the 1st alias using some algorithm
   */
  static long seed(long nid) {
    byte[] id = new byte[6];
    id[5] = (byte)((nid>>40)&0xffl); // MSB
    id[4] = (byte)((nid>>32)&0xffl);
    id[3] = (byte)((nid>>24)&0xffl);
    id[2] = (byte)((nid>>16)&0xffl);
    id[1] = (byte)((nid>>8)&0xffl);
    id[0] = (byte)(nid&0xff);        // LSB
    
    long reg;  // generator state
    
    // Seeding 
    
    // prototype v1 seed algorithm
    reg = id[0] ^ id[1] <<5 ^ id[2] <<10 ^ id[3] <<15 ^ id[4] << 20 ^ id[5] << 24;
    if (reg == 0)
        reg = ( id[0] << 23)+(id[1] << 19)+(id[2] << 15)+(id[3] << 11)+(id[4] << 7)+id[5];
    if (reg == 0)
        reg = 0xAC01;
      
    // prototype v2
    reg = id[0] ^ (id[1] <<5) ^ (id[2] <<10) ^ (id[3] <<15) 
                ^ (id[4] << 20) ^ (id[5] << 24) 
                ^ ((id[3]^id[4]^id[5])<<8);

    // prototype v3: xor 3 16-bit quantities
    reg =         ( ((id[0]<<8) & 0xFF) | (id[1] & 0xFF) ) 
                ^ ( ((id[2]<<8) & 0xFF) | (id[3] & 0xFF) )  
                ^ ( ((id[4]<<8) & 0xFF) | (id[5] & 0xFF) );

    // first step
    
    // prototype V1 stepped
    // see <http://en.wikipedia.org/wiki/Linear_feedback_shift_register> example 2
    reg = (reg >> 1) ^ (-(reg & 1) & 0xd0000001); 
    
    // create alias
    int retval;
    
    //prototype v1
    retval = (int) ( (reg ^ (reg >>16) ) & 0xFFFF);
    if (retval == 0) retval = 1;
    
    return retval;
  }
  
  static long arrayMax(long[] a) {
    long m = 0;
    for (int i = 0; i<a.length; i++) 
        if (m<a[i]) m = a[i];
    return m;
  }
  
  static long countCollisions(long[] a) {
    java.util.Arrays.sort(a);
    long c = 0;
    for (int i = 0; i<a.length-1; i++) 
        if (a[i]==a[i+1]) c++;
    return c;
  }
  
  static long[] chooseNfromM(int n, int m) {
    long[] result = new long[n];
    boolean[] used = new boolean[m];
    for (int i = 0; i < m; i++) used[i] = false;
    int i = 0;
    while (i < result.length) {
        int j = (int)(Math.random()*m);
        if (!used[j]) {
            result[i++] = j;
            used[j] = true;
        }  // otherwise, go round again without incrementing
    }
    return result;
  }
  
  public static void main(String[] args) {

    singleCollisionCheck(12, 10, 10*1000*1000);
    singleCollisionCheck(12, 25, 1000*1000);
    singleCollisionCheck(12, 100, 300*1000);
    singleCollisionCheck(12, 250, 300*300);
    singleCollisionCheck(12, 1000, 100*300);
    System.out.println("");

    checkSeedCollisions();
    System.out.println("");
        
    checkMfgNumberedGroups(10, 2, 100, 300*300, false);
    checkMfgNumberedGroups(5, 5, 100, 300*300, false);
    checkMfgNumberedGroups(5, 20, 100, 300*300, false);
    System.out.println("");
    
    checkMfgNumberedGroups(10, 2, 1000, 300*300, false);
    checkMfgNumberedGroups(5, 5, 1000, 300*300, false);
    checkMfgNumberedGroups(5, 20, 1000, 300*300, false);
    System.out.println("");

    checkMfgNumberedGroups(10, 2, 100, 300*300, true);
    checkMfgNumberedGroups(5, 5, 100, 300*300, true);
    checkMfgNumberedGroups(5, 20, 100, 300*300, true);
    System.out.println("");

    checkMfgNumberedGroups(10, 2, 1000, 300*300, true);
    checkMfgNumberedGroups(5, 5, 1000, 300*300, true);
    checkMfgNumberedGroups(5, 20, 1000, 300*300, true);
    System.out.println("");


    checkMfgNumberedGroups(10, 2, 65535, 100*300, false);
    checkMfgNumberedGroups(5, 5, 65535, 100*300, false);
    checkMfgNumberedGroups(50, 2, 65535, 100*300, false);
    System.out.println("");

    checkMfgNumberedGroups(10, 2, 65535, 100*300, true);
    checkMfgNumberedGroups(5, 5, 65535, 100*300, true);
    checkMfgNumberedGroups(50, 2, 65535, 100*300, true);
    System.out.println("");
  }
  
  static void singleCollisionCheck(int nBits, int nNodes, int nRuns) {
    long[] samples = new long[nNodes];
    int hitruns = 0;
    int hitnodes = 0;
    int run = 0;
    while (run < nRuns) {
        for (int i = 0; i< nNodes; i++) 
            samples[i] = randomNbits(nBits);
        long n = countCollisions(samples);
        hitnodes += n;
        if (n>0) hitruns++;
        run++;
    }   
    System.out.println(""+nBits+" ID "+nNodes
                            +" net collision rate "+((double)hitruns)/((double)nRuns)
                            +" node collision rate "+((double)hitnodes)/((double)nRuns)/((double)nNodes));
  }
  
  static void checkSeedCollisions() {
        // check for collisions in the seed
        int nNodes = 100;
        int nRuns = 300*300;
        long[] samples = new long[nNodes];
        int hitruns = 0;
        int hitnodes = 0;
        int run = 0;
        while (run < nRuns) {
            for (int i = 0; i< nNodes; i++) 
                samples[i] = seed(random48());
            long n = countCollisions(samples);
            hitnodes += n;
            if (n>0) hitruns++;
            run++;
        }   
        System.out.println("48 seed "+nNodes
                            +" net collision rate "+((double)hitruns)/((double)nRuns)
                            +" node collision rate "+((double)hitnodes)/((double)nRuns)/((double)nNodes));

        hitruns = 0;
        hitnodes = 0;
        run = 0;
        while (run < nRuns) {
            for (int i = 0; i< nNodes; i++) 
                samples[i] = random48()&0xFFFF;
            long n = countCollisions(samples);
            hitnodes += n;
            if (n>0) hitruns++;
            run++;
        }   
        System.out.println("48 random low bits "+nNodes
                            +" net collision rate "+((double)hitruns)/((double)nRuns)
                            +" node collision rate "+((double)hitnodes)/((double)nRuns)/((double)nNodes));
    }


    static void checkMfgNumberedGroups(int nNodes, int nMfg, int nBoards, int nRuns, boolean seeded) {
        // check for collisions of N boards of M manufacturers 
        //              from 1st L boards produced    
    
        long[] samples = new long[nNodes];
        int hitruns = 0;
        int hitnodes = 0;
        int run = 0;
        
        while (run < nRuns) {
            long[] temp;
            samples = new long[nNodes*nMfg];
    
            int k = 0;
            for (int j = 0; j < nMfg; j++) {
                // concatenate low values from the five sets of boards
                temp = chooseNfromM(nNodes,nBoards);
                for (int i = 0; i< temp.length; i++) samples[k++] = seed(temp[i]+(seeded? (3<<24)*(j+1): 0));
            }        
            long n = countCollisions(samples);
            hitnodes += n;
            if (n>0) hitruns++;
            run++;
        }   
        System.out.println("12 bit "+(seeded?" seeded ":"")+" ID in "+nNodes+" nodes from each of "+nMfg+" vendors first "+nBoards+" production "
                                +" net collision rate "+((double)hitruns)/((double)nRuns)
                                +" node collision rate "+((double)hitnodes)/((double)nRuns)/((double)nNodes));
    }  
}
