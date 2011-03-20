package org.openlcb.cdi.jdom;

import org.openlcb.cdi.*;

import java.io.*;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

/**
 * JDOM-based OpenLCB loader
 *
 * @author  Bob Jacobsen   Copyright 2011
 * @version $Revision: -1 $
 */
public class JdomCdiReader {

    Element getHeadFromReader(Reader rdr) throws Exception {
        SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser",false);  // argument controls validation
        
        //builder.setEntityResolver(new jmri.util.JmriLocalEntityResolver());
        builder.setFeature("http://apache.org/xml/features/xinclude", true);
        builder.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
        
        // for schema validation. Not needed for DTDs, so continue if not found now
        try {
            builder.setFeature("http://apache.org/xml/features/validation/schema", false);
            builder.setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);

            // parse namespaces, including the noNamespaceSchema
            builder.setFeature("http://xml.org/sax/features/namespaces", true);

        } catch (Exception e) {
            System.err.println("Could not set schema validation feature: "+e);
        }
        
        Document doc;
        try {
            doc = builder.build(rdr);
            return doc.getRootElement();
        } catch (JDOMException e) {
            System.err.println("Could not create Document: "+e);
            throw new Exception(e);
        } catch (IOException e) {
            System.err.println("Could not create Document: "+e);
            throw new Exception(e);
        }        
    }
    
    public CdiRep getRep(Element root) {
        return new JdomCdiRep(root);
    }
}
