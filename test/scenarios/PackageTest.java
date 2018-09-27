package scenarios;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.Test;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        NineOnALink.class,
        TwoBuses.class,
        TwoBusesFiltered.class,
        ThreeBuses.class,
        scenarios.can.CanScenarios.class
})
/**
 * Primary test runner for this package.
 *
 * @author  Bob Jacobsen   Copyright 2009
 * @version $Revision$
 */
public class PackageTest {
    
    // BlueGoldCheck not JUnit so can run standalone
    @Test
    public void testBlueGold() throws Exception {
        BlueGoldCheck.runTest();
    }
    
    // ConfigDemoApplet not JUnit so can run standalone
    @Test
    public void testConfigDemoApplet() throws Exception {
        ConfigDemoApplet.runTest();
    }

}
