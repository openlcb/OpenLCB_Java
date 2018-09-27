package org.openlcb.cdi.impl;

import org.junit.*;

import org.jdom2.Document;
import org.jdom2.Element;
import org.openlcb.FakeOlcbInterface;
import org.openlcb.NodeID;
import org.openlcb.cdi.jdom.SampleFactory;
import org.openlcb.implementations.FakeMemoryConfigurationService;

import java.io.StringWriter;

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

            String s = fmt.outputString(doc);
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
        mcs.addSpace(remoteNode, mcs.SPACE_CONFIG, config, true);
        ConfigRepresentation rep = new ConfigRepresentation(iface, remoteNode);
        // Since all of our memory configuration commands execute inline, the representation will
        // be ready by the time it returns.
        Assert.assertEquals("Representation complete.", rep.getStatus());
        Assert.assertNotNull(rep.getRoot());

        ConfigRepresentation.CdiContainer cont = rep.getRoot();
        Assert.assertEquals(2, cont.getEntries().size());

        class Offset {
            int i;
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
                super.visitLeaf(e);
            }
        });
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
