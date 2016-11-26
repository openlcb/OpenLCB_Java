package org.openlcb.cdi.impl;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.openlcb.cdi.swing.CdiPanel;
import org.openlcb.implementations.MemoryConfigurationService;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Helper vlass for various demo and test code to put in as a fake into the ConfigRepresentation constructor.
 * Created by bracz on 11/20/16.
 */
public class DemoReadWriteAccess extends CdiPanel.ReadWriteAccess {
    @Override
    public void doWrite(long address, int space, byte[] data, MemoryConfigurationService.McsWriteHandler handler) {
        System.out.println(data.length);
        System.out.println("write " + address + " " + space + ": " + org.openlcb.Utilities.toHexDotsString(data));
    }

    @Override
    public void doRead(long address, int space, int length, MemoryConfigurationService.McsReadHandler handler) {
        byte[] resp = new byte[length];
        for (int i = 0; i < resp.length; ++i) {
            resp[i] = (byte)i;
        }
        handler.handleReadData(null, space, address, resp);
        System.out.println("read " + address + " " + space);
    }

    static public ConfigRepresentation demoRepFromSample(Element root) {
        ConfigRepresentation rep = new ConfigRepresentation(new DemoReadWriteAccess(), new org.openlcb.cdi.jdom.JdomCdiRep(
                root
        ));
        return rep;
    }

    static public ConfigRepresentation demoRepFromFile(File file) {
        Element root = null;
        try {
            SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", false);  // argument controls validation
            Document doc = builder.build(new BufferedInputStream(new FileInputStream(file)));
            root = doc.getRootElement();
        } catch (Exception e) { System.out.println("While reading file: "+e);}

        return demoRepFromSample(root);
    }

}
