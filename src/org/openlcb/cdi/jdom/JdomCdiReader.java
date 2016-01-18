package org.openlcb.cdi.jdom;

import org.openlcb.cdi.*;

import java.io.*;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

/**
 * JDOM-based OpenLCB loader
 *
 * @author  Bob Jacobsen   Copyright 2011
 * @version $Revision: -1 $
 */
public class JdomCdiReader {

    public static Element getHeadFromReader(Reader rdr) throws Exception {
        String parserName = org.apache.xerces.parsers.SAXParser.class.getName();
        SAXBuilder builder;

        try {
            //"org.apache.xerces.parsers.SAXParser";
            builder = new SAXBuilder(parserName,false);  // argument
            // controls validation
        } catch (Exception e) {
            builder = new SAXBuilder(false);
        }

        try {
            //builder.setEntityResolver(new jmri.util.JmriLocalEntityResolver());
            builder.setFeature("http://apache.org/xml/features/xinclude", true);
            builder.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
        } catch (Exception e) {
            System.err.println("Could not set xinclude feature: "+e);
        }

        // for schema validation. Not needed for DTDs, so continue if not found now
        try {
            // parse namespaces, including the noNamespaceSchema
            builder.setFeature("http://xml.org/sax/features/namespaces", true);

            builder.setFeature("http://apache.org/xml/features/validation/schema", false);
            builder.setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);
        } catch (Exception e) {
            System.err.println("Could not set schema validation feature: "+e);
        }
        
        Document doc;
        try {
            doc = builder.build(rdr);
            return doc.getRootElement();
        } catch (JDOMException e) {
            System.err.println("Could not create Document: "+e);
            throw e;
        } catch (IOException e) {
            System.err.println("Could not create Document: "+e);
            throw new Exception(e);
        }        
    }
    
    public CdiRep getRep(Element root) {
        return new JdomCdiRep(root);
    }
}
