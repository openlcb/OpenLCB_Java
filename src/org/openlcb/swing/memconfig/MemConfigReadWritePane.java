// MemConfigReadWritePane.java

package org.openlcb.swing.memconfig;

import javax.swing.*;
import javax.swing.text.*;
import java.beans.PropertyChangeListener;

import org.openlcb.*;
import org.openlcb.implementations.*;

/**
 * Provide read/write access to a node
 *
 * @author	Bob Jacobsen   Copyright (C) 2012
 * @version	$Revision$
 */
public class MemConfigReadWritePane extends JPanel  {
    
    MimicNodeStore store;
    public MemConfigReadWritePane(MimicNodeStore store) {
        this.store = store;        
    }
    
}
