package org.openlcb.cdi.jdom;

import org.jdom.*;

import org.openlcb.cdi.CdiRep;

/**
 * Static methods for creating sample XML trees.
 *
 * @author  Bob Jacobsen   Copyright 2011
 * @version $Revision: 34 $
 */
public class SampleFactory {
    
    public static Element getBasicSample() {
        Element root = new Element("cdi");
        
        root.addContent(
            new Element("identification")
                .addContent(new Element("manufacturer").addContent("mfg1"))
                .addContent(new Element("model").addContent("mod1"))
                .addContent(new Element("hardwareVersion").addContent("hard1"))
                .addContent(new Element("softwareVersion").addContent("soft1"))
                .addContent(new Element("map").addContent(
                        new Element("relation")
                            .addContent(new Element("property").addContent("extra property"))
                            .addContent(new Element("value").addContent("extra value"))
                ))
        );
        
        root.addContent(
            new Element("segment").setAttribute("space","2")
        );

        root.addContent(
            new Element("segment")
        );

        return root;
    }
    
}
