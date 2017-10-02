package com.nordstrom.automation.selenium.junit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.RunWith;

@RunWith(HookInstallingRunner.class)
@JUnitMethodWatchers({UnitTestWatcher.class})
public class HookInstallingRunnerTest extends JUnitBase {
    
    @Rule
    public final TestWatcher watcher = UnitTestWatcher.getTestWatcher(this);
    
    @Test
    public void testHappyPath() {
        System.out.println("testHappyPath");
    }
}
