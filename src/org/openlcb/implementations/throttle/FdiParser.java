package org.openlcb.implementations.throttle;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.openlcb.implementations.MemoryConfigurationService;

/**
 * Helper class to parse the FDI XML representation into a useful set of functions.
 * <p>
 * Created by bracz on 1/17/16.
 */
public class FdiParser {
    private final static Logger logger = Logger.getLogger(FdiParser.class.getName());
    private ArrayList<FunctionInfo> allFunctions;

    public FdiParser(Element root) {
        allFunctions = new ArrayList<>();
        for (Element segment : root.getChildren("segment")) {
            int space = getIntAttribute(segment, "space", MemoryConfigurationService
                    .SPACE_TRACTION_FUNCTION);
            long offset = getIntAttribute(segment, "origin", 0);
            for (Element group : segment.getChildren("group")) {
                offset += getIntAttribute(group, "offset", 0);
                for (Element fn : group.getChildren("function")) {
                    FunctionInfo fni = new FunctionInfo();
                    fni.segment = space;
                    Integer fnNum = getIntChild(fn, "number", null);
                    if (fnNum == null) {
                        logger.warning("Function with no number specified: " + fn.toString());
                        continue;
                    }
                    fni.fn = fnNum;
                    fni.name = fn.getChildText("name");
                    if (fni.name == null) {
                        fni.name = "F" + fni.fn;
                    }
                    fni.size = getIntAttribute(fn, "size", 1);
                    fni.offset = offset;
                    offset += fni.size;
                    String type = fn.getAttributeValue("kind");
                    if (type == null) type = "toggle";
                    if (type.equals("momentary")) {
                        fni.fnType = FunctionType.MOMENTARY;
                    } else if (type.equals("toggle") || type.equals("binary")) {
                        fni.fnType = FunctionType.TOGGLE;
                    } else if (type.equals("analog")) {
                        fni.fnType = FunctionType.ANALOG;
                    } else {
                        logger.warning("Unknown function kind: '" + type + "'");
                        fni.fnType = FunctionType.TOGGLE;
                    }
                    allFunctions.add(fni);
                }
            }
        }
    }

    /**
     * Extracts an integer attribute from an XML element.
     * @param el the element whose attribute we want
     * @param name the name of the attribute
     * @param defaultValue will be returned if the attribute does not exist or is not parseable
     *                     as an integer.
     * @return an integer (the attribute value), or the defaultValue.
     */
    private static Integer getIntAttribute(Element el, String name, Integer defaultValue) {
        Attribute a = el.getAttribute(name);
        if (a == null) return defaultValue;
        try {
            return a.getIntValue();
        } catch (DataConversionException e) {
            logger.info("Error parsing attribute " + name + " on element " + el.getName() + ": " +
                    "unparseable integer '" + a.getValue() + "'");
            return defaultValue;
        }
    }

    private static Integer getIntChild(Element el, String name, Integer defaultValue) {
        String value = el.getChildText(name);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.info("Error parsing element " + name + " on parent " + el.getName() + ": " +
                    "unparseable integer '" + value + "'");
            return defaultValue;
        }
    }

    public List<FunctionInfo> getAllFunctions() {
        return allFunctions;
    }

    public enum FunctionType {
        MOMENTARY,
        TOGGLE,
        ANALOG
    }

    public class FunctionInfo {
        int segment;
        long offset;
        int size;
        int fn;
        String name;
        FunctionType fnType;

        /**
         * @return the memory space identifier for this function.
         */
        public int getSpace() {
            return segment;
        }

        /**
         * @return the offset of this function in the memory space.
         */
        public long getOffset() {
            return offset;
        }

        /**
         * @return the number of bytes that are occupied by this function in the memory space.
         */
        public int getSize() {
            return size;
        }
        /**
         * @return the OpenLCB function number (the 24-bit address to send in Set Function and Query
         * Function traction commands).
         */
        public int getFn() {
            return fn;
        }

        /**
         * @return the user-visible name of the function.
         */
        public String getName() {
            return name;
        }

        /**
         * @return the declared type of the function.
         */
        public FunctionType getType() {
            return fnType;
        }
    }
}
