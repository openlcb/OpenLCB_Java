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
  static long random16() { return randomNbits(16); }
  static long random48() { return randomNbits(48); }
  
  static long seed(long id) {
    long t5 = (id>>40)&0xffl; // MSB
    long t4 = (id>>32)&0xffl;
    long t3 = (id>>24)&0xffl;
    long t2 = (id>>16)&0xffl;
    long t1 = (id>>8)&0xffl;
    long t0 = id&0xff;        // LSB
    
    // long lfsr = t0 ^ (t1 <<5) ^ (t2 <<10) ^ (t3 <<15) ^ (t4 << 20) ^ (t5 << 24);
    long lfsr = t0 ^ (t1 <<5) ^ (t2 <<10) ^ (t3 <<15) ^ (t4 << 20) ^ (t5 << 24) ^ ((t3^t4^t5)<<8);

    if (lfsr == 0)
      lfsr = (t0 << 23)+(t1 << 19)+(t2 << 15)+(t3 << 11)+(t4 << 7)+t5;
    if (lfsr == 0)
      lfsr = 0xAC01;
      
    // first step
    lfsr = (lfsr >> 1) ^ (-(lfsr & 1) & 0xd0000001);
    
    return (lfsr^(lfsr>>16))&0xFFFF;
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

    //singleCollisionCheck(16, 10, 10*1000*1000);
    //singleCollisionCheck(16, 25, 1000*1000);
    //singleCollisionCheck(16, 100, 300*1000);
    //singleCollisionCheck(16, 250, 300*300);
    //singleCollisionCheck(16, 1000, 100*300);

    // checkSeedCollisions();
        
    //checkMfgNumberedGroups(10, 2, 100, 300*300, false);
    //checkMfgNumberedGroups(5, 5, 100, 300*300, false);
    checkMfgNumberedGroups(5, 20, 100, 300*300, false);
    //checkMfgNumberedGroups(5, 5, 1000, 300*300, false);

    //checkMfgNumberedGroups(10, 2, 100, 300*300, true);
    //checkMfgNumberedGroups(5, 5, 100, 300*300, true);
    checkMfgNumberedGroups(5, 20, 100, 300*300, true);
    //checkMfgNumberedGroups(5, 5, 1000, 300*300, true);


    //checkMfgNumberedGroups(10, 2, 65535, 100*300, false);
    //checkMfgNumberedGroups(5, 5, 65535, 100*300, false);
    //checkMfgNumberedGroups(50, 2, 65535, 100*300, false);

    //checkMfgNumberedGroups(10, 2, 65535, 100*300, true);
    //checkMfgNumberedGroups(5, 5, 65535, 100*300, true);
    //checkMfgNumberedGroups(50, 2, 65535, 100*300, true);
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
                for (int i = 0; i< temp.length; i++) samples[k++] = seed(temp[i]+(seeded? (1<<24)*j: 0));
            }        
            long n = countCollisions(samples);
            hitnodes += n;
            if (n>0) hitruns++;
            run++;
        }   
        System.out.println("16 bit ID in "+nNodes+" nodes from each of "+nMfg+" vendors first "+nBoards+" production "
                                +" net collision rate "+((double)hitruns)/((double)nRuns)
                                +" node collision rate "+((double)hitnodes)/((double)nRuns)/((double)nNodes));
    }  
}
