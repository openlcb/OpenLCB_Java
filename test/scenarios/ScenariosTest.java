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
        scenarios.can.CanScenariosTest.class
})
/**
 * Primary test runner for this package.
 * This Package is named so that maven will automatically pick it up to run.
 *
 * @author  Bob Jacobsen   Copyright 2009
 */
public class ScenariosTest {
    
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
