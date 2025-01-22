package org.openlcb.cdi.cmd;

import org.openlcb.NodeID;
import org.openlcb.Utilities;
import org.openlcb.can.impl.OlcbConnection;
import org.openlcb.cdi.impl.ConfigRepresentation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by bracz on 4/9/16.
 */
public class BackupConfig {


    static public void writeEntry(BufferedWriter writer, String key, String value) {
        try {
            writer.write(Util.escapeString(key));
            writer.write('=');
            writer.write(Util.escapeString(value));
            writer.write('\n');
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(1);
        }
    }

    public static void writeConfigToFile(String fileName, ConfigRepresentation repr) throws
            IOException {
        
        BufferedWriter outFile = Files.newBufferedWriter(Paths.get(fileName), Charset.forName("UTF-8"));
        
        writeConfigToWriter(outFile, repr);
        
        outFile.close();
    }

    /**
     * @param writer Receives output.  Flushed at end, but not closed.
     * @param repr Representation containing contents to be written.
     * @throws IOException if trouble writing out
     */
    public static void writeConfigToWriter(BufferedWriter writer, ConfigRepresentation repr) throws
            IOException {

        final BufferedWriter finalWriter = writer;
        repr.visit(new ConfigRepresentation.Visitor() {
                       @Override
                       public void visitString(ConfigRepresentation.StringEntry e) {
                           writeEntry(finalWriter, e.key, e.getValue());
                       }

                       @Override
                       public void visitInt(ConfigRepresentation.IntegerEntry e) {
                           writeEntry(finalWriter, e.key, Long.toString(e.getValue()));
                       }

                       @Override
                       public void visitEvent(ConfigRepresentation.EventEntry e) {
                           writeEntry(finalWriter, e.key, e.getValue());
                       }
                   }
        );
        
        finalWriter.flush();
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
        final String dstFile = args[4];

        final OlcbConnection connection = Util.connect(localNode, host, port);
        System.out.println("Fetching CDI.");
        ConfigRepresentation repr = connection.getConfigForNode(remoteNode);
        Util.waitForPropertyChange(repr, ConfigRepresentation.UPDATE_REP);
        System.out.println("CDI fetch done. Waiting for caches.");
        Util.waitForPropertyChange(repr, ConfigRepresentation.UPDATE_CACHE_COMPLETE);
        System.out.println("Caches complete. Writing variables.");

        try {
            writeConfigToFile(dstFile, repr);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Done.");
        System.exit(0);
    }

    private static void usage() {
        String usageString = "usage: saveconfig local_node_id hub_host hub_port dst_node_id " +
                "dst_filename\n";
        System.err.print(usageString);
    }

}
