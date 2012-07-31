// SimpleNodeIdent.java

package org.openlcb;

import java.util.ArrayList;
import java.util.List;

import java.nio.charset.Charset;

/**
 * Accumulates data from Simple Node Ident Protocol replies and 
 * provides access to the resulting data to represent a single node.
 *
 * @see             "http://www.openlcb.org/trunk/specs/drafts/GenProtocolIdS.pdf"
 * @author			Bob Jacobsen   Copyright (C) 2012
 * @version			$Revision: 18542 $
 *
 */
public class SimpleNodeIdent {
    
    /*
     * @param msg Message, already known to be from proper node.
     */
    public SimpleNodeIdent( SimpleNodeIdentInfoReplyMessage msg) {
        byte data[] = msg.getData();
        for (int i = 0; i < data.length ; i++ ) {
           bytes[next++] = (byte)data[i];
        }
    }
    public SimpleNodeIdent(NodeID source, NodeID dest) {
        this.source = source;
        this.dest = dest;
    }

    NodeID source;
    NodeID dest;
    
    void start(Connection connection) {
        next = 0;
        connection.put(new SimpleNodeIdentInfoRequestMessage(source, dest), null);
    }
    
    static final Charset UTF8 = Charset.forName("UTF8");

    static final int MAX_REPLY_LENGTH = 256; // TODO from standard
    byte[] bytes = new byte[MAX_REPLY_LENGTH];
    int next = 0;
   
    public void addMsg(SimpleNodeIdentInfoReplyMessage msg) {
        byte data[] = msg.getData();
        for (int i = 0; i < data.length ; i++ ) {
           bytes[next++] = (byte)data[i];
        }
    }
        
    public String getMfgName() {
        int len = 1;
        int start = 1;
        // skip mfg
        for (; len < bytes.length; len++)
            if (bytes[len] == 0) break;
       String s = new String(bytes,start, len-start, UTF8);
       if (s == null) return "";
       else return s;
    }
    public String getModelName() {
        int len = 1;
        int start = 1;
        // skip mfg
        for (; len < bytes.length; len++)
            if (bytes[len] == 0) break;
        start = ++len;
        for (; len < bytes.length; len++)
            if (bytes[len] == 0) break;
       String s = new String(bytes,start, len-start, UTF8);
       if (s == null) return "";
       else return s;
    }
    public String getHardwareVersion() {
        int len = 1;
        int start = 1;
        // skip mfg, model
        for (int i = 0; i<2; i++) {
            for (; len < bytes.length; len++)
                if (bytes[len] == 0) break;
            start = ++len;
        }
        
        // find this string
        for (; len < bytes.length; len++)
            if (bytes[len] == 0) break;
       String s = new String(bytes,start, len-start, UTF8);
       if (s == null) return "";
       else return s;
    }

    public String getSoftwareVersion() {
        int len = 1;
        int start = 1;
        // skip mfg, model, hardware_version
        for (int i = 0; i<3; i++) {
            for (; len < bytes.length; len++)
                if (bytes[len] == 0) break;
            start = ++len;
        }
        
        // find this string
        for (; len < bytes.length; len++)
            if (bytes[len] == 0) break;
       String s = new String(bytes,start, len-start, UTF8);
       if (s == null) return "";
       else return s;
    }

    public String getUserName() {
        int len = 1;
        int start = 1;
        // skip mfg, model, hardware_version, software_version, version byte
        for (int i = 0; i<4; i++) {
            for (; len < bytes.length; len++)
                if (bytes[len] == 0) break;
            start = ++len;
        }
        if (len < bytes.length) start = ++len;
        
        // find this string
        for (; len < bytes.length; len++)
            if (bytes[len] == 0) break;
       String s = new String(bytes,start, len-start, UTF8);
       if (s == null) return "";
       else return s;
    }

    public String getUserDesc() {
        int len = 1;
        int start = 1;
        // skip mfg, model, hardware_version, software_version, version byte, user_name
        for (int i = 0; i<4; i++) {
            for (; len < bytes.length; len++)
                if (bytes[len] == 0) break;
            start = ++len;
        }
        if (len < bytes.length) start = ++len;
        for (int i = 0; i<1; i++) {
            for (; len < bytes.length; len++)
                if (bytes[len] == 0) break;
            start = ++len;
        }
        
        // find this string
        for (; len < bytes.length; len++)
            if (bytes[len] == 0) break;
       String s = new String(bytes,start, len-start, UTF8);
       if (s == null) return "";
       else return s;
    }

}
