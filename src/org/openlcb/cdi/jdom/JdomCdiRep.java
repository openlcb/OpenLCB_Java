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

    public java.util.List getSegments() {
        java.util.List list = root.getChildren("segment");
        java.util.ArrayList result = new java.util.ArrayList();
        for (int i = 0; i<list.size(); i++) {
            result.add(new Segment((Element)list.get(i)));
        }
        return result;
    }
    
    /**
     * Comment implementation of finding the list of contained Items
     */
    static class Nested {
        public java.util.List<CdiRep.Item> getItems() { return null; }
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
            java.util.List relations = map.getChildren("relation");
            for (int i = 0; i<relations.size(); i++) {
                list.add(((Element)relations.get(i)).getChild("property").getText());
            }
            return list;
        }
        
        Element map;
    }

    public static class Item implements CdiRep.Item {
        public String getName() { return null; }
        public String getDescription() { return null; }
        public Map getMap() {
            return new Map(e.getChild("map"));
        }
        
        Item(Element e) { this.e = e; }
        Element e;
    }
    public static class Group extends Nested implements CdiRep.Group {
        public String getName() { return null; }
        public String getDescription() { return null; }
        public Map getMap() {
            return new Map(e.getChild("map"));
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
        
        Int(Element e) { super(e); }
    }
    public static class Bit extends Item implements CdiRep.Bit {
        public boolean getDefault() { return false; }

        Bit(Element e) { super(e); }
    }

    Element root;
}
