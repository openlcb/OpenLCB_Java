package org.openlcb.cdi.jdom;

import org.jdom.Attribute;
import org.jdom.Element;
import org.openlcb.cdi.CdiRep;

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
        @Override
        public String getManufacturer() {
            Element c = id.getChild("manufacturer");
            if (c == null) return null;
            return c.getText();
        }
        @Override
        public String getModel() {
            Element c = id.getChild("model");
            if (c == null) return null;
            return c.getText();
        }
        @Override
        public String getHardwareVersion() {
            Element c = id.getChild("hardwareVersion");
            if (c == null) return null;
            return c.getText();
        }
        @Override
        public String getSoftwareVersion() {
            Element c = id.getChild("softwareVersion");
            if (c == null) return null;
            return c.getText();
        }

        @Override
        public Map getMap() {
            return new Map(id.getChild("map"));
        }
        
        Identification(Element id) {
            this.id = id;
        }
        Element id;
    }

    @Override
    public Identification getIdentification() {
        Element id = root.getChild("identification");
        if (id == null) return null;
        return new Identification(id);
    }

    @Override
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
                else if ("bit".equals(element.getName())) list.add(new BitRep(element));
                else if ("int".equals(element.getName())) list.add(new IntRep(element));
                else if ("eventid".equals(element.getName())) list.add(new EventID(element));
                else if ("string".equals(element.getName())) list.add(new StringRep(element));
            }
            return list;
        }
        
        Nested(Element e) { this.e = e; }
        Element e;
    }
    
    public static class Segment extends Nested implements CdiRep.Segment {
        Segment(Element segment) { super(segment); }
        
        @Override
        public int getSpace() {
            Attribute a = e.getAttribute("space");
            try {
                if (a == null) return 0;
                else return a.getIntValue();
            } catch (org.jdom.DataConversionException e1) { return 0; }
        }
        
        @Override
        public int getOrigin() {
            Attribute a = e.getAttribute("origin");
            try {
                if (a == null) return 0;
                else return a.getIntValue();
            } catch (org.jdom.DataConversionException e1) { return 0; }
        }
    }

    public static class Map implements CdiRep.Map {
        Map(Element map) {
            this.map = map;
        }
        
        @Override
        public String getEntry(String key) {
            java.util.List relations = map.getChildren("relation");
            for (int i = 0; i<relations.size(); i++) {
                if (key.equals(((Element)relations.get(i)).getChild("property").getText()) )
                    return ((Element)relations.get(i)).getChild("value").getText();
            }
            return null;
        }
        
        @Override
        public String getKey(String entry) {
            java.util.List relations = map.getChildren("relation");
            for (int i = 0; i<relations.size(); i++) {
                if (entry.equals(((Element)relations.get(i)).getChild("value").getText()) )
                    return ((Element)relations.get(i)).getChild("property").getText();
            }
            return null;
        }
        
        @Override
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
        
        @Override
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
        Item(Element e) { this.e = e; }
        Element e;

        @Override
        public String getName() { 
            Element d = e.getChild("name");
            if (d==null) return null;
            return d.getText();
        }
        
        @Override
        public String getDescription() { 
            Element d = e.getChild("description");
            if (d==null) return null;
            return d.getText();
        }
        
        @Override
        public Map getMap() {
            return new Map(e.getChild("map"));
        }
        
        @Override
        public int getOffset() {
            Attribute a = e.getAttribute("offset");
            try {
                if (a == null) return 0;
                else return a.getIntValue();
            } catch (org.jdom.DataConversionException e) { return 0; }
        }
    }

    public static class Group extends Nested implements CdiRep.Group {
        Group(Element e) { super(e); }
        
        @Override
        public int getReplication() {
            Attribute a = e.getAttribute("replication");
            try {
                if (a == null) return 0;
                else return a.getIntValue();
            } catch (org.jdom.DataConversionException e1) { return 0; }
        }
        
        @Override
        public int getOffset() {
            Attribute a = e.getAttribute("offset");
            try {
                if (a == null) return 0;
                else return a.getIntValue();
            } catch (org.jdom.DataConversionException e1) { return 0; }
        }
        
        @Override
        public String getRepName() {
            Element d = e.getChild("repname");
            if (d==null) return null;
            return d.getText();
       }
    }

    public static class EventID extends Item implements CdiRep.EventID {
        EventID(Element e) { super(e); }
    }
    public static class IntRep extends Item implements CdiRep.IntegerRep {
        IntRep(Element e) { super(e); }
                
        @Override
        public int getDefault() { return 0; }
        @Override
        public int getMin() { return 0; }
        @Override
        public int getMax() { return 0; }

        @Override
        public int getSize() { 
            Attribute a = e.getAttribute("size");
            try {
                if (a == null) return 1;
                else return a.getIntValue();
            } catch (org.jdom.DataConversionException e1) { return 0; }
        }
    }
    
    public static class BitRep extends Item implements CdiRep.BitRep {
        BitRep(Element e) { super(e); }
        
        @Override
        public boolean getDefault() { return false; }

        @Override
        public int getSize() { 
            Attribute a = e.getAttribute("size");
            try {
                if (a == null) return 1;
                else return a.getIntValue();
            } catch (org.jdom.DataConversionException e1) { return 0; }
        }
    }
    
    public static class StringRep extends Item implements CdiRep.StringRep {

        StringRep(Element e) { super(e); }
        
        @Override
        public int getSize() { 
            Attribute a = e.getAttribute("size");
            try {
                if (a == null) return 1;
                else return a.getIntValue();
            } catch (org.jdom.DataConversionException e1) { return 0; }
        }
    }

    Element root;
}
