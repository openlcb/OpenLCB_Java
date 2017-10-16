package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.FileInputStream;
import java.util.jar.Manifest;

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

    public void testLibMin() throws Exception {
        assertTrue(Version.libVersionAtLeast(Version.major, Version.minor, Version.libMod));
        assertFalse(Version.libVersionAtLeast(Version.major, Version.minor,
                Version.libMod + 1));
        assertTrue(Version.libVersionAtLeast(Version.major - 1, Version.minor,
                Version.libMod + 1));
        assertTrue(Version.libVersionAtLeast(Version.major, Version.minor - 1,
                Version.libMod + 1));
    }

    public void testSpecMin() throws Exception {
        assertTrue(Version.specVersionAtLeast(Version.major, Version.minor,
                Version.specMod));
        assertFalse(Version.specVersionAtLeast(Version.major, Version.minor,
                Version.specMod + 1));
        assertTrue(Version.specVersionAtLeast(Version.major - 1, Version.minor,
                Version.specMod + 1));
        assertTrue(Version.specVersionAtLeast(Version.major, Version.minor - 1,
                Version.specMod + 1));
    }

   // from here down is testing infrastructure

    public VersionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {VersionTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(VersionTest.class);
        return suite;
    }
}
