package org.openlcb.implementations.throttle;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.openlcb.*;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class FdiParserTest {

    @Test
    public void testCTor() {
        Element e = new Element("root");
        Element segment = new Element("segment");
        segment.setAttribute("space","5");
        segment.setAttribute("origin","0");
        e.addContent(segment);
        Element group = new Element("group");
        group.setAttribute("offset","0");
        Element fm = new Element("function");
        Element fmn = new Element("name");
        fmn.addContent("F1");
        fm.addContent(fmn);
        fm.setAttribute("size","1");
        fm.setAttribute("kind","momentary");
        group.addContent(fm);
        Element ft = new Element("function");
        Element ftn = new Element("name");
        ftn.addContent("F2");
        ft.addContent(ftn);
        ft.setAttribute("size","1");
        ft.setAttribute("kind","toggle");
        group.addContent(ft);
        Element fa = new Element("function");
        Element fan = new Element("name");
        fan.addContent("F3");
        fa.addContent(fan);
        fa.setAttribute("size","1");
        fa.setAttribute("kind","analog");
        group.addContent(fa);
        e.addContent(group);
        FdiParser t = new FdiParser(e);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testReadFromFile() throws Exception {
        FileReader r = new FileReader("test/org/openlcb/implementations/throttle/FdiTestFile.xml");
        Element e = org.openlcb.cdi.jdom.XmlHelper.parseXmlFromReader(r);
        FdiParser t = new FdiParser(e);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

}
