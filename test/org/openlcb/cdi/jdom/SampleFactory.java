package org.openlcb.cdi.jdom;

import org.jdom2.*;

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
                .addContent(new Element("manufacturer").addContent("OpenLCB Prototype"))
                .addContent(new Element("model").addContent("Basic sketch"))
                .addContent(new Element("hardwareVersion").addContent("Arduino (any)"))
                .addContent(new Element("softwareVersion").addContent("0.4"))
                .addContent(new Element("map")
                    .addContent(
                        new Element("relation")
                            .addContent(new Element("property").addContent("size"))
                            .addContent(new Element("value").addContent("8 cm by 12 cm"))
                    )
                    .addContent(
                        new Element("relation")
                            .addContent(new Element("property").addContent("weight"))
                            .addContent(new Element("value").addContent("220g"))
                    )
                    .addContent(
                        new Element("relation")
                            .addContent(new Element("property").addContent("power"))
                            .addContent(new Element("value").addContent("12V at 100mA"))
                    )
                )
        );
        
        root.addContent(
            new Element("segment").setAttribute("space","0").setAttribute("origin","0")
                .addContent(new Element("name").addContent("Content"))
                .addContent(new Element("description").addContent("Variables for controlling general operation"))
                .addContent(new Element("group")
                    .addContent(new Element("name").addContent("Produced Events"))
                    .addContent(new Element("description").addContent("The EventIDs for the producers"))
                    .addContent(new Element("eventid"))
                    .addContent(new Element("eventid"))
                )
                .addContent(new Element("group")
                    .addContent(new Element("name").addContent("Consumed Events"))
                    .addContent(new Element("description").addContent("The EventIDs for the consumers"))
                    .addContent(new Element("eventid"))
                    .addContent(new Element("eventid"))
                )
                .addContent(new Element("bit")
                    .addContent(new Element("name").addContent("Regular bit variable"))
                    .addContent(new Element("description").addContent("Demonstrate how a standard bit (boolean) variable can be shown"))
                )
                .addContent(new Element("bit")
                    .addContent(new Element("name").addContent("Bit variable with named states"))
                    .addContent(new Element("description").addContent("Demonstrate how a map relabels the states of a bit (boolean) variable"))
                    .addContent(new Element("map")
                        .addContent(
                            new Element("relation")
                                .addContent(new Element("property").addContent("true"))
                                .addContent(new Element("value").addContent("Lit"))
                        )
                        .addContent(
                            new Element("relation")
                                .addContent(new Element("property").addContent("false"))
                                .addContent(new Element("value").addContent("Not Lit"))
                        )
                    )
                )
        );

        root.addContent(
            new Element("segment").setAttribute("space","1").setAttribute("origin","128")
                .addContent(new Element("name").addContent("Resets"))
                .addContent(new Element("description").addContent("Memory locations controlling resets"))
                .addContent(new Element("int").setAttribute("size","1")
                    .addContent(new Element("name").addContent("Reset"))
                    .addContent(new Element("description").addContent("Controls reloading and clearing node memory. Board must be restarted for this to take effect."))
                    .addContent(new Element("map")
                        .addContent(
                            new Element("relation")
                                .addContent(new Element("property").addContent("85"))
                                .addContent(new Element("value").addContent("(No reset)"))
                        )
                        .addContent(
                            new Element("relation")
                                .addContent(new Element("property").addContent("0"))
                                .addContent(new Element("value").addContent("Reset all to defaults"))
                        )
                        .addContent(
                            new Element("relation")
                                .addContent(new Element("property").addContent("170"))
                                .addContent(new Element("value").addContent("Reset just EventIDs to defaults"))
                        )
                    )
                )
        );

        root.addContent(
            new Element("segment").setAttribute("space","3").setAttribute("origin","0")
                .addContent(new Element("name").addContent("Demos"))
                .addContent(new Element("description").addContent("Demonstrations of various CDI capabilities"))
                .addContent(new Element("group").setAttribute("replication","2")
                    .addContent(new Element("name").addContent("Outer Group"))
                    .addContent(new Element("description").addContent("The contents of this group are replicated by 2"))
                    .addContent(new Element("group").setAttribute("replication","3")
                        .addContent(new Element("name").addContent("Inner Group"))
                        .addContent(new Element("repname").addContent("Inner Label"))
                        .addContent(new Element("description").addContent("The contents of this group are replicated by 3"))
                        .addContent(new Element("int").setAttribute("size","1")
                            .addContent(new Element("name").addContent("Int inside groups"))
                            .addContent(new Element("description").addContent("This is inside a 2x3 group"))
                        )
                    )
                )
        );

        return root;
    }

    /**
     *
     * @return An example configuration with complicated offset computation cases.
     */
    public static Element getOffsetSample() {
        Element root = new Element("cdi");

        root.addContent(new Element("segment").setAttribute("space", "13").setAttribute("origin", "132")
                .addContent(new Element("int").setAttribute("size", "2").setAttribute("offset","21"))
                .addContent(new Element("eventid").setAttribute("offset","3"))
                .addContent(new Element("group").setAttribute("offset", "1"))
                .addContent(new Element("int").setAttribute("size", "1"))
                .addContent(new Element("group").setAttribute("replication", "2").setAttribute("offset", "11")
                        .addContent(new Element("int").setAttribute("size", "2").setAttribute("offset","3"))
                        .addContent(new Element("group").setAttribute("offset", "-5"))
                        .addContent(new Element("group").setAttribute("replication", "3")
                            .addContent(new Element("string").setAttribute("size", "9"))
                        )
                )
                .addContent(new Element("int").setAttribute("size", "2").setAttribute("offset","21"))
        );

        root.addContent(new Element("segment").setAttribute("space", "14").setAttribute("origin", "0").addContent(new Element("int").setAttribute("size", "2")));

        return root;
    }

    /**
     *
     * @return An example configuration with a long string field.
     */
    public static Element getLargeStringSample() {
        Element root = new Element("cdi");

        root.addContent(new Element("segment").setAttribute("space", "13")
                .addContent(new Element("string").setAttribute("size", "200"))
        );
        return root;
    }

    /**
     * A sample CDI where a large string variable is sandwiched by two small variables.
     * @return Parsed XML root.
     */
    public static Element getLargeStringWithNeighborsSample() {
        Element root = new Element("cdi");

        root.addContent(new Element("segment").setAttribute("space", "13")
                .addContent(new Element("int").setAttribute("size", "2"))
                .addContent(new Element("string").setAttribute("size", "200").addContent(new Element("name").setText("longdata")))
                .addContent(new Element("int").setAttribute("size", "2"))
        );
        return root;
    }


    // Main entry point for standalone run
    static public void main(String[] args) {
        // dump a document to stdout
        Element root = getOffsetSample();
        Document doc = new Document(root);
        
        try {
            org.jdom2.output.XMLOutputter fmt = new org.jdom2.output.XMLOutputter();
        
            fmt.setFormat(org.jdom2.output.Format.getPrettyFormat());
        
            fmt.output(doc, System.out);
        } catch (Exception e) {
            System.err.println("Exception writing file: "+e);
        }

    }
}
