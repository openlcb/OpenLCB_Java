package org.openlcb;

import org.junit.*;

/**
 * @author  Bob Jacobsen   Copyright 2012
 */
public class SimpleNodeIdentTest  {
    NodeID nid1 = new NodeID(new byte[]{1,3,3,4,5,6});
    NodeID nid2 = new NodeID(new byte[]{2,3,3,4,5,6});
 
    @Test   
    public void testCtor() {
        new SimpleNodeIdent(new NodeID(new byte[]{1,3,3,4,5,6}),
                            new NodeID(new byte[]{1,3,3,4,5,7}));
    }
        
    @Test   
    public void testCreationFromMessage() {
        SimpleNodeIdent id = new SimpleNodeIdent(
            new SimpleNodeIdentInfoReplyMessage(
                new NodeID(new byte[]{1,3,3,4,5,6}), nid2, 
                new byte[]{1,'a','b','c',0,'z','y','x'}));
                
        Assert.assertEquals("abc", id.getMfgName());
        Assert.assertFalse("not complete", id.contentComplete());
    }
    
    @Test   
    public void testCreationFromTwoMessages() {
        SimpleNodeIdent id = new SimpleNodeIdent(
            new SimpleNodeIdentInfoReplyMessage(
                new NodeID(new byte[]{1,2,3,4,5,6}), nid2, 
                new byte[]{1,'a','b','c',0,'z','y','x'}));
        id.addMsg(
            new SimpleNodeIdentInfoReplyMessage(
                new NodeID(new byte[]{1,2,3,4,5,6}), nid2, 
                new byte[]{0,'A','B','C',0,'Z',0}));

        Assert.assertEquals("abc", id.getMfgName());
        Assert.assertEquals("zyx", id.getModelName());
        Assert.assertFalse("not complete", id.contentComplete());
    }

    @Test   
    public void testSpannedCreationFromTwoMessages() {
        SimpleNodeIdent id = new SimpleNodeIdent(
            new SimpleNodeIdentInfoReplyMessage(
                new NodeID(new byte[]{1,2,3,4,5,6}), nid2, 
                new byte[]{1,'a','b','c','d','e','f'}));
        id.addMsg(
            new SimpleNodeIdentInfoReplyMessage(
                new NodeID(new byte[]{1,2,3,4,5,6}), nid2, 
                new byte[]{'g',0,'A','B',0,'Z',0}));

        Assert.assertEquals("abcdefg", id.getMfgName());
        Assert.assertEquals("AB", id.getModelName());
        Assert.assertEquals("Z", id.getHardwareVersion());
        Assert.assertFalse("not complete", id.contentComplete());
    }

    @Test   
    public void testCreationWithUserPart() {
        SimpleNodeIdent id = new SimpleNodeIdent(
            new SimpleNodeIdentInfoReplyMessage(
                new NodeID(new byte[]{1,2,3,4,5,6}), nid2, 
                new byte[]{1,'a','b',0,'1',0,'2',0,'A',0,1,'u','s',0,'3','4',0}));

        Assert.assertEquals("ab", id.getMfgName());
        Assert.assertEquals("1", id.getModelName());
        Assert.assertEquals("2", id.getHardwareVersion());
        Assert.assertEquals("A", id.getSoftwareVersion());
        Assert.assertEquals("us", id.getUserName());
        Assert.assertEquals("34", id.getUserDesc());
        Assert.assertTrue("complete", id.contentComplete());
    }

    @Test   
    public void testOverrunMessage() {
        SimpleNodeIdent id = new SimpleNodeIdent(
            new SimpleNodeIdentInfoReplyMessage(
                new NodeID(new byte[]{1,2,3,4,5,6}), nid2, 
                new byte[]{1,'a','b',0,'1',0,'2',0,'A',0,1,'u','s',0,'3','4',0}));

        Assert.assertEquals("ab", id.getMfgName());
        Assert.assertEquals("1", id.getModelName());
        Assert.assertEquals("2", id.getHardwareVersion());
        Assert.assertEquals("A", id.getSoftwareVersion());
        Assert.assertEquals("us", id.getUserName());
        Assert.assertEquals("34", id.getUserDesc());
        Assert.assertTrue("complete", id.contentComplete());

        id.addMsg(
            new SimpleNodeIdentInfoReplyMessage(
                new NodeID(new byte[]{1,2,3,4,5,6}), nid2, 
                new byte[]{1,'s','t','a','r','t','s',0}));
        Assert.assertEquals("starts", id.getMfgName());
    }

}
