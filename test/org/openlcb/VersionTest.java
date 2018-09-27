package org.openlcb;

import org.junit.*;

import java.io.FileInputStream;
import java.util.jar.Manifest;

/**
 * @author  Bob Jacobsen   Copyright 2012
 */
public class VersionTest  {

    @Test
    public void testLibVersion() throws Exception {
            Manifest manifest = new Manifest(new FileInputStream("manifest"));
            Assert.assertEquals("Manifest must match Version class", manifest.getEntries().get("org.openlcb").getValue("Package-Version"), Version.libVersion());
    }

    @Test
    public void testSpecVersion() throws Exception {
            Manifest manifest = new Manifest(new FileInputStream("manifest"));
            Assert.assertEquals("Manifest must match Version class", manifest.getEntries().get("org.openlcb").getValue("Specification-Version"), Version.specVersion());
    }

    @Test
    public void testLibMin() throws Exception {
        Assert.assertTrue(Version.libVersionAtLeast(Version.major, Version.minor, Version.libMod));
        Assert.assertFalse(Version.libVersionAtLeast(Version.major, Version.minor,
                Version.libMod + 1));
        Assert.assertTrue(Version.libVersionAtLeast(Version.major - 1, Version.minor,
                Version.libMod + 1));
        Assert.assertTrue(Version.libVersionAtLeast(Version.major, Version.minor - 1,
                Version.libMod + 1));
    }

    @Test
    public void testSpecMin() throws Exception {
        Assert.assertTrue(Version.specVersionAtLeast(Version.major, Version.minor,
                Version.specMod));
        Assert.assertFalse(Version.specVersionAtLeast(Version.major, Version.minor,
                Version.specMod + 1));
        Assert.assertTrue(Version.specVersionAtLeast(Version.major - 1, Version.minor,
                Version.specMod + 1));
        Assert.assertTrue(Version.specVersionAtLeast(Version.major, Version.minor - 1,
                Version.specMod + 1));
    }

}
