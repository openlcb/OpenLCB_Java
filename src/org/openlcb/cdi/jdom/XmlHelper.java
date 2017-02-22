package org.openlcb.cdi.jdom;

import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * Created by bracz on 3/31/16.
 */
public class XmlHelper {

    private final static Logger logger = Logger.getLogger(XmlHelper.class.getName());

    public static Element parseXmlFromReader(Reader r) throws Exception {
        SAXBuilder builder = new SAXBuilder(false);

        // parse namespaces, including the noNamespaceSchema
        builder.setFeature("http://xml.org/sax/features/namespaces", true);

        Document doc;
        try {
            doc = builder.build(r);
            return doc.getRootElement();
        } catch (JDOMException | IOException e) {
            logger.log(Level.SEVERE, "Could not create Document: {0}", e);
            throw e;
        }
    }
}
