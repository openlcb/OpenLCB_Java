package org.openlcb.cdi.impl;

import org.junit.*;

import org.jdom2.Document;
import org.jdom2.Element;
import org.openlcb.FakeOlcbInterface;
import org.openlcb.NodeID;
import org.openlcb.cdi.jdom.SampleFactory;
import org.openlcb.implementations.FakeMemoryConfigurationService;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Created by bracz on 11/9/16.
 */
public class ConfigRepresentationTest {
    protected FakeOlcbInterface iface;
    protected FakeMemoryConfigurationService mcs;
    protected NodeID remoteNode = new NodeID("05.01.01.01.14.39");

    protected void addCdiData(Element root) {
        Document doc = new Document(root);
        try {
            org.jdom2.output.XMLOutputter fmt = new org.jdom2.output.XMLOutputter();

            fmt.setFormat(org.jdom2.output.Format.getPrettyFormat());

            String s = fmt.outputString(doc) + "\0";
            byte[] b = s.getBytes();
            mcs.addSpace(remoteNode, mcs.SPACE_CDI, b, false);
        } catch (Exception e) {
            System.err.println("Exception rendering CDI: " + e);
        }
    }

    @Test
    public void testComplexCdiLoad() throws Exception {
        addCdiData(SampleFactory.getOffsetSample());
        byte[] config = new byte[1000];
        mcs.addSpace(remoteNode, 13, config, true);
        mcs.addSpace(remoteNode, 14, config, true);
        ConfigRepresentation rep = new ConfigRepresentation(iface, remoteNode);
        // Since all of our memory configuration commands execute inline, the representation will
        // be ready by the time it returns.
        Assert.assertEquals("Representation complete.", rep.getStatus());
        Assert.assertNotNull(rep.getRoot());

        ConfigRepresentation.CdiContainer cont = rep.getRoot();
        Assert.assertEquals(2, cont.getEntries().size());

        class Offset {
            int i = 0;
        }
        final Offset o = new Offset();
        final int[][] readOffsets = {
                {153, 2, 13},
                {158, 8, 13},
                {167, 1, 13},
                {182, 2, 13},
                {179, 9, 13},
                {188, 9, 13},
                {197, 9, 13},
                {209, 2, 13},
                {206, 9, 13},
                {215, 9, 13},
                {224, 9, 13},
                {254, 2, 13},
                {0, 2, 14}
        };
        rep.visit(new ConfigRepresentation.Visitor() {
            @Override
            public void visitLeaf(ConfigRepresentation.CdiEntry e) {
                Assert.assertTrue(o.i < readOffsets.length);
                Assert.assertEquals(e.space, readOffsets[o.i][2]);
                Assert.assertEquals(e.size, readOffsets[o.i][1]);
                Assert.assertEquals(e.origin, readOffsets[o.i][0]);
                o.i++;
                super.visitLeaf(e);
            }
        });
    }

    @Test
    public void testStringWrites() throws Exception {
        addCdiData(SampleFactory.getLargeStringSample());
        byte[] config = new byte[1000];
        mcs.addSpace(remoteNode, 13, config, true);
        ConfigRepresentation rep = new ConfigRepresentation(iface, remoteNode);
        ConfigRepresentation.CdiContainer cont = rep.getRoot();
        Assert.assertNotNull(cont);

        Assert.assertEquals(1, cont.getEntries().size());

        // Uses the visitor to find the string entry.
        class PE {
            ConfigRepresentation.StringEntry e = null;
        }
        final PE p = new PE();
        rep.visit(new ConfigRepresentation.Visitor() {
            @Override
            public void visitString(ConfigRepresentation.StringEntry e) {
                p.e = e;
            }
        });
        Assert.assertNotNull(p.e);

        p.e.setValue("abcdef");
        Assert.assertEquals(1, mcs.actualWriteList.size());
        FakeMemoryConfigurationService.ActualWrite aw = mcs.actualWriteList.get(0);
        Assert.assertEquals(0, aw.address);
        Assert.assertEquals(7, aw.data.length);
        mcs.actualWriteList.clear();

        char[] carr = new char[151];
        for (int i = 0; i < 151; ++i) {
            carr[i] = (char) (64 + i % 10);
        }
        String testLong = new String(carr);
        p.e.setValue(testLong);

        Assert.assertEquals(3, mcs.actualWriteList.size());
        Assert.assertEquals(13, mcs.actualWriteList.get(0).space);
        Assert.assertEquals(0, mcs.actualWriteList.get(0).address);
        Assert.assertEquals(64, mcs.actualWriteList.get(0).data.length);
        Assert.assertArrayEquals(new byte[]{ //
                64, 65, 66, 67, 68, 69, 70, 71, 72, 73, // 10
                64, 65, 66, 67, 68, 69, 70, 71, 72, 73, // 20
                64, 65, 66, 67, 68, 69, 70, 71, 72, 73, // 30
                64, 65, 66, 67, 68, 69, 70, 71, 72, 73, // 40
                64, 65, 66, 67, 68, 69, 70, 71, 72, 73, // 50
                64, 65, 66, 67, 68, 69, 70, 71, 72, 73, // 60
                64, 65, 66, 67 //
        }, mcs.actualWriteList.get(0).data);

        Assert.assertEquals(13, mcs.actualWriteList.get(1).space);
        Assert.assertEquals(64, mcs.actualWriteList.get(1).address);
        Assert.assertEquals(64, mcs.actualWriteList.get(1).data.length);
        Assert.assertArrayEquals(new byte[]{ //
                68, 69, 70, 71, 72, 73, // 6
                64, 65, 66, 67, 68, 69, 70, 71, 72, 73, // 16
                64, 65, 66, 67, 68, 69, 70, 71, 72, 73, // 26
                64, 65, 66, 67, 68, 69, 70, 71, 72, 73, // 36
                64, 65, 66, 67, 68, 69, 70, 71, 72, 73, // 46
                64, 65, 66, 67, 68, 69, 70, 71, 72, 73, // 56
                64, 65, 66, 67, 68, 69, 70, 71 // 64
        }, mcs.actualWriteList.get(1).data);


        Assert.assertEquals(13, mcs.actualWriteList.get(2).space);
        Assert.assertEquals(128, mcs.actualWriteList.get(2).address);
        Assert.assertEquals(24, mcs.actualWriteList.get(2).data.length);
        Assert.assertArrayEquals(new byte[]{ //
                72, 73, // 2
                64, 65, 66, 67, 68, 69, 70, 71, 72, 73, // 12
                64, 65, 66, 67, 68, 69, 70, 71, 72, 73, // 22
                64, 0 // NULL termination!
        }, mcs.actualWriteList.get(2).data);
    }

    @Test
    public void testStringWriteClips() throws Exception {
        addCdiData(SampleFactory.getOffsetSample());
        byte[] config = new byte[1000];
        mcs.addSpace(remoteNode, 13, config, true);
        mcs.addSpace(remoteNode, 14, config, true);
        ConfigRepresentation rep = new ConfigRepresentation(iface, remoteNode);
        ConfigRepresentation.CdiContainer cont = rep.getRoot();
        Assert.assertNotNull(cont);

        // Uses the visitor to find the string entry.
        class PE {
            ConfigRepresentation.StringEntry e = null;
        }
        final PE p = new PE();
        rep.visit(new ConfigRepresentation.Visitor() {
            @Override
            public void visitString(ConfigRepresentation.StringEntry e) {
                p.e = e;
            }
        });
        Assert.assertNotNull(p.e);

        p.e.setValue("12345678901234567"); // too long
        Assert.assertEquals(1, mcs.actualWriteList.size());
        FakeMemoryConfigurationService.ActualWrite aw = mcs.actualWriteList.get(0);
        Assert.assertEquals(224, aw.address);
        Assert.assertEquals(9, aw.data.length);
        Assert.assertArrayEquals(new byte[]{ //
                0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0
        }, mcs.actualWriteList.get(0).data);
    }

    @Test
    public void testLongStringLoad() throws Exception {
        addCdiData(SampleFactory.getLargeStringWithNeighborsSample());
        byte[] config = new byte[1000];
        mcs.addSpace(remoteNode, 13, config, true);

        ConfigRepresentation rep = new ConfigRepresentation(iface, remoteNode);
        ConfigRepresentation.CdiContainer cont = rep.getRoot();
        Assert.assertNotNull(cont);

        List<FakeMemoryConfigurationService.ActualRead> trail = mcs.actualReadList.subList(mcs.actualReadList.size() - 3, mcs.actualReadList.size());
        Assert.assertEquals(trail.get(0).space, 13);
        Assert.assertEquals(trail.get(1).space, 13);
        Assert.assertEquals(trail.get(2).space, 13);
        Assert.assertEquals(trail.get(0).size, 2);
        Assert.assertEquals(trail.get(1).size, 64);
        Assert.assertEquals(trail.get(2).size, 2);
        Assert.assertEquals(trail.get(0).address, 0);
        Assert.assertEquals(trail.get(1).address, 2);
        Assert.assertEquals(trail.get(2).address, 202);

        mcs.actualReadList.clear();

        ConfigRepresentation.CdiEntry e = rep.getVariableForKey("seg1.longdata");
        Assert.assertTrue(e instanceof ConfigRepresentation.StringEntry);
        e.reload();
        Assert.assertEquals(1, mcs.actualReadList.size());
        trail = mcs.actualReadList;
        Assert.assertEquals(trail.get(0).space, 13);
        Assert.assertEquals(trail.get(0).address, 2);
        Assert.assertEquals(trail.get(0).size, 64);

        // Now save some data into the string that makes it longer.
        for (int i = 0; i < 129; i++) {
            config[2+i] = 64;
        }
        mcs.actualReadList.clear();
        e.reload();
        Assert.assertEquals(3, mcs.actualReadList.size());
        Assert.assertEquals(trail.get(0).space, 13);
        Assert.assertEquals(trail.get(0).address, 2);
        Assert.assertEquals(trail.get(0).size, 64);
        Assert.assertEquals(trail.get(1).space, 13);
        Assert.assertEquals(trail.get(1).address, 66);
        Assert.assertEquals(trail.get(1).size, 64);
        Assert.assertEquals(trail.get(2).space, 13);
        Assert.assertEquals(trail.get(2).address, 130);
        Assert.assertEquals(trail.get(2).size, 64);
    }

    @Before
    public void setUp() {
        iface = new FakeOlcbInterface();
        mcs = new FakeMemoryConfigurationService(iface);
    }

    @After
    public void tearDown(){
        iface.dispose();
        mcs.dispose();
    }

}
