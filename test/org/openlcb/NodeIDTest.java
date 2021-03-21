package org.openlcb;

import org.junit.Assert;
import org.junit.Test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class NodeIDTest {
    @SuppressFBWarnings(value="NP_NONNULL_PARAM_VIOLATION",
            justification="Null passed for non null parameter")
    @Test
    public void testNullArg() {
        try {
            new NodeID((byte[]) null);
        } catch (IllegalArgumentException e) {
            return;
        }
        Assert.fail("Should have thrown exception");
    }

    @Test
    public void testTooLongArg() {
        // shouldn't throw, just takes 1st part
        NodeID e1 = new NodeID(new byte[]{1,2,3,4,5,6,7});
        NodeID e2 = new NodeID(new byte[]{1,2,3,4,5,6});
        Assert.assertEquals(e1, e2);
    }

    @Test
    public void testTooShortArg() {
        try {
            new NodeID(new byte[]{1,2,3,4,5});
        } catch (IllegalArgumentException e) {
            return;
        }
        Assert.fail("Should have thrown exception");
    }
    
    @Test
    public void testOKArg() {
        NodeID e = new NodeID(new byte[]{1,2,3,4,5,6});
        Assert.assertNotNull(e);
    }
    
    @SuppressFBWarnings(value="NP_NONNULL_PARAM_VIOLATION",
            justification="Null passed for non null parameter")
    @Test
    public void testNullStringArg() {
        try {
            new NodeID((String) null);
        } catch (IllegalArgumentException e) {
            return;
        }
        Assert.fail("Should have thrown exception");
    }

    @Test
    public void testTooLongStringArg() {
        // shouldn't throw, just takes 1st part
        NodeID e1 = new NodeID("1.2.3.4.5.6.7");
        NodeID e2 = new NodeID(new byte[]{1,2,3,4,5,6});
        Assert.assertEquals(e1, e2);
    }

    @Test
    public void testTooShortStringArg() {
        try {
            new NodeID("1.2.3.4.5");
        } catch (IllegalArgumentException e) { return; }
        Assert.fail("Should have thrown exception");
    }
    
    @Test
    public void testOKStringArg() {
        NodeID e = new NodeID("1.2.3.4.5.6");
        Assert.assertNotNull(e);
    }
    
    @Test
    public void testEqualsSame() {
        NodeID e1 = new NodeID(new byte[]{1,2,3,4,5,6});
        NodeID e2 = new NodeID(new byte[]{1,2,3,4,5,6});
        Assert.assertTrue(e1.equals(e2));
    }
    
    @Test
    public void testEqualsSameString() {
        NodeID e1 = new NodeID(new byte[]{1,2,3,4,5,6});
        NodeID e2 = new NodeID("1.2.3.4.5.6");
        Assert.assertTrue(e1.equals(e2));
    }
    
    @Test
    public void testEqualsCastSame() {
        Object e1 = new NodeID(new byte[]{1,2,3,4,5,6});
        NodeID e2 = new NodeID(new byte[]{1,2,3,4,5,6});
        Assert.assertTrue(e1.equals(e2));
    }
    
    @Test
    public void testEqualsSelf() {
        NodeID e1 = new NodeID(new byte[]{1,2,3,4,5,6});
        Assert.assertTrue(e1.equals(e1));
    }
    
    @Test
    public void testEqualsCastSelf() {
        NodeID e1 = new NodeID(new byte[]{1,2,3,4,5,6});
        Object e2 = e1;
        Assert.assertTrue(e1.equals(e2));
    }
    
    @Test
    public void testNotEquals() {
        NodeID e1 = new NodeID(new byte[]{1,2,3,4,5,6});
        NodeID e2 = new NodeID(new byte[]{1,3,3,4,5,6});
        Assert.assertTrue(!e1.equals(e2));
    }
    
    @Test
    public void testNotEqualsOtherType() {
        NodeID e1 = new NodeID(new byte[]{1,2,3,4,5,6});
        Object e2 = new Object();
        Assert.assertTrue(!e1.equals(e2));
    }

    @SuppressFBWarnings(value="EC_UNRELATED_TYPES",
            justification="Call to equals with unrelated types")
    @Test
    public void testNodesAreNotEvents() {
        NodeID e1 = new NodeID(new byte[]{1,2,3,4,5,6});
        EventID e2 = new EventID(new byte[]{1,0,0,0,0,0,1,0});
        Assert.assertTrue(!e1.equals(e2));
    }

    @Test
    public void testGetContents() {
        NodeID e1 = new NodeID(new byte[]{1,2,3,4,5,6});
        byte[] contents;
        
        contents = e1.getContents();
        
        Assert.assertTrue(contents.length==6);
        Assert.assertTrue(contents[0] == 1);
        Assert.assertTrue(contents[1] == 2);
        Assert.assertTrue(contents[2] == 3);
        Assert.assertTrue(contents[3] == 4);
        Assert.assertTrue(contents[4] == 5);
        Assert.assertTrue(contents[5] == 6);
    }

    @Test
    public void testImmutable() {
        NodeID e1 = new NodeID(new byte[]{1,2,3,4,5,6});
        byte[] contents;
        
        contents = e1.getContents();
        contents[5] = 100;
        contents = e1.getContents();
        
        Assert.assertTrue(contents.length==6);
        Assert.assertTrue(contents[0] == 1);
        Assert.assertTrue(contents[1] == 2);
        Assert.assertTrue(contents[2] == 3);
        Assert.assertTrue(contents[3] == 4);
        Assert.assertTrue(contents[4] == 5);
        Assert.assertTrue(contents[5] == 6);
    }

    @Test
    public void testOutputFormat() {
        NodeID e1 = new NodeID(new byte[]{1,0x10,0x13,0x0D,(byte)0xD0,(byte)0xAB});
        Assert.assertEquals("01.10.13.0D.D0.AB", e1.toString());
    }
}
