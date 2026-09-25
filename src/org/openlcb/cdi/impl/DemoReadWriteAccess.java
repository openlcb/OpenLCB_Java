package org.openlcb.cdi.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
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
    
    // store written values for later reading
    private long getAddressHash(long address, int space, int length) {
        return address+1000000*space+1000000000*length;
    }
    
    private final static HashMap<Long, byte[]> contents = new HashMap<>();
    
    @Override
    public void doWrite(long address, int space, byte[] data, MemoryConfigurationService.McsWriteHandler handler) {
        logger.log(Level.INFO, "write {0} {1} with {2} bytes: {3}", new Object[]{address, space, data.length, org.openlcb.Utilities.toHexDotsString(data)});
        
        contents.put(getAddressHash(address, space, data.length), data);
    }

    @Override
    public void doRead(long address, int space, int length, MemoryConfigurationService.McsReadHandler handler) {
        byte[] resp = contents.get(getAddressHash(address, space, length));
        if (resp == null) {
            // no prior write, load with ascii letters
            resp = new byte[length];
            for (int i = 0; i < resp.length; ++i) {
                resp[i] = (byte)(((address + i) % 91) + 32);
            }
        }
        handler.handleReadData(null, space, address, resp);
        logger.log(Level.INFO, "read {0} {1} with {2} bytes: {3}", new Object[]{address, space, resp.length, org.openlcb.Utilities.toHexDotsString(resp)});
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
