package org.openlcb.cdi.jdom;

import org.openlcb.cdi.CdiRep;

import org.jdom.*;

/**
 * Implement the CdiRep interface using 
 * JDOM for reading the underlying XML.
 *
 * @author  Bob Jacobsen   Copyright 2011
 * @version $Revision: -1 $
 */
public class JdomCdiRep implements CdiRep {

    public JdomCdiRep(Element root) {
        this.root = root;
    }
    
    public static class Identification implements CdiRep.Identification {    
        public String getManufacturer() {
            Element c = id.getChild("manufacturer");
            if (c == null) return null;
            return c.getText();
        }
        public String getModel() {
            Element c = id.getChild("model");
            if (c == null) return null;
            return c.getText();
        }
        public String getHardwareVersion() {
            Element c = id.getChild("hardwareVersion");
            if (c == null) return null;
            return c.getText();
        }
        public String getSoftwareVersion() {
            Element c = id.getChild("softwareVersion");
            if (c == null) return null;
            return c.getText();
        }

        public Map getMap() {
            return new Map(id.getChild("map"));
        }
        
        Identification(Element id) {
            this.id = id;
        }
        Element id;
    }

    public Identification getIdentification() {
        Element id = root.getChild("identification");
        if (id == null) return null;
        return new Identification(id);
    }

    public java.util.List<CdiRep.Segment> getSegments() {
        java.util.List list = root.getChildren("segment");
        java.util.ArrayList<CdiRep.Segment> result = new java.util.ArrayList<CdiRep.Segment>();
        for (int i = 0; i<list.size(); i++) {
            result.add(new Segment((Element)list.get(i)));
        }
        return result;
    }
    
    /**
     * Comment implementation of finding the list of contained Items
     */
    static class Nested {
        public String getName() { 
            Element d = e.getChild("name");
            if (d==null) return null;
            return d.getText();
        }
        public String getDescription() { 
            Element d = e.getChild("description");
            if (d==null) return null;
            return d.getText();
        }
        public Map getMap() {
            return new Map(e.getChild("map"));
        }
        
        public java.util.List<CdiRep.Item> getItems() {
            java.util.List<CdiRep.Item> list = new java.util.ArrayList<CdiRep.Item>();
            if (e == null) return list;
            java.util.List elements = e.getChildren();
            for (int i = 0; i<elements.size(); i++) {
                // some elements aren't contained items
                Element element = (Element)elements.get(i);
                if ("group".equals(element.getName())) list.add(new Group(element));
                else if ("bit".equals(element.getName())) list.add(new Bit(element));
                else if ("int".equals(element.getName())) list.add(new Int(element));
                else if ("eventid".equals(element.getName())) list.add(new EventID(element));
                else if ("string".equals(element.getName())) list.add(new CdiString(element));
            }
            return list;
        }
        
        Nested(Element e) { this.e = e; }
        Element e;
    }
    
    public static class Segment extends Nested implements CdiRep.Segment {
        public int getSpace() {
            Attribute a = e.getAttribute("space");
            try {
                if (a == null) return 0;
                else return a.getIntValue();
            } catch (org.jdom.DataConversionException e) { return 0; }
        }
        
        Segment(Element segment) { super(segment); }
    }

    public static class Map implements CdiRep.Map {
        Map(Element map) {
            this.map = map;
        }
        
        public String getEntry(String key) {
            java.util.List relations = map.getChildren("relation");
            for (int i = 0; i<relations.size(); i++) {
                if (key.equals(((Element)relations.get(i)).getChild("property").getText()) )
                    return ((Element)relations.get(i)).getChild("value").getText();
            }
            return null;
        }
        
        public java.util.List<String> getKeys() {
            java.util.ArrayList<String> list = new java.util.ArrayList<String>();
            if (map == null) return list;
            java.util.List relations = map.getChildren("relation");
            if (relations == null) return list;
            for (int i = 0; i<relations.size(); i++) {
                list.add(((Element)relations.get(i)).getChild("property").getText());
            }
            return list;
        }
        public java.util.List<String> getValues() {
            java.util.ArrayList<String> list = new java.util.ArrayList<String>();
            if (map == null) return list;
            java.util.List relations = map.getChildren("relation");
            if (relations == null) return list;
            for (int i = 0; i<relations.size(); i++) {
                list.add(((Element)relations.get(i)).getChild("value").getText());
            }
            return list;
        }
        
        Element map;
    }

    public static class Item implements CdiRep.Item {
        public String getName() { 
            Element d = e.getChild("name");
            if (d==null) return null;
            return d.getText();
        }
        public String getDescription() { 
            Element d = e.getChild("description");
            if (d==null) return null;
            return d.getText();
        }
        public Map getMap() {
            return new Map(e.getChild("map"));
        }
        
        Item(Element e) { this.e = e; }
        Element e;
    }
    public static class Group extends Nested implements CdiRep.Group {
        public int getReplication() {
            Attribute a = e.getAttribute("replication");
            try {
                if (a == null) return 0;
                else return a.getIntValue();
            } catch (org.jdom.DataConversionException e) { return 0; }
        }

        Group(Element e) { super(e); }
    }
    public static class EventID extends Item implements CdiRep.EventID {
        EventID(Element e) { super(e); }
    }
    public static class Int extends Item implements CdiRep.Int {
        public int getDefault() { return 0; }
        public int getMin() { return 0; }
        public int getMax() { return 0; }

        public int getSize() { 
            Attribute a = e.getAttribute("size");
            try {
                if (a == null) return 1;
                else return a.getIntValue();
            } catch (org.jdom.DataConversionException e) { return 0; }
        }
        
        Int(Element e) { super(e); }
    }
    public static class Bit extends Item implements CdiRep.Bit {
        public boolean getDefault() { return false; }

        public int getSize() { 
            Attribute a = e.getAttribute("size");
            try {
                if (a == null) return 1;
                else return a.getIntValue();
            } catch (org.jdom.DataConversionException e) { return 0; }
        }

        Bit(Element e) { super(e); }
    }
    public static class CdiString extends Item implements CdiRep.CdiString {

        public int getSize() { 
            Attribute a = e.getAttribute("size");
            try {
                if (a == null) return 1;
                else return a.getIntValue();
            } catch (org.jdom.DataConversionException e) { return 0; }
        }

        CdiString(Element e) { super(e); }
    }

    Element root;
}
