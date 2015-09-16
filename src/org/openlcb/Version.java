package org.openlcb;


/**
 * Defines a simple place to get the OpenLCB library version string.
 * <P>
 * Note that there are separate version numbers for the OpenLCB spec 
 * and the library itself.  By convention, these agree in the 1st
 * two places.
 * <P>
 * You have to manually keep this synchronized with the manifest file.
 * <P>
 * @author  Bob Jacobsen   Copyright 2011 - 2012
 * @version $Revision: 17977 $
 */

public class Version {

    /**
     * Major number changes with large incompatible
     * changes in requirements or API.
     */
    static final public int major = 0;
     
    /**
     * Minor number changes with change that
     * effects interoperability
     */
    static final public int minor = 6;
     
    /* Specification modifier - updated periodically
     */
    static final public int specMod = 4;

    /* Library modifier - updated periodically
     */
    static final public int libMod = 5;


    /**
     * Provide the current specification version string.  
     *
     * @return The current specification version string
     */
    static public String specVersion() { 
        return ""+major+"."+minor+"."+specMod;
    }
     
    /**
     * Provide the current library version string.  
     *
     * @return The current library version string
     */
    static public String libVersion() { 
        return ""+major+"."+minor+"."+libMod;
    }
     
    /**
     * Standalone print of version string and exit.
     * 
     * This is used in the build.xml to generate parts of the installer release file name, so
     * take care in altering this code to make sure the ant recipes are also suitably modified.
     */
    static public void main(String[] args) {
        System.out.println(specVersion()+" "+libVersion());
    }
}
