package org.openlcb.cdi.impl;

import junit.framework.TestCase;

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
public class ConfigRepresentationTest extends TestCase {
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

    public void testComplexCdiLoad() throws Exception {
        addCdiData(SampleFactory.getOffsetSample());
        byte[] config = new byte[1000];
        mcs.addSpace(remoteNode, mcs.SPACE_CONFIG, config, true);
        ConfigRepresentation rep = new ConfigRepresentation(iface, remoteNode);
        assertEquals("Representation complete.", rep.getStatus());
        assertNotNull(rep.getRoot());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        iface = new FakeOlcbInterface();
        mcs = new FakeMemoryConfigurationService(iface);
    }
}