package org.openlcb.cdi.jdom;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.openlcb.cdi.*;

/**
 * JDOM-based OpenLCB loader
 *
 * @author Bob Jacobsen Copyright 2011
 * @version $Revision: -1 $
 */
public class JdomCdiReader {

    private final static Logger logger = Logger.getLogger(JdomCdiReader.class.getName());

    public static Element getHeadFromReader(Reader rdr) throws Exception {
        String parserName = org.apache.xerces.parsers.SAXParser.class.getName();
        SAXBuilder builder;

        try {
            //"org.apache.xerces.parsers.SAXParser";
            builder = new SAXBuilder(parserName, false);  // argument
            // controls validation
        } catch (Exception e) {
            builder = new SAXBuilder(false);
        }

        try {
            //builder.setEntityResolver(new jmri.util.JmriLocalEntityResolver());
            builder.setFeature("http://apache.org/xml/features/xinclude", true);
            builder.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not set xinclude feature: {0}", e);
        }

        // for schema validation. Not needed for DTDs, so continue if not found now
        try {
            // parse namespaces, including the noNamespaceSchema
            builder.setFeature("http://xml.org/sax/features/namespaces", true);

            builder.setFeature("http://apache.org/xml/features/validation/schema", false);
            builder.setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not set schema validation feature: {0}", e);
        }

        Document doc;
        try {
            doc = builder.build(rdr);
            return doc.getRootElement();
        } catch (JDOMException e) {
            logger.log(Level.SEVERE, "Could not create Document: {0}", e);
            throw e;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not create Document: {0}", e);
            throw new Exception(e);
        }
    }

    public CdiRep getRep(Element root) {
        return new JdomCdiRep(root);
    }
}
