package org.openlcb.cdi.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.openlcb.EventID;
import org.openlcb.NodeID;
import org.openlcb.can.impl.OlcbConnection;
import org.openlcb.cdi.impl.ConfigRepresentation;

/**
 * Created by bracz on 4/9/16.
 */
public class RestoreConfig {

    private final static Logger logger = Logger.getLogger(RestoreConfig.class.getName());
    
    public static interface ConfigCallback {
        void onConfigEntry(String key, String value);
        void onError(String error);
    }

    public static void parseConfigFromFile(@Nonnull String filePath, @Nonnull ConfigCallback callback) {
        BufferedReader inFile = null;
        try {
            inFile = Files.newBufferedReader(Paths.get(filePath), Charset.forName("UTF-8"));
        } catch (IOException e) {
            callback.onError("Failed to open input file: " + e.toString());
            return;
        }
        String line = null;
        try {
            while ((line = inFile.readLine()) != null) {
                if (line.charAt(0) == '#') continue;
                int pos = line.indexOf('=');
                if (pos < 0) {
                    logger.log(Level.WARNING, "Failed to parse line: {0}", line);
                    continue;
                }
                String key = Util.unescapeString(line.substring(0, pos));
                String value = Util.unescapeString(line.substring(pos + 1));
                callback.onConfigEntry(key, value);
            }

            inFile.close();
        } catch (IOException x) {
            callback.onError("Error reading input file: " + x.toString());
            return;
        }
    }

    // Main entry point
    static public void main(String[] args) {
        if (args.length != 5) {
            usage();
            return;
        }
        System.out.println("arg0: " + args[0]);
        NodeID localNode = new NodeID(args[0]);
        final String host = args[1];
        final int port = Integer.parseInt(args[2]);
        final NodeID remoteNode = new NodeID(args[3]);
        final String srcFileName = args[4];

        final OlcbConnection connection = Util.connect(localNode, host, port);
        System.out.println("Fetching CDI.");
        ConfigRepresentation repr = connection.getConfigForNode(remoteNode);
        Util.waitForPropertyChange(repr, ConfigRepresentation.UPDATE_REP);
        System.out.println("CDI fetch done. Waiting for caches.");
        Util.waitForPropertyChange(repr, ConfigRepresentation.UPDATE_CACHE_COMPLETE);
        System.out.println("Caches complete. Writing variables to the node.");
        parseConfigFromFile(srcFileName, new ConfigCallback() {
            @Override
            public void onConfigEntry(String key, String value) {
                ConfigRepresentation.CdiEntry e = repr.getVariableForKey(key);
                if (e == null) {
                    System.out.println("Variable not found: " + key);
                    return;
                }
                if (e instanceof ConfigRepresentation.EventEntry) {
                    ((ConfigRepresentation.EventEntry) e).setValue(new EventID(value));
                } else if (e instanceof ConfigRepresentation.IntegerEntry) {
                    ((ConfigRepresentation.IntegerEntry) e).setValue(Long.parseLong(value));
                } else if (e instanceof ConfigRepresentation.StringEntry) {
                    ((ConfigRepresentation.StringEntry) e).setValue(value);
                } else {
                    System.out.println("Unknown variable type: " + e.getClass().getName() + " for" +
                            " key: " + key);
                    return;
                }
                Util.waitForPropertyChange(e, ConfigRepresentation.UPDATE_WRITE_COMPLETE);
                System.out.print(e.key + "\n"); System.out.flush();
            }

            @Override
            public void onError(String error) {
                System.err.println(error);
                System.exit(1);
            }
        });
        System.out.println("Done.");
        System.exit(0);
    }

    private static void usage() {
        String usageString = "usage: loadconfig local_node_id hub_host hub_port dst_node_id " +
                "src_filename\n";
        System.err.print(usageString);
    }

}
