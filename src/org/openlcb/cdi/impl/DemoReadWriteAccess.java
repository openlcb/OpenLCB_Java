package org.openlcb.cdi.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.openlcb.implementations.MemoryConfigurationService;

/**
 * Helper class for various demo and test code to put in as a fake into the ConfigRepresentation constructor.
 * Created by bracz on 11/20/16.
 */
public class DemoReadWriteAccess extends ReadWriteAccess {

    private final static Logger logger = Logger.getLogger(DemoReadWriteAccess.class.getName());
    
    @Override
    public void doWrite(long address, int space, byte[] data, MemoryConfigurationService.McsWriteHandler handler) {
        logger.log(Level.INFO, "Wrote {0} bytes", data.length);
        logger.log(Level.INFO, "write {0} {1}: {2}", new Object[]{address, space, org.openlcb.Utilities.toHexDotsString(data)});
    }

    @Override
    public void doRead(long address, int space, int length, MemoryConfigurationService.McsReadHandler handler) {
        byte[] resp = new byte[length];
        for (int i = 0; i < resp.length; ++i) {
            resp[i] = (byte)(((address + i) % 91) + 32);
        }
        handler.handleReadData(null, space, address, resp);
        logger.log(Level.INFO, "read {0} {1}", new Object[]{address, space});
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
        } catch (IOException | JDOMException e) { logger.log(Level.INFO, "While reading file: {0}", e);}

        return demoRepFromSample(root);
    }

}
