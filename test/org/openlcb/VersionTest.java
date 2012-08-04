package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.*;
import java.util.jar.*;
import java.io.*;
import java.net.*;

/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class VersionTest extends TestCase {

    public void testLibVersion() throws Exception {
            Manifest manifest = new Manifest(new FileInputStream("manifest"));
            Assert.assertEquals("Manifest must match Version class", manifest.getEntries().get("org.openlcb").getValue("Package-Version"), Version.libVersion());
    }
    
    public void testSpecVersion() throws Exception {
            Manifest manifest = new Manifest(new FileInputStream("manifest"));
            Assert.assertEquals("Manifest must match Version class", manifest.getEntries().get("org.openlcb").getValue("Specification-Version"), Version.specVersion());
    }
    
   // from here down is testing infrastructure
    
    public VersionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {VersionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(VersionTest.class);
        return suite;
    }
}
