package org.openlcb.messages;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TractionControlRequestMessageTest.class,
        TractionControlReplyMessageTest.class,       
        TractionProxyRequestMessageTest.class,       
        TractionProxyReplyMessageTest.class       
})
/**
 * @author  Bob Jacobsen   Copyright 2009, 2012
 */
public class PackageTest {
}
