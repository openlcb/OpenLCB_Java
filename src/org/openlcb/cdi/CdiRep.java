package org.openlcb.cdi;

/**
 * Interface representing CDI information.
 * <p>
 * Generally, the CDI XML will be read into an
 * object implementing this interface.
 *
 * @author  Bob Jacobsen   Copyright 2011
 * @version $Revision: -1 $
 */
public interface CdiRep {

    public static interface Identification {
        public String getManufacturer();
        public String getModel();
        public String getHardwareVersion();
        public String getSoftwareVersion();
        public Map getMap();
    }

    public Identification getIdentification();
    
    public java.util.List<Segment> getSegments();
    
    public static interface Segment {
        public int getSpace();
        public java.util.List<Item> getItems();

        public String getName();
        public String getDescription();
        public Map getMap();
    }
    
    public static interface Item {
        public String getName();
        public String getDescription();
        public Map getMap();
    }

    public static interface Group extends Item {
        public java.util.List<Item> getItems();
    }

    public static interface Map {
        public String getEntry(String key);
        public java.util.List<String> getKeys();
        public java.util.List<String> getValues();
    }

    public static interface EventID extends Item {
    }
    public static interface Int extends Item {
        public int getDefault();
        public int getMin();
        public int getMax();
    }
    public static interface Bit extends Item {
        public boolean getDefault();
    }

}
