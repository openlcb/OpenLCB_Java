package org.openlcb.cdi;

/**
 * Interface representing CDI information.
 * <p>
 * Generally, the CDI XML will be read into an
 * object implementing this interface.
 *
 * @author  Bob Jacobsen   Copyright 2011
 */
public interface CdiRep {

    public static interface Identification {
        public String getManufacturer();
        public String getModel();
        public String getHardwareVersion();
        public String getSoftwareVersion();
        public String getLinkText();
        public String getLinkURL();
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
        public String getLinkText();
        public String getLinkURL();
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
        public String getLinkText();
        public String getLinkURL();
        public String getRepName(int index, int replications);
        public boolean isHideable();
        public boolean isHidden();
        public boolean isReadOnly();
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
        
        /**
         * Add an item to the map.  Useful if e.g. a non-mapped
         * value is found in a location.
         * @param key to be added
         * @param entry to be added
         */
        public void addItemToMap(String key, String entry);
    }

    public static interface EventID extends Item {
    }

    public static interface IntegerRep extends Item {
        public int getDefault();
        public long getMin();
        public long getMax();

        public int getSize();
        
        // Did the CDI content hint that this value should be presented as a slider?
        public boolean isSliderHint();
        // Should the slider itself immediately write its value on change?
        public boolean isSliderImmediate();
        // Optionally specifies the 'distance' between tick marks on the slider.
        // If 0 (default value) or 1, don't show tick marks.
        public int getSliderTickSpacing();
        // Optionally specifies if the slider value should be shown in text box
        public boolean isSliderShowValue();
        // Did the CDI content hint that this value should be presented as a radio button?
        public boolean isRadioButtonHint();
    }

    public static interface FloatRep extends Item {
        public double getDefault();
        public double getMin();
        public double getMax();

        public int getSize();
    }

    public static interface BitRep extends Item {
        public boolean getDefault();

        public int getSize();
    }

    public static interface UnknownRep extends Item {
        public boolean getDefault();

        public int getSize();
    }

    public static interface StringRep extends Item {  // "String" causes too many name conflicts

        public int getSize();
    }

    public static interface ActionButtonRep extends Item {
    
        public long getValue();
        public String getButtonText();
        public String getDialogText();

        public int getSize();
    }

}
