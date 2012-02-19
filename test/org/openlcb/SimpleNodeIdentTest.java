package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class SimpleNodeIdentTest extends TestCase {
    public void testCtor() {
        new SimpleNodeIdent();
    }
        
    public void testCreationFromMessage() {
        SimpleNodeIdent id = new SimpleNodeIdent(
            new SimpleNodeIdentInfoReplyMessage(
                new NodeID(new byte[]{1,3,3,4,5,6}), 
                new byte[]{1,'a','b','c',0,'z','y','x'}));
                
        Assert.assertEquals("abc", id.getMfgName());
    }
    
    public void testCreationFromTwoMessages() {
        SimpleNodeIdent id = new SimpleNodeIdent(
            new SimpleNodeIdentInfoReplyMessage(
                new NodeID(new byte[]{1,2,3,4,5,6}), 
                new byte[]{1,'a','b','c',0,'z','y','x'}));
        id.addMsg(
            new SimpleNodeIdentInfoReplyMessage(
                new NodeID(new byte[]{1,2,3,4,5,6}), 
                new byte[]{0,'A','B','C',0,'Z',0}));

        Assert.assertEquals("abc", id.getMfgName());
        Assert.assertEquals("zyx", id.getModelName());
    }

    public void testSpannedCreationFromTwoMessages() {
        SimpleNodeIdent id = new SimpleNodeIdent(
            new SimpleNodeIdentInfoReplyMessage(
                new NodeID(new byte[]{1,2,3,4,5,6}), 
                new byte[]{1,'a','b','c','d','e','f'}));
        id.addMsg(
            new SimpleNodeIdentInfoReplyMessage(
                new NodeID(new byte[]{1,2,3,4,5,6}), 
                new byte[]{'g',0,'A','B',0,'Z',0}));

        Assert.assertEquals("abcdefg", id.getMfgName());
        Assert.assertEquals("AB", id.getModelName());
        Assert.assertEquals("Z", id.getVersion());
    }

    // from here down is testing infrastructure
    
    public SimpleNodeIdentTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SimpleNodeIdentTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SimpleNodeIdentTest.class);
        return suite;
    }
}
