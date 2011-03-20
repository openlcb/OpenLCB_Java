package org.openlcb.cdi.jdom;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom.*;

import org.openlcb.cdi.CdiRep;

/**
 * @author  Bob Jacobsen   Copyright 2011
 * @version $Revision: 34 $
 */
public class JdomCdiRepTest extends TestCase {
    
    public void testCtor() {
        new JdomCdiRep(null);
    }
    
    public void testGetIdent() {
        CdiRep rep = new JdomCdiRep(SampleFactory.getBasicSample());
        
        CdiRep.Identification id = rep.getIdentification();
        Assert.assertNotNull(id);
        
        Assert.assertEquals("mfg", "OpenLCB Prototype", id.getManufacturer());
        Assert.assertEquals("model", "Basic sketch", id.getModel());
        Assert.assertEquals("hardware", "Arduino (any)", id.getHardwareVersion());
        Assert.assertEquals("software", "0.4", id.getSoftwareVersion());
    }
    
    public void testMap() {
        Element e = new Element("map")
                    .addContent(new Element("relation")
                        .addContent(new Element("property").addContent("prop1"))
                        .addContent(new Element("value").addContent("val1"))
                    )
                    .addContent(new Element("relation")
                        .addContent(new Element("property").addContent("prop2"))
                        .addContent(new Element("value").addContent("val2"))
                    )
                    .addContent(new Element("relation")
                        .addContent(new Element("property").addContent("prop3"))
                        .addContent(new Element("value").addContent("val3"))
                    )
            ;
     
        JdomCdiRep.Map map = new JdomCdiRep.Map(e);
        
        Assert.assertEquals("prop1 value","val1",map.getEntry("prop1"));
        Assert.assertEquals("prop2 value","val2",map.getEntry("prop2"));
        Assert.assertEquals("prop3 value","val3",map.getEntry("prop3"));

        Assert.assertEquals("non-existant value",null,map.getEntry("propX"));
        
        java.util.List list = map.getKeys();
        Assert.assertNotNull(list);
        Assert.assertEquals("num keys", 3, list.size());
        Assert.assertEquals("key1", "prop1", list.get(0));
        Assert.assertEquals("key2", "prop2", list.get(1));
        Assert.assertEquals("key3", "prop3", list.get(2));
    }
    
    public void testSegments() {
        CdiRep rep = new JdomCdiRep(SampleFactory.getBasicSample());
        
        java.util.List list = rep.getSegments();
        
        Assert.assertEquals("len", 2, list.size());
        
        CdiRep.Segment segment;
        segment = (CdiRep.Segment)list.get(0);
        Assert.assertNotNull(segment);
        Assert.assertEquals("space", 0, segment.getSpace());
        Assert.assertEquals("name", "Content", segment.getName());
        Assert.assertEquals("description", "Variables for controlling general operation", segment.getDescription());
        
        segment = (CdiRep.Segment)list.get(1);
        Assert.assertNotNull(segment);
        Assert.assertEquals("space", 1, segment.getSpace());
        Assert.assertEquals("name", "Resets", segment.getName());
        Assert.assertEquals("description", "Memory locations controlling resets", segment.getDescription());
    }
        
    public void testGroupsInSegments() {
        CdiRep rep = new JdomCdiRep(SampleFactory.getBasicSample());
        
        java.util.List list = rep.getSegments();
        
        Assert.assertEquals("len", 2, list.size());
        
        CdiRep.Segment segment;
        segment = (CdiRep.Segment)list.get(0);
        Assert.assertNotNull(segment);
        
        java.util.List<CdiRep.Item> items = segment.getItems();
        Assert.assertNotNull(items);
        Assert.assertEquals("contents length",4,items.size());
    }

    public JdomCdiRepTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JdomCdiRepTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JdomCdiRepTest.class);

        return suite;
    }
}
