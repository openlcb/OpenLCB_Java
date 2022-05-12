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
    static final public int minor = 7;
     
    /* Specification modifier - updated periodically
     */
    static final public int specMod = 4;

    /* Library modifier - updated periodically
     */
    static final public int libMod = 30;

    /**
     * Checks if the current specification version is above a specific threshold.
     * @param maj threshold major version
     * @param min threshold minor version
     * @param mod threshold specification modifier
     * @return true if current specification version &gt;= maj.min.mod
     */
    static public boolean specVersionAtLeast(int maj, int min, int mod) {
        return major > maj || minor > min || specMod >= mod;
    }

    /**
     * Checks if the current library version is above a specific threshold.
     * @param maj threshold major version
     * @param min threshold minor version
     * @param mod threshold library modifier
     * @return true if current library version &gt;= maj.min.mod
     */
    static public boolean libVersionAtLeast(int maj, int min, int mod) {
        return major > maj || minor > min || libMod >= mod;
    }

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
     * @param args    commandline
     */
    static public void main(String[] args) {
        System.out.println(specVersion()+" "+libVersion());
    }
}
