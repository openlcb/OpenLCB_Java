package org.openlcb.cdi.jdom;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.Reader;

/**
 * Created by bracz on 3/31/16.
 */
public class XmlHelper {
    public static Element parseXmlFromReader(Reader r) throws Exception {
        SAXBuilder builder = new SAXBuilder(false);

        // parse namespaces, including the noNamespaceSchema
        builder.setFeature("http://xml.org/sax/features/namespaces", true);

        Document doc;
        try {
            doc = builder.build(r);
            return doc.getRootElement();
        } catch (JDOMException | IOException e) {
            System.err.println("Could not create Document: " + e);
            throw e;
        }
    }
}
