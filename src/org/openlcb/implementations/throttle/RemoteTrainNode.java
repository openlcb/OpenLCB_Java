package org.openlcb.implementations.throttle;

import java.io.Reader;
import java.util.logging.Logger;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jdom2.Element;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.cdi.jdom.CdiMemConfigReader;
import org.openlcb.cdi.jdom.XmlHelper;
import org.openlcb.implementations.MemoryConfigurationService;

/**
 * Represents local view about a remote Train Node, a node that implements the Traction protocol.
 *
 * @author Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class RemoteTrainNode {

    public static final String UPDATE_PROP_FDI = "fdi";
    private final static Logger logger = Logger.getLogger(RemoteTrainNode.class.getName());
    private final OlcbInterface iface;
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    private Element fdiRoot;
    private NodeID node;
    private String fdiXml = null;

    public RemoteTrainNode(NodeID node, OlcbInterface iface) {
        this.node = node;
        this.iface = iface;
    }

    public NodeID getNodeId() {
        return node;
    }

    public boolean hasFdiXml() {
        return fdiXml != null;
    }

    public synchronized void setFdiXmlCached(String payload) {
        if (fdiXml != null) { fdiXml = payload; }
    }

    public synchronized void flushCache() {
        fdiRoot = null;
    }

    public synchronized Element getFdiXml() {
        if (fdiRoot != null) return fdiRoot;
        new CdiMemConfigReader(node, iface, MemoryConfigurationService.SPACE_TRACTION_FDI)
                .startLoadReader(new CdiMemConfigReader.ReaderAccess() {
                    @Override
                    public void progressNotify(long bytesRead, long totalBytes) {

                    }

                    @Override
                    public void provideReader(Reader r) {
                        try {
                            fdiRoot = XmlHelper.parseXmlFromReader(r);//JdomCdiReader.getHeadFromReader(r);
                        } catch (Exception e) {
                            logger.warning("Unable to parse returned FDI from train " + node
                                    .toString());
                            e.printStackTrace();
                            //throw new RuntimeException(e);
                            return;
                        }
                        firePropertyChange(UPDATE_PROP_FDI, null, fdiRoot);
                    }
                });
        return null;
    }

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }
}
