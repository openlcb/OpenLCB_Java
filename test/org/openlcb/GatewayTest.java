package org.openlcb;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class GatewayTest {
    protected Gateway getGateway() {
        return new Gateway();
    }

    @Test    
    public void testCtor() {
        Assert.assertNotNull(getGateway());
    }

    @Test    
    public void testGet() {
        g = getGateway();
        Assert.assertNotNull(g.getEastConnection());
        Assert.assertNotNull(g.getWestConnection());
    }
    
    protected boolean resultE;
    protected boolean resultW;
    protected Gateway g;
    protected TestListener tE;
    protected TestListener tW;
    protected Connection cE;
    protected Connection cW;
    
    abstract class TestListener extends AbstractConnection {
        @Override
        public void put(Message m, Connection n) {
            setResult();
        }
        
        abstract void setResult();
    }

    protected void buildGateway() {
        g = getGateway();
        resultE = false;
        resultW = false;
        tE = new TestListener() {
            @Override
            void setResult() { resultE = true;}
        };
        tW = new TestListener() {
            @Override
            void setResult() { resultW = true;}
        };
        g.registerWest(tW);
        g.registerEast(tE);
        cE = g.getEastConnection();
        cW = g.getWestConnection();
    }

    @Test    
    public void testEastToWest() {
        buildGateway();
        Message m = new Message(new NodeID(new byte[]{1,2,3,4,5,6}))
            {@Override
            public int getMTI() {return 0; }};

        cE.put(m, tE);
        
        checkMovedEastToWestOnly();
    }
    
    protected void checkMovedEastToWestOnly() {
        Assert.assertTrue(!resultE);
        Assert.assertTrue(resultW);
        resultE = false;
        resultW = false;
    }
    
    @Test    
    public void testWestToEast() {
        buildGateway();
        Message m = new Message(new NodeID(new byte[]{1,2,3,4,5,6}))
            {@Override
            public int getMTI() {return 0; }};

        cW.put(m, tW);
        
        checkMovedWestToEastOnly();
    }
    
    protected void checkMovedWestToEastOnly() {
        Assert.assertTrue(resultE);
        Assert.assertTrue(!resultW);
        resultE = false;
        resultW = false;
    }
}
