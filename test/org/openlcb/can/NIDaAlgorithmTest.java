package org.openlcb.can;

import org.openlcb.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class NIDaAlgorithmTest extends TestCase {
    
    public void testBuild() {
        // just checks ctor via setup
        Assert.assertTrue("not complete", !alg.isComplete());
    }
    
    public void testFirst() {
        NmraNetCanFrame f = alg.nextFrame();        
        Assert.assertTrue("not complete", !alg.isComplete());
        
        // first frame is CIM
        Assert.assertTrue(f.isCIM());

    }
    
    public void testSeventh() {
        NmraNetCanFrame f;        
        Assert.assertTrue("not complete", !alg.isComplete());
        
        // seventh frame is RIM
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        Assert.assertTrue((f = alg.nextFrame()).isRIM());
    
        Assert.assertTrue("complete", alg.isComplete());

        Assert.assertEquals((f = alg.nextFrame()), null);
    }
    
    public void testNotAConflict() {
        NmraNetCanFrame f;        
        Assert.assertTrue("not complete", !alg.isComplete());
        
        // seventh frame is RIM
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        alg.processFrame(NmraNetCanFrame.makeCimFrame(1, 0, 0));
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        alg.processFrame(NmraNetCanFrame.makeCimFrame(1, 0, 0));
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        alg.processFrame(NmraNetCanFrame.makeCimFrame(1, 0, 0));
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        alg.processFrame(NmraNetCanFrame.makeCimFrame(1, 0, 0));
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        alg.processFrame(NmraNetCanFrame.makeCimFrame(1, 0, 0));
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        alg.processFrame(NmraNetCanFrame.makeCimFrame(1, 0, 0));
        Assert.assertTrue((f = alg.nextFrame()).isRIM());
        alg.processFrame(NmraNetCanFrame.makeCimFrame(1, 0, 0));
    
        Assert.assertTrue("complete", alg.isComplete());

        Assert.assertEquals((f = alg.nextFrame()), null);
    }
    
    public void testConflictAfterOne() {
        NmraNetCanFrame f;        
        Assert.assertTrue("not complete", !alg.isComplete());
        
        // start
        Assert.assertTrue((f = alg.nextFrame()).isCIM());

        // inject conflict 
        alg.processFrame(NmraNetCanFrame.makeCimFrame(f.getNodeIDa(), 0, 0));
        
        // seventh frame after now is RIM
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        Assert.assertTrue((f = alg.nextFrame()).isRIM());
    
        Assert.assertTrue("complete", alg.isComplete());

        Assert.assertEquals((f = alg.nextFrame()), null);
    }
    
    public void testLatecomerConflict() {
        NmraNetCanFrame f;        
        Assert.assertTrue("not complete", !alg.isComplete());
        
        // seventh frame after start is RIM
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        Assert.assertTrue((f = alg.nextFrame()).isCIM());
        Assert.assertTrue((f = alg.nextFrame()).isRIM());
        int nida = f.getNodeIDa();
        Assert.assertTrue("complete", alg.isComplete());

        Assert.assertEquals((f = alg.nextFrame()), null);

        // inject conflict 
        alg.processFrame(NmraNetCanFrame.makeCimFrame(nida, 0, 0));

        // still active
        Assert.assertTrue("complete", alg.isComplete());
        // wants to send RIM
        Assert.assertTrue((f = alg.nextFrame()).isRIM());
    }
    
    public void testSequentialStart2() {
        NIDaAlgorithm alg1 = new NIDaAlgorithm(new NodeID(new byte[]{10,11,12,13,14,15}));
        NIDaAlgorithm alg2 = new NIDaAlgorithm(new NodeID(new byte[]{20,21,22,23,24,25}));
        NmraNetCanFrame f;        

        // check to make sure seeds are different; condition of test, not test itself
        Assert.assertTrue("starting aliases should differ", 
                            alg1.getNIDa()!=alg2.getNIDa());

        // take two steps in 1, then start up 2
        f = alg1.nextFrame();
        f = alg1.nextFrame();
        
        int expectedCount = 7;
        int count = sequentialRunner(new NIDaAlgorithm[]{alg1, alg2}, expectedCount);

        debug("tSS2 converges "+count);
        if (count != expectedCount)
            warn("tSS2 count "+count+" not expectedCount "+expectedCount); 
        
        // should be done
        Assert.assertTrue("1 complete", alg1.isComplete());
        Assert.assertTrue("2 complete", alg2.isComplete());
        
    }
    
    public void testSequentialCollisionStart2() {
        // this is getting identical aliases by tricking the seed computation.
        NubNIDaAlgorithm alg1 = new NubNIDaAlgorithm(new NodeID(new byte[]{10,11,12,13,14,15}));
        alg1.forceSeedValue(0xAC01L);
        NubNIDaAlgorithm alg2 = new NubNIDaAlgorithm(new NodeID(new byte[]{11,10,12,13,14,15}));
        alg2.forceSeedValue(0xAC01L);
        NmraNetCanFrame f;        

        // check to make sure seeds are same; condition of test, not test itself
        Assert.assertTrue("starting aliases should agree", 
                            alg1.getNIDa()==alg2.getNIDa());

        // take two steps in 1, then start up 2
        f = alg1.nextFrame();
        f = alg1.nextFrame();
        
        int expectedCount = 7;
        int count = sequentialRunner(new NIDaAlgorithm[]{alg1, alg2}, 2*expectedCount);

        debug("tSCS2 converges in "+count);
        if (count != expectedCount)
            warn("tSCS2 count "+count+" not expectedCount "+expectedCount); 
        
        // should be done
        Assert.assertTrue("1 complete", alg1.isComplete());
        Assert.assertTrue("2 complete", alg2.isComplete());
        Assert.assertTrue("found different", alg1.getNIDa() != alg2.getNIDa());
    }
    
    /**
     * Test whether 10 nodes can converge when messages aren't resequenced.
     * The simulates the case where nodes send slowly, so
     * CAN propagates them in order.
     */
    public void testSequentialStart10() {
        NIDaAlgorithm alg1 = new NIDaAlgorithm(new NodeID(new byte[]{10,11,12,13,14,15}));
        NIDaAlgorithm alg2 = new NIDaAlgorithm(new NodeID(new byte[]{20,21,22,23,14,15}));
        NIDaAlgorithm alg3 = new NIDaAlgorithm(new NodeID(new byte[]{30,31,32,33,14,15}));
        NIDaAlgorithm alg4 = new NIDaAlgorithm(new NodeID(new byte[]{40,41,42,43,14,15}));
        NIDaAlgorithm alg5 = new NIDaAlgorithm(new NodeID(new byte[]{50,51,52,53,14,15}));
        NIDaAlgorithm alg6 = new NIDaAlgorithm(new NodeID(new byte[]{60,61,62,63,14,15}));
        NIDaAlgorithm alg7 = new NIDaAlgorithm(new NodeID(new byte[]{70,71,72,73,14,15}));
        NIDaAlgorithm alg8 = new NIDaAlgorithm(new NodeID(new byte[]{80,81,82,83,14,15}));
        NIDaAlgorithm alg9 = new NIDaAlgorithm(new NodeID(new byte[]{90,91,92,93,14,15}));
        NIDaAlgorithm alg10 = new NIDaAlgorithm(new NodeID(new byte[]{100,101,102,103,14,15}));
        NmraNetCanFrame f;        

        NIDaAlgorithm[] algs = new NIDaAlgorithm[]
                        {alg1, alg2, alg3, alg4, alg5, alg6, alg7, alg8, alg9, alg10};

        // check to make sure seeds are different; condition of test, not test itself
        for (int i=0; i<algs.length; i++)
            for (int j=i+1; j<algs.length; j++)
                Assert.assertTrue("starting aliases should differ: "+i+","+j, 
                            algs[i].getNIDa()!=algs[j].getNIDa());
        // take two steps in 1, then start up 
        f = alg1.nextFrame();
        f = alg1.nextFrame();
        
        int expectedCount = 7; // count cycles
        int count = sequentialRunner(algs, 2*expectedCount);

        debug("tSS10 converges "+count);
        if (count != expectedCount)
            warn("tSS10 count "+count+" not expectedCount "+expectedCount); 

        // should all be done
        Assert.assertTrue("1 complete", alg1.isComplete());
        Assert.assertTrue("2 complete", alg2.isComplete());
        Assert.assertTrue("3 complete", alg3.isComplete());
        Assert.assertTrue("4 complete", alg4.isComplete());
        Assert.assertTrue("5 complete", alg5.isComplete());
        Assert.assertTrue("6 complete", alg6.isComplete());
        Assert.assertTrue("7 complete", alg7.isComplete());
        Assert.assertTrue("8 complete", alg8.isComplete());
        Assert.assertTrue("9 complete", alg9.isComplete());
        Assert.assertTrue("10 complete", alg10.isComplete());
        
    }
    
    /**
     * Test whether 10 nodes can converge using the CAN-priority simulator.
     * The simulates the case where nodes are sending as fast as possible, so
     * CAN arbitrates. Seeds are different.
     */
    public void testPriorityStart10() {
        NIDaAlgorithm alg1 = new NIDaAlgorithm(new NodeID(new byte[]{10,11,12,13,14,15}));
        NIDaAlgorithm alg2 = new NIDaAlgorithm(new NodeID(new byte[]{20,21,22,23,14,15}));
        NIDaAlgorithm alg3 = new NIDaAlgorithm(new NodeID(new byte[]{30,31,32,33,14,15}));
        NIDaAlgorithm alg4 = new NIDaAlgorithm(new NodeID(new byte[]{40,41,42,43,14,15}));
        NIDaAlgorithm alg5 = new NIDaAlgorithm(new NodeID(new byte[]{50,51,52,53,14,15}));
        NIDaAlgorithm alg6 = new NIDaAlgorithm(new NodeID(new byte[]{60,61,62,63,14,15}));
        NIDaAlgorithm alg7 = new NIDaAlgorithm(new NodeID(new byte[]{70,71,72,73,14,15}));
        NIDaAlgorithm alg8 = new NIDaAlgorithm(new NodeID(new byte[]{80,81,82,83,14,15}));
        NIDaAlgorithm alg9 = new NIDaAlgorithm(new NodeID(new byte[]{90,91,92,93,14,15}));
        NIDaAlgorithm alg10 = new NIDaAlgorithm(new NodeID(new byte[]{100,101,102,103,14,15}));

        NIDaAlgorithm[] algs = new NIDaAlgorithm[]
                        {alg1, alg2, alg3, alg4, alg5, alg6, alg7, alg8, alg9, alg10};
                        
        // check to make sure seeds are different; condition of test, not test itself
        for (int i=0; i<algs.length; i++)
            for (int j=i+1; j<algs.length; j++)
                Assert.assertTrue("starting aliases should differ: "+i+","+j, 
                            algs[i].getNIDa()!=algs[j].getNIDa());


        NmraNetCanFrame f;        

        // run the startup
        int expectedCount = (6+1)*10; // count messages
        int count = priorityRunner(algs, 2*expectedCount);

        debug("tPS10 converges "+count);
        if (count != expectedCount)
            warn("tPS10 count "+count+" not expectedCount "+expectedCount); 
        
        // should all be done
        Assert.assertTrue("1 complete", alg1.isComplete());
        Assert.assertTrue("2 complete", alg2.isComplete());
        Assert.assertTrue("3 complete", alg3.isComplete());
        Assert.assertTrue("4 complete", alg4.isComplete());
        Assert.assertTrue("5 complete", alg5.isComplete());
        Assert.assertTrue("6 complete", alg6.isComplete());
        Assert.assertTrue("7 complete", alg7.isComplete());
        Assert.assertTrue("8 complete", alg8.isComplete());
        Assert.assertTrue("9 complete", alg9.isComplete());
        Assert.assertTrue("10 complete", alg10.isComplete());
        
    }
    
    /**
     * Test whether 10 nodes can converge using the CAN-priority simulator.
     * The simulates the case where nodes are sending as fast as possible, so
     * CAN arbitrates. Seeds are forced to be the same, but NodeIDs differ.
     * 
     */
    public void testPriorityCollisionStart10() {
        NubNIDaAlgorithm alg1 = new NubNIDaAlgorithm(new NodeID(new byte[]{10,11,12,13,14,15}));
        NubNIDaAlgorithm alg2 = new NubNIDaAlgorithm(new NodeID(new byte[]{10,11,12,13,14,25}));
        NubNIDaAlgorithm alg3 = new NubNIDaAlgorithm(new NodeID(new byte[]{10,11,12,13,24,15}));
        NubNIDaAlgorithm alg4 = new NubNIDaAlgorithm(new NodeID(new byte[]{10,11,12,23,14,15}));
        NubNIDaAlgorithm alg5 = new NubNIDaAlgorithm(new NodeID(new byte[]{10,21,22,13,14,15}));
        NubNIDaAlgorithm alg6 = new NubNIDaAlgorithm(new NodeID(new byte[]{20,11,12,13,14,15}));
        NubNIDaAlgorithm alg7 = new NubNIDaAlgorithm(new NodeID(new byte[]{10,11,12,23,24,15}));
        NubNIDaAlgorithm alg8 = new NubNIDaAlgorithm(new NodeID(new byte[]{10,11,22,23,24,15}));
        NubNIDaAlgorithm alg9 = new NubNIDaAlgorithm(new NodeID(new byte[]{10,21,22,23,24,15}));
        NubNIDaAlgorithm alg10 = new NubNIDaAlgorithm(new NodeID(new byte[]{20,21,22,23,24,15}));
        NmraNetCanFrame f;        

        NubNIDaAlgorithm[] algs = new NubNIDaAlgorithm[]
                        {alg1, alg2, alg3, alg4, alg5, alg6, alg7, alg8, alg9, alg10};
                        
        // set to same seed
        for (int i = 0; i<algs.length; i++)
            algs[i].forceSeedValue(0xAC01L);
            
        // this group of checks is just to make sure seeds are same
        // condition of test, not test itself
        Assert.assertEquals("starting aliases same", alg1.getNIDa(), alg2.getNIDa());
        Assert.assertEquals("starting aliases same", alg1.getNIDa(), alg3.getNIDa());
        Assert.assertEquals("starting aliases same", alg1.getNIDa(), alg4.getNIDa());
        Assert.assertEquals("starting aliases same", alg1.getNIDa(), alg5.getNIDa());
        Assert.assertEquals("starting aliases same", alg1.getNIDa(), alg6.getNIDa());
        Assert.assertEquals("starting aliases same", alg1.getNIDa(), alg7.getNIDa());
        Assert.assertEquals("starting aliases same", alg1.getNIDa(), alg8.getNIDa());
        Assert.assertEquals("starting aliases same", alg1.getNIDa(), alg9.getNIDa());
        Assert.assertEquals("starting aliases same", alg1.getNIDa(), alg10.getNIDa());
        
        // run the startup
        int expectedCount = 114; // messages (empirically determined, depends on NodeID bytes)
        int count = priorityRunner(algs, 2*expectedCount);

        debug("tPCS10 converges "+count);
        if (count != expectedCount)
            warn("tPCS10 count "+count+" not expectedCount "+expectedCount); 
        
        // should all be done
        Assert.assertTrue("1 complete", alg1.isComplete());
        Assert.assertTrue("2 complete", alg2.isComplete());
        Assert.assertTrue("3 complete", alg3.isComplete());
        Assert.assertTrue("4 complete", alg4.isComplete());
        Assert.assertTrue("5 complete", alg5.isComplete());
        Assert.assertTrue("6 complete", alg6.isComplete());
        Assert.assertTrue("7 complete", alg7.isComplete());
        Assert.assertTrue("8 complete", alg8.isComplete());
        Assert.assertTrue("9 complete", alg9.isComplete());
        Assert.assertTrue("10 complete", alg10.isComplete());
        
    }
    
    /**
     * Test whether 20 nodes with small serial numbers from each of 5 manufacturers
     * converge.   As a simplification, the serial numbers are taken to be
     * in order.
     * 
     */
    public void testPriorityMultiMsgSerialNumbers() {
        byte nNodes = 20;
        byte nMfgs =5;
        
        NubNIDaAlgorithm[] algs = new NubNIDaAlgorithm[nNodes*nMfgs];
        
        for (byte i=0; i<nNodes; i++) {
            for (byte j=0; j<nMfgs; j++) {
                NubNIDaAlgorithm a = new NubNIDaAlgorithm(
                                            new NodeID(
                                            new byte[]{1,1,j,0,0,i}
                                            ));
                algs[i*nMfgs+j] = a;                     
            }
        }

        
        // run the startup
        int expectedCount = 800; // messages (empirically determined, depends on NodeID bytes)
        int count = priorityRunner(algs, 2*expectedCount);

        debug("tPMNSN converges "+count);
        if (count != expectedCount)
            warn("tPMNSN count "+count+" not expectedCount "+expectedCount); 
        
        // should all be done
        for (int i=0; i<nNodes; i++) {
            for (int j=0; j<nMfgs; j++) {
                Assert.assertTrue("node "+i+" mfg "+j+" complete", algs[i*nMfgs+j].isComplete());
            }
        }
    }

    // Various scaffolding follows

    /**
     * Run a series of nodes, taking a frame from each in turn
     * and sending to others.
     */
    int sequentialRunner(NIDaAlgorithm[] algs, int nCycles) {
        NmraNetCanFrame f;
        for (int i = 0; i < nCycles; i++) {
            for (int j = 0; j < algs.length; j++) { // provides next message
                f = algs[j].nextFrame();
                for (int k = 0; k < algs.length; k++) { // distribute message
                    if (j != k)
                        algs[k].processFrame(f);
                }
            }
            // check complete
            boolean done = true;
            for (int j = 0; j < algs.length; j++) 
                if (!algs[j].isComplete()) {
                    done = false;
                    break;
                }
            // all complete
            if (done) return i+1;
        }
        return nCycles+1;
    }
    
    /**
     * Run a series of nodes, taking each output in priority order. This
     * simulates nodes sending very fast, so that CAN arbitrates.
     */
     int priorityRunner(NIDaAlgorithm[] algs, int nCycles) {
        NmraNetCanFrame[] q = new NmraNetCanFrame[algs.length];
        NmraNetCanFrame f;
        // start with 1st one each wants to send
        for (int j = 0; j < algs.length; j++)
            q[j] = algs[j].nextFrame();
        // loop, processing lowest header value each time
        for (int i = 0; i < nCycles; i++) {
            // find lowest header; null means that one is done
            int m = 0;
            for (int j = 1; j < algs.length; j++) { // provides next message
                if (q[m] == null) m = j;
                else if ( (q[j]!=null) && (q[j].getHeader()<q[m].getHeader())) m = j;
            }
            // process that one
            f = q[m];
            for (int j = 0; j < algs.length; j++)
                if (j!=m) 
                    algs[j].processFrame(f);
            // replace with next queued frame
            q[m] = algs[m].nextFrame();

            // check for done
            boolean done = true;
            for (int j = 0; j < algs.length; j++) 
                if (q[j] != null) {
                    done = false;
                    break;
                }
            // all complete
            if (done) return i+1;
        }
        return nCycles+1;
    }

    NIDaAlgorithm alg;
   
    // Local version of classes to allow forcing identical alias, state
    class NubNIDaAlgorithm extends NIDaAlgorithm {
        public NubNIDaAlgorithm(NodeID nid) { super(nid); this.nida = new NubNIDa(nid); }
        public void forceSeedValue(long seed) { ((NubNIDa)nida).forceSeedValue(seed); }
        public void forceAliasValue(int alias) { ((NubNIDa)nida).forceAliasValue(alias); }
    }
    class NubNIDa extends NIDa {
        public NubNIDa(NodeID nid) { super(nid);}
        public void forceSeedValue(long seed) { super.forceSeedValue(seed); }
        public void forceAliasValue(int alias) { super.forceAliasValue(alias); }
    }
   
    public void setUp() {
        alg = new NIDaAlgorithm(new NodeID(new byte[]{10,11,12,13,14,15}));
    }

    // Local micro-logger
    void warn(String msg) { System.out.println(msg); }
    void debug(String msg) { System.out.println(msg); }
    
    // from here down is testing infrastructure
    
    public NIDaAlgorithmTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NIDaAlgorithmTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NIDaAlgorithmTest.class);
        return suite;
    }
}
