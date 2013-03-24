package tools.jmri;

import org.openlcb.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import javax.swing.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Small stand-alone utility to read a JMRI
 * decoder definition file and emit a 
 * OpenLCB decoder CDI.
 *
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision: 2175 $
 */
public class DecoderDefnToCdi {
    
    Document doc;
    Element root;
    
    public void init() {
        root = new Element("cdi");
        root.setAttribute("noNamespaceSchemaLocation", // NOI18N
                "http://openlcb.org/trunk/specs/schema/cdi.xsd", // NOI18N
                org.jdom.Namespace.getNamespace("xsi", // NOI18N
                "http://www.w3.org/2001/XMLSchema-instance")); // NOI18N

        doc = new Document(root);
        
    }
    public void convert(Element inRoot) throws org.jdom.DataConversionException {
        addHeader(inRoot);
        addAcdiElement();
        addCommonCDI();
        addDefaultCVs();
        addVariables(inRoot);
    }
    
    public void addHeader(Element inRoot) {
        Element id = new Element("identification");
        
        String mfg = inRoot.getChild("decoder").getChild("family").getAttributeValue("mfg");
        id.addContent(new Element("manufacturer").addContent(mfg));
                
        String name = inRoot.getChild("decoder").getChild("family").getAttributeValue("name");
        id.addContent(new Element("model").addContent(name));
        id.addContent(new Element("hardwareVersion"));
        id.addContent(new Element("softwareVersion"));
        
        // add models in map, if any
        Element map = new Element("map");
        for (Object o : inRoot.getChild("decoder").getChild("family").getChildren("model")) {
            Element e = (Element) o;
            Element relation = new Element("relation");
            relation.addContent(new Element("property").addContent("Model"));
            relation.addContent(new Element("value").addContent(e.getAttributeValue("model")));
            map.addContent(relation);
        }
        id.addContent(map);
        
        root.addContent(id);
    }

    public void addCommonCDI() {
        Element segment = new Element("segment");
        segment.setAttribute("space","253");
        segment.setAttribute("origin","256");
        segment.addContent(new Element("name").addContent("DCC Controls"));
        segment.addContent(new Element("description").addContent("DCC-specific control items"));
        root.addContent(segment);
        
        Element i;
        Element map;
        Element relation;
        
        i = new Element("int");
        i.addContent(new Element("name").addContent("Programming Mode"));
        map = new Element("map");
        i.addContent(map);

        relation = new Element("relation");
        map.addContent(relation);
        relation.addContent(new Element("property").addContent("1"));
        relation.addContent(new Element("value").addContent("Register Mode"));

        relation = new Element("relation");
        map.addContent(relation);
        relation.addContent(new Element("property").addContent("2"));
        relation.addContent(new Element("value").addContent("Paged Mode"));

        relation = new Element("relation");
        map.addContent(relation);
        relation.addContent(new Element("property").addContent("4"));
        relation.addContent(new Element("value").addContent("Direct Bit Mode"));

        relation = new Element("relation");
        map.addContent(relation);
        relation.addContent(new Element("property").addContent("5"));
        relation.addContent(new Element("value").addContent("Direct Byte Mode"));

        segment.addContent(i);

        i = new Element("int");
        i.setAttribute("size", "2");
        i.addContent(new Element("name").addContent("DCC Address"));
        segment.addContent(i);

        i = new Element("int");
        i.addContent(new Element("name").addContent("Speed Steps"));
        segment.addContent(i);

        i = new Element("int");
        i.addContent(new Element("name").addContent("Single Index CV Address"));
        segment.addContent(i);

        i = new Element("int");
        i.setAttribute("size", "2");
        i.addContent(new Element("name").addContent("Double Index CV Addresses"));
        segment.addContent(i);

    }
      
    public void addAcdiElement() {
        root.addContent(new Element("acdi"));
    }
      
    public void addDefaultCVs() {
        Element segment = new Element("segment");
        segment.setAttribute("origin","4278190081");  // 255.0.0.1
        segment.setAttribute("space","253");
        
        Element group = new Element("group");
        group.setAttribute("replication","256");
        group.addContent(new Element("name").addContent("CVs"));
        group.addContent(new Element("description").addContent("Raw CV access"));
        group.addContent(new Element("repname").addContent("CV"));
        segment.addContent(group);

        Element intElement = new Element("int");
        intElement.setAttribute("size", "1");
        intElement.addContent(new Element("min").addContent("0"));
        intElement.addContent(new Element("max").addContent("255"));
        group.addContent(intElement);
                
        root.addContent(segment);
    }
    
    public void addVariables(Element inRoot) 
        throws org.jdom.DataConversionException {
        Element vs = inRoot.getChild("decoder").getChild("variables");

        Element segment = new Element("segment");
        segment.setAttribute("space","253");
        root.addContent(segment);
        
        Iterator it = vs.getDescendants();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Element) {
                Element e = (Element) o;
                if (e.getName().equals("variable")) {
                    // get common attributes
                    String comment = e.getAttributeValue("comment");
                    for (Object oc : e.getChildren("comment")) {
                        Element ec = (Element) oc;
                        if (ec.getAttributeValue("lang", "xml").equals("")) comment = ec.getText();
                    }
                    String name = e.getAttributeValue("label");
                    if (name.equals("")) name = e.getAttributeValue("item");
                    for (Object on : e.getChildren("label")) {
                        Element en = (Element) on;
                        if (en.getAttributeValue("lang", "xml").equals("")) name = en.getText();
                    }
                    
                    long cv = e.getAttribute("CV").getIntValue();
                    
                    // find subtype and process
                    Element type;
                    type = e.getChild("decVal");
                    if (type != null) {
                        segment.addContent(handleDecVal(type, cv, name, comment, e.getAttributeValue("mask")));
                        continue;
                    }
                    type = e.getChild("enumVal");
                    if (type != null) {
                        segment.addContent(handleEnumVal(type, cv, name, comment, e.getAttributeValue("mask")));
                        continue;
                    }
                    type = e.getChild("shortAddressVal");
                    if (type != null) {
                        segment.addContent(handleShortAddressVal(type, cv, name, comment));
                        continue;
                    }
                    type = e.getChild("longAddressVal");
                    if (type != null) {
                        segment.addContent(handleLongAddressVal(type, cv, name, comment));
                        continue;
                    }
                }
            }
        } 
    }
    
    public Element handleDecVal(Element type, long cv, String name, String comment, String mask) {
        Element r;
        if (mask != null && !mask.equals("")) {
            r = new Element("bit");
            r.setAttribute("size", "8");
            r.setAttribute("mask", maskToInt(mask));
        } else {
            r = new Element("int");
        }
        r.setAttribute("origin", ""+(4278190080L+cv));
        r.addContent(new Element("name").addContent(name));
        if (comment != null && !comment.equals("")) r.addContent(new Element("description").addContent(comment));
        return r;
    }
    
    public Element handleEnumVal(Element type, long cv, String name, String comment, String mask) 
            throws org.jdom.DataConversionException {
        Element r;
        if (mask != null && !mask.equals("")) {
            r = new Element("bit");
            r.setAttribute("size", "8");
            r.setAttribute("mask", maskToInt(mask));
        } else {
            r = new Element("int");
        }
        r.setAttribute("origin", ""+(4278190080L+cv));
        r.addContent(new Element("name").addContent(name));
        if (comment != null && !comment.equals("")) r.addContent(new Element("description").addContent(comment));
        
        Element map = new Element("map");
        int counter = 0;
        r.addContent(map);
        for (Object c : type.getChildren("enumChoice")) {
            Element choice = (Element)c;

            if (choice.getAttribute("value")!=null) {
                counter = choice.getAttribute("value").getIntValue();
            }
            
            String cname = choice.getAttributeValue("choice");
            for (Object oc : choice.getChildren("choice")) {
                Element ec = (Element) oc;
                if (ec.getAttributeValue("lang", "xml").equals("")) cname = ec.getText();
            }

            Element relation = new Element("relation");
            map.addContent(relation);
            relation.addContent(new Element("property").addContent(""+counter));
            relation.addContent(new Element("value").addContent(cname));
            counter++;
        }
        return r;
    }
    
    public Element handleShortAddressVal(Element type, long cv, String name, String comment) {
        Element r = new Element("int");
        r.setAttribute("origin", ""+(4278190080L+cv));
        r.addContent(new Element("name").addContent(name));
        if (comment != null && !comment.equals("")) r.addContent(new Element("description").addContent(comment));
        return r;
    }
    
    public Element handleLongAddressVal(Element type, long cv, String name, String comment) {
        Element r = new Element("int");
        r.setAttribute("origin", ""+(4278190080L+cv));
        r.setAttribute("size", "2");
        r.addContent(new Element("name").addContent(name));
        if (comment != null && !comment.equals("")) r.addContent(new Element("description").addContent(comment));
        return r;
    }
    
    public String maskToInt(String mask) {
        int value = 0;
        int bitval = 1;
        while (mask.length()>0) {
            if (mask.substring(mask.length()-1, mask.length()).equals("V")) value = value+bitval;
            mask = mask.substring(0, mask.length()-1);
            bitval = bitval*2;
        }
        return ""+value;
    }
    
    public Element getFileRoot(String filename) {
        
        // find file & load file
        Element root = null;
        
        boolean verify = false;
        
        try {
            SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", verify);
            
            builder.setFeature("http://apache.org/xml/features/xinclude", true);
            builder.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
            builder.setFeature("http://apache.org/xml/features/allow-java-encodings", true);
            builder.setFeature("http://apache.org/xml/features/validation/schema", verify);
            builder.setFeature("http://apache.org/xml/features/validation/schema-full-checking", verify);
            builder.setFeature("http://xml.org/sax/features/namespaces", true);

            Document doc = builder.build(new BufferedInputStream(new FileInputStream(new File(filename))));
            root = doc.getRootElement();
        } catch (Exception e) { System.out.println("While reading file: "+e);}
        return root;
    }
    
    void prettyPrint(Element element) {
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        System.out.println(out.outputString(element));
    }
    
    // Main entry point
    static public void main(String[] args) throws org.jdom.DataConversionException {
        DecoderDefnToCdi c= new DecoderDefnToCdi();
        c.init();
        
        String filename = "/Users/jake/JMRI/projects/HEAD/xml/decoders/SoundTraxx_Tsu_Steam.xml";
        Element inRoot = c.getFileRoot(filename);
        
        c.convert(inRoot);
        
        c.prettyPrint(c.root);      
    }

}
