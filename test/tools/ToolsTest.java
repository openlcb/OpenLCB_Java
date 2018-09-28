package tools;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TimerTest.class,
        tools.cansim.CanSimTest.class,
})

/**
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class ToolsTest  {
}
