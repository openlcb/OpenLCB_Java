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
        public int getOrigin();
        public java.util.List<Item> getItems();

        public String getName();
        public String getDescription();
        public Map getMap();
        public int getIndexInParent();
    }
    
    public static interface Item {
        public String getName();
        public String getDescription();
        public Map getMap();
        public int getOffset();
        public int getIndexInParent();
    }

    public static interface Group extends Item {
        public java.util.List<Item> getItems();
        public int getReplication();
        public String getRepName();
    }

    public static interface Map {
        /**
         * Converts stored values to visible values.
         * @param key a network-stored value, usually a decimal rendered number.
         * @return the user-visible string explanation that should be displayed.
         */
        public String getEntry(String key);

        /**
         * Converts visible values to stored values.
         * @param entry the visible value that was selected by the user
         * @return the (string representation) of the value to store to the node's memory space
         * (usually a decimal formatted number).
         */
        public String getKey(String entry);

        /**
         * Gets valid values (returned array is parallel to {@link #getValues()}
         * )
         *
         * @return a list of all valid stored values (usually a list of decimal
         * formatted integers).
         */
        public java.util.List<String> getKeys();

        /**
         * Gets all user-visible string explanations (returned array is parallel
         * to {@link #getKeys()} )
         *
         * @return a list of all user-visible values.
         */
        public java.util.List<String> getValues();
    }

    public static interface EventID extends Item {
    }
    public static interface IntegerRep extends Item {
        public int getDefault();
        public int getMin();
        public int getMax();

        public int getSize();
    }
    public static interface BitRep extends Item {
        public boolean getDefault();

        public int getSize();
    }
    public static interface StringRep extends Item {  // "String" causes too many name conflicts

        public int getSize();
    }

}
