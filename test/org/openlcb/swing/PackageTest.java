package org.openlcb.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import javax.swing.*;

import org.openlcb.swing.*;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MonPaneTest.class,
        NodeSelectorTest.class,
        EventIdTextFieldTest.class,
        org.openlcb.swing.networktree.PackageTest.class,
        org.openlcb.swing.memconfig.PackageTest.class,
        ConsumerPaneTest.class,       
        ProducerPaneTest.class,       
        NodeIdTextFieldTest.class       
})

/**
 * @author  Bob Jacobsen   Copyright 2009
 */
public class PackageTest {
}
