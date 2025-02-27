package org.openlcb.implementations;

import org.junit.*;

import java.util.*;
import org.openlcb.*;

/**
 * Tests for the LocationServiceUtils class
 @
 @ @author Bob Jacobsen  (C) 2025
 */
public class LocationServiceUtilsTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testParseNotEventID() {

        NodeID source =      new NodeID("02.03.04.05.06.07");
        EventID eID = new EventID("01.01.02.03.04.05.06.07"); // not proper prefix
        ArrayList<Byte> list = bytes(Arrays.asList(
                                0x01, 0x00, 0x02, 0x03, 0x04, 0x05, 0x08, 0x09  // flags, being scanned
                                                                                // no content blocks                                       
                            ));
        Message msg = new ProducerConsumerEventReportMessage(source, eID, list);
        
        LocationServiceUtils.Content result = LocationServiceUtils.parse(msg);
        
        Assert.assertNull(result);
    }

    @Test
    public void testParseWrongMessageType() {

        NodeID source =      new NodeID("02.03.04.05.06.07");
        Message msg = new InitializationCompleteMessage(source);
        
        LocationServiceUtils.Content result = LocationServiceUtils.parse(msg);
        
        Assert.assertNull(result);
    }

    // helper to make it easier to create ArrayList<Byte> from a List
    ArrayList<Byte> bytes(List<Integer> input ) {
        ArrayList<Byte> retval = new ArrayList<Byte>();
        for (int index = 0; index < input.size(); index++) {
            retval.add( (byte) (input.get(index)&0xFF) );
        }
        return retval;
    }
    
    @Test
    public void testParseAbortShortLS() {

        NodeID source =      new NodeID("02.03.04.05.06.07");
        EventID eID = new EventID("01.02.02.03.04.05.06.07"); // prefix, scanner
        ArrayList<Byte> list = new ArrayList<Byte>();
        Message msg = new ProducerConsumerEventReportMessage(source, eID, list);
        
        LocationServiceUtils.Content result = LocationServiceUtils.parse(msg);
        
        Assert.assertNull(result);
    }

    @Test
    public void testParseEmptyLS() {

        NodeID source =      new NodeID("02.03.04.05.06.07");
        EventID eID = new EventID("01.02.02.03.04.05.06.07"); // prefix, scanner
        ArrayList<Byte> list = bytes(Arrays.asList(
                                0x01, 0x00, 0x02, 0x03, 0x04, 0x05, 0x08, 0x09  // flags, being scanned
                                                                                // no content blocks                                       
                            ));
        Message msg = new ProducerConsumerEventReportMessage(source, eID, list);
        
        LocationServiceUtils.Content result = LocationServiceUtils.parse(msg);
        
        Assert.assertNotNull(result);
        
        Assert.assertEquals(result.getOverallFlags(), 0x0100);
        Assert.assertEquals(result.getScannerReporting(), new NodeID("02.03.04.05.06.07"));
        Assert.assertEquals(result.getScannedDevice(), new NodeID("02.03.04.05.08.09"));
        Assert.assertEquals(result.getBlocks().size(), 0);
    }

    @Test
    public void testParseEmptyLSwTrailingZeros() {

        NodeID source =      new NodeID("02.03.04.05.06.07");
        EventID eID = new EventID("01.02.02.03.04.05.06.07"); // prefix, scanner
        ArrayList<Byte> list = bytes(Arrays.asList(
                                0x01, 0x00, 0x02, 0x03, 0x04, 0x05, 0x08, 0x09,  // flags, being scanned
                                0x00, 0x00                                       // zero-length blocks                                     
                            ));
        Message msg = new ProducerConsumerEventReportMessage(source, eID, list);
        
        LocationServiceUtils.Content result = LocationServiceUtils.parse(msg);
        
        Assert.assertNotNull(result);

        Assert.assertEquals(result.getOverallFlags(), 0x0100);
        Assert.assertEquals(result.getScannerReporting(), new NodeID("02.03.04.05.06.07"));
        Assert.assertEquals(result.getScannedDevice(), new NodeID("02.03.04.05.08.09"));
        Assert.assertEquals(result.getBlocks().size(), 2);

        // check the 1st block's contents
        LocationServiceUtils.Block block = result.getBlocks().get(0);
        Assert.assertEquals(block.getLength(), 0);
        Assert.assertEquals(block.getType(), LocationServiceUtils.Block.Type.RESERVED);

        // check the 2nd block's contents
        block = result.getBlocks().get(1);
        Assert.assertEquals(block.getLength(), 0);
        Assert.assertEquals(block.getType(), LocationServiceUtils.Block.Type.RESERVED);

    }

    @Test
    public void testParseTypicalBoosterContent() {

        NodeID source =      new NodeID("02.03.04.05.06.07");
        EventID eID = new EventID("01.02.02.03.04.05.06.07"); // prefix, scanner
        ArrayList<Byte> list = bytes(Arrays.asList(
                                0x10, 0x00, 0x02, 0x03, 0x04, 0x05, 0x08, 0x09,  // flags, being scanned

                                0x11, 0x0B, 0x4B, 0xB1, 0x01, 0x54, 0x72, 0x61,
                                0x63, 0x6B, 0x20, 0x56, 0x6F, 0x6C, 0x74, 0x61,
                                0x67, 0x65, 
                                            0x11, 0x0B, 0x40, 0x42, 0x02, 0x54,
                                0x72, 0x61, 0x63, 0x6B, 0x20, 0x43, 0x75, 0x72,
                                0x72, 0x65, 0x6E, 0x74

                            ));
        Message msg = new ProducerConsumerEventReportMessage(source, eID, list);
        
        LocationServiceUtils.Content result = LocationServiceUtils.parse(msg);
        
        Assert.assertNotNull(result);

        Assert.assertEquals(result.getOverallFlags(), 0x1000);
        Assert.assertEquals(result.getScannerReporting(), new NodeID("02.03.04.05.06.07"));
        Assert.assertEquals(result.getScannedDevice(), new NodeID("02.03.04.05.08.09"));
        Assert.assertEquals(result.getBlocks().size(), 2);
                
        // check the 1st block's contents
        LocationServiceUtils.Block block = result.getBlocks().get(0);
        Assert.assertEquals(block.getLength(), 0x11);
        Assert.assertEquals(block.getType(), LocationServiceUtils.Block.Type.ANALOG);
        Assert.assertEquals(((LocationServiceUtils.AnalogBlock) block).getUnit(), LocationServiceUtils.AnalogBlock.Unit.VOLTS);
        Assert.assertEquals(((LocationServiceUtils.AnalogBlock) block).getText(), "Track Voltage");
        Assert.assertEquals(((LocationServiceUtils.AnalogBlock) block).getValue(), 15.382812, 0.0001);
        

        // check the 2nd block's contents
        block = result.getBlocks().get(1);
        Assert.assertEquals(block.getLength(), 0x11);
        Assert.assertEquals(block.getType(), LocationServiceUtils.Block.Type.ANALOG);
        Assert.assertEquals(((LocationServiceUtils.AnalogBlock) block).getUnit(), LocationServiceUtils.AnalogBlock.Unit.AMPERES);
        Assert.assertEquals(((LocationServiceUtils.AnalogBlock) block).getText(), "Track Current");
        Assert.assertEquals(((LocationServiceUtils.AnalogBlock) block).getValue(), 2.12890625, 0.0001);

    }

}
