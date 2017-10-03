package com.nordstrom.automation.selenium.junit;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(HookInstallingRunner.class)
@JUnitMethodWatchers({UnitTestWatcher.class})
public class HookInstallingRunnerTest extends JUnitBase {
    
    @Test
    public void testHappyPath() {
        System.out.println("testHappyPath");
    }
}
