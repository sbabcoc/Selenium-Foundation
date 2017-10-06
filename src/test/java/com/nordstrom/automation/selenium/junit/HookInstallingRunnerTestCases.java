package com.nordstrom.automation.selenium.junit;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(HookInstallingRunner.class)
@JUnitMethodWatchers({UnitTestWatcher.class})
public class HookInstallingRunnerTestCases {
    
    @Before
    public void unitTestBeforeMethod() {
    }
    
    @Test
    public void unitTestMethod() {
        assertTrue(true);
    }
    
    @After
    public void unitTestAfterMethod() {
    }
    
}
