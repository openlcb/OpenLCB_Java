package org.openlcb.cdi.impl;

import org.openlcb.implementations.MemoryConfigurationService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
  * Provide access to e.g. a MemoryConfig service.
 *
  * Default just writes output for debug
  */
public class ReadWriteAccess {
    private static final Logger logger = Logger.getLogger(ReadWriteAccess.class.getName());
    public void doWrite(long address, int space, byte[] data, final
                        MemoryConfigurationService.McsWriteHandler handler) {
        logger.log(Level.FINE, "Write to {0} in space {1}", new Object[]{address, space});
    }
    public void doRead(long address, int space, int length, final MemoryConfigurationService
            .McsReadHandler handler) {
        logger.log(Level.FINE, "Read from {0} in space {1}", new Object[]{address, space});
    }
}
